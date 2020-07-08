package zss.Tester;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import greenfoot.Actor;
import greenfoot.World;
import run.myCode.compiler.SimpleFile;
import zss.compiler.InMemoryJavaFileObject;
import zss.compiler.MemoryCompiler;

/**
 * Verify that a MyZombie.java file solves the given problem
 *
 * @author bdahl
 */
public class ZombieLandTester {

    /**
     * Prepare and run a demo of MyZombie in a world
     *
     * @param scenarios the xml descriptions of the scenarios to test
     * @param zombieSource the java source code for MyZombie.java
     * @param maxTime the maximum time to allow for running the scenario
     * @param allowUltraZombie if the zombieSource is allowed to extend UltraZombie
     * @return the result of the test
     */
    public static List<Result> doScenario(SimpleFile[] scenarios,
            String zombieSource, long maxTime, boolean allowUltraZombie) {
        // long startTime = System.nanoTime();

        List<Result> results = new ArrayList<>();

        // Create a stream to hold system output during tests
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        PrintStream oldErr = System.err;
        System.setOut(ps);
        System.setErr(ps);

        try {
            InMemoryJavaFileObject myZombie = 
                    new InMemoryJavaFileObject("MyZombie.java", zombieSource);

            List<InMemoryJavaFileObject> files = new ArrayList<>();
            files.add(myZombie);
            
            // Create a classloader that can load from the current folder
            URL[] urls = new URL[]{ZombieLandTester.class.getProtectionDomain()
                    .getCodeSource().getLocation()};

            final URLClassLoader urlcl = new URLClassLoader(urls, 
                    ZombieLandTester.class.getClassLoader());

            Class<?> zlc = urlcl.loadClass("ZombieLand");

            ClassLoader cl = MemoryCompiler.compile(files, urlcl);
            
            if (!allowUltraZombie) {
                Class<?> mz = cl.loadClass("MyZombie");
                if (!mz.getSuperclass().getSimpleName().equals("Zombie")) {
                    throw new MyZombieMustExtendZombieException();
                }
            }
            
            // long compTime = System.nanoTime();
            // System.err.printf("Compile time: %.2f\n", (compTime - startTime) / 1.0e9);
            if (cl != null) {
                Method lw = zlc.getMethod("loadWorld", String.class,
                        ClassLoader.class);

                for (SimpleFile scenarioDesc : scenarios) {
                    // Create the world from the description, passing in the classloader
                    World zl = (World) lw.invoke(null,  scenarioDesc.getData(),
                            cl);

                    Result r = runTest(zl, maxTime);

                    r.setOutput("World: " + scenarioDesc.getName() + '\n' +
                            baos.toString());
                    baos.reset();

                    results.add(r);
                }
            } else {
                Result r = new Result(false, "Plan no make sense.", null, 0, 0);
                r.setOutput(baos.toString());
                baos.reset();
                results.add(r);
            }
        } 
        catch (InvocationTargetException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println(e.getTargetException().getMessage());
            e.getTargetException().printStackTrace(pw);

            Result r = new Result(false, sw.toString(), null, 0, 0);
            r.setOutput(baos.toString() + sw.toString());
            results.add(r);
        }
        catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            // If running causes an expression, log the failure
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            Result r = new Result(false, sw.toString(), null, 0, 0);
            r.setOutput(baos.toString() + sw.toString());

            results.add(r);
        }
        catch (MyZombieMustExtendZombieException e) {
            // If the user attempted to extend UltraZombie
            Result r = new Result(false, e.getMessage(), null, 0, 0);
            results.add(r);
        }
        finally {
            System.setOut(old);
            System.setErr(oldErr);
        }
        
        return results;
    }

    /**
     * Run the Scenario
     *
     * @param zl the ZombieLand to test (must be an instance of ZombieLand)
     * @param maxTime the maximum time to allow for running the scenario
     * @return the result of the test
     */
    private static Result runTest(World zl, long maxTime)
            throws IllegalArgumentException, ReflectiveOperationException {

        if (!zl.getClass().getName().equals("ZombieLand")) {
            throw new IllegalArgumentException("Bad world class passed to test runner");
        }

        long startTime = System.nanoTime();
        long actCount = 1;

        // Repeatedly run the scenario by calling act on the world and all of
        // the actors therein
        while ((System.nanoTime() - startTime < maxTime)
                && !(Boolean) zl.getClass().getMethod("isFinished").invoke(zl)) {
            zl.act();

            List<Actor> actors = zl.getObjects(null);
            actors.forEach(a -> a.act());
            actCount++;
            Thread.yield();
        }

        // System.err.println("Test Complete");
        // Once the scenario completes or runs out of time, generate and return
        // the result
        boolean success = (Boolean) zl.getClass().getMethod("success").invoke(zl);
        String finalMessage = (String) zl.getClass().getMethod("finalMessage").invoke(zl);

        // System.err.println("Making image");
        Image image = (Image) zl.getClass().getMethod("image").invoke(zl);

        double elapsed = (System.nanoTime() - startTime) / 1000000000D;

        // System.err.println("Ending it all");
        zl.getClass().getMethod("endIt").invoke(zl);

        return new Result(success, finalMessage, image, elapsed, actCount);
    }
}
