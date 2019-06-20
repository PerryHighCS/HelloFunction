package run.myCode.compiler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.tools.JavaFileObject;
import org.junit.runner.JUnitCore;
import static run.myCode.Hello.MAX_ZOMBIETIME;
import run.myCode.ResultInnumerator;
import run.myCode.TestResult;
import run.myCode.ZombieResult;
import zss.Tester.ZombieLandTester;


public class CodeRunner {
    /**
     * Compile and Run submitted java files
     *
     * @param files the list of java source files to compile
     * @param mainClass the name of the class containing the main method to run
     * @return true if compilation and running completed without exceptions
     */
    public boolean runIt(Iterable<? extends JavaFileObject> files, String mainClass) {
        // Compile the files using the JavaCompiler
        try {
            FromMemoryClassLoader classLoader = JavaCodeCompiler.compile(files, null);

            Class<?> compiledClass = classLoader.findClass(mainClass);

            // Call main method of compiled class by reflection
            compiledClass.getMethod("main", String[].class).invoke(null, new Object[]{null});
            compiledClass = null;
            classLoader = null;
        }
        catch (ClassNotFoundException | NullPointerException e) {
            // Handle exceptions caused by the code being compiled
            if (System.err != System.out) {
                System.err.println(e.toString());
            }
            System.out.println(e.toString());
            System.out.println("Main class: " + mainClass + " not found in source files, could not execute.");
            return false;
        }
        catch (NoSuchMethodException | IllegalArgumentException e) {
            // Handle exceptions caused by an incorrect main method
            if (System.err != System.out) {
                System.err.println(e.toString());
            }
            System.out.println(e.toString());
            System.out.println("Main class: " + mainClass
                    + " does not contain a \"main\" method, or main method has incorrect parameter list.");
            return false;
        }
        catch (IllegalAccessException e) {
            // Handle exceptions caused by incorrect visibility on main method
            if (System.err != System.out) {
                System.err.println(e.toString());
            }
            System.err.println(stackTrace(e.getStackTrace(), null));
            System.out.println(e.toString());
            System.out.println(stackTrace(e.getStackTrace(), null));
            System.out.println("Main class: " + mainClass + " \"main\" method is inaccessable.  Is it \"public\"?");
            return false;
        }
        catch (InvocationTargetException e) {
            // Handle exceptions caused inside of the main method
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }

            StackTraceElement[] frames = cause.getStackTrace();

            // Show the reason for the exception
            if (System.err != System.out) {
                System.err.println(cause.toString());
            }
            System.out.println(cause.toString());
            System.out.println("Call Stack:");

            // Show the call stack for the exception... but do not include any of stack
            // containing this method or below
            System.out.println(stackTrace(frames, this.getClass().getCanonicalName()  + ".runIt"));

            return false;
        }
        catch (OutOfMemoryError | SecurityException | ExceptionInInitializerError e) {
            // If there is an out of memory, the gc should have run, but call it again Sam
            System.gc();

            // Display the exception and call stack
            StackTraceElement[] frames = e.getStackTrace();

            if (System.err != System.out) {
                System.err.println(e.toString());
            }
            System.out.println(e.toString());
            System.out.println("Call Stack:");
            
            // Show only the part of the call stack above this method
            System.out.println(stackTrace(frames, this.getClass().getCanonicalName() + ".runIt"));

            return false;
        }

        return true;
    }

    /**
     * Compile and Run submitted java files
     *
     * @param files the source files to compile (including test files)
     * @param tests the names of classes containing tests to run
     * @return true if compilation and running completed without exceptions
     */
    public TestResult testIt(Iterable<? extends JavaFileObject> files, List<String> tests) {
        TestResult score = new TestResult();

        FromMemoryClassLoader classLoader;
        
        try {
            // Compile the source files using the JavaCompiler
            classLoader = JavaCodeCompiler.compile(files, null);
        } 
        catch (ClassNotFoundException | NullPointerException e) {
            // If compiling caused an exception, fail
            if (System.err != System.out) {
                System.err.println(e.toString());
            }
            System.out.println(e.toString());
            return score;
        }

        // Create a stream to hold system output during tests
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        JUnitCore junit = new JUnitCore();
        ResultInnumerator allResults = new ResultInnumerator(baos);
        junit.addListener(allResults);

        // Have JUnit run the test classes specified
        tests.forEach((test) -> {
            try {
                // Run the test
                junit.run(classLoader.loadClass(test));

                // Store the results from the test
                TestResult tr = allResults.retrieveResults();
                score.addResults(tr);
            } 
            catch (ClassNotFoundException | NullPointerException e) {
                // If the test caused an error, Log the error
                System.err.println(e.toString());

                // Add a failed test to the results
                score.addFailedTest(test,
                        "The test class " + test + " was not found. Check \"Test Summary\" for compilation errors.");
            } catch (OutOfMemoryError e) {
                // If there is an out of memory, the gc should have run, but call it again Sam
                System.gc();

                // Log the error
                System.err.println(e.toString());

                // Add a failed test to the results
                score.addFailedTest(e.toString(),
                        baos.toString() + stackTrace(e.getStackTrace(), this.getClass().getCanonicalName() + ".testIt"));
            }
        });

        // Clear the output and restore the original
        if (System.out != null) 
            System.out.flush();
   
        System.setOut(old);

        return score;
    }

    /**
     * Compile and run ZombieLand code
     * 
     * @param myZombieSource the MyZombie.java source file
     * @param scenarios a list of the XML descriptions of the scenarios to test
     * 
     * @return the results of the test
     */
    public ZombieResult zombieDo(String myZombieSource, List<String> scenarios) {
        ZombieResult res = new ZombieResult();

        // Strip out comments
        myZombieSource = myZombieSource.replaceAll("//(?:.|)*?[\\n\\r]", "") // singleline comments
                .replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", ""); // multiline comments

        // If there is an =
        if (myZombieSource.matches("[\\S\\s]*(?<![=!])=(?!=)[\\S\\s]*")) {
            // Quit because there's an assignment statement
            return badZombie("Zombie not know =");
        } else if (myZombieSource.matches("[\\S\\s]*\\d[\\S\\s]*")) {
            // Quit because there's a scary number
            return badZombie("Zombie not know numbers");
        } else if (myZombieSource.contains("<")) {
            // Quit because there's a comparison statement
            return badZombie("Zombie not know <");
        } else if (myZombieSource.contains(">")) {
            // Quit because there's a comparison statement
            return badZombie("Zombie not know >");
        } else if (myZombieSource.contains("+")) {
            // Quit because there's a math expression
            return badZombie("Zombie not know +");
        } else if (myZombieSource.contains("-")) {
            // Quit because there's a math expression
            return badZombie("Zombie not know -");
        }

        // Compile and run the scenarios
        List<zss.Tester.Result> zr = ZombieLandTester.doScenario(scenarios.toArray(new String[0]), myZombieSource,
                MAX_ZOMBIETIME);

        // Extract and save the results from each scenario
        zr.forEach(r -> {
            ZombieResult.ScenarioResult scenarioCase = new ZombieResult.ScenarioResult();
            scenarioCase.setDescription(r.message());
            scenarioCase.setPassed(r.success());
            scenarioCase.setBody(r.getOutput());
            scenarioCase.setImage(r.image());
            scenarioCase.setElapsedTime(r.getTime());
            scenarioCase.setActCount(r.getActCount());
            res.addCase(scenarioCase);
        });

        return res;
    }

    /**
     * Create an error result for bad zombieland code
     * @param message The message to report
     * 
     * @return a failure result
     */
    private static ZombieResult badZombie(String message) {
        ZombieResult res = new ZombieResult();

        ZombieResult.ScenarioResult scenarioCase = new ZombieResult.ScenarioResult();
        scenarioCase.setDescription(message);
        scenarioCase.setPassed(false);
        scenarioCase.setBody(message);
        scenarioCase.setImage(
                "data:image/png;base64,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");
        scenarioCase.setElapsedTime(0.0);
        scenarioCase.setActCount(0);

        res.addCase(scenarioCase);

        return res;
    }

    /**
     * Create a stack trace containing information from the top of the stack
     * down to (not including) the class.method stackBottom
     * 
     * @param frames the frames of the stack trace
     * @param stackBottom the "class.method" below which nothing should be
     * included in the trace
     * 
     * @return A string representation of the stack down to (but not including)
     * the stackBottom
     */
    private static String stackTrace(StackTraceElement[] frames, String stackBottom) {
        StringBuilder trace = new StringBuilder("");

        // Add the stack frames to the trace until the method "stackBottom" is reached
        for (StackTraceElement frame : frames) {
            if (!(frame.getClassName() + "." + frame.getMethodName()).equals(stackBottom)) {
                trace.append("\t");
                trace.append(frame.toString());
                trace.append("\n");
            } else {
                break;
            }
        }
        return trace.toString();
    }

}
