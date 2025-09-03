package run.myCode.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import javax.tools.ToolProvider;

public class JavaCodeCompiler {

    private static final boolean DEBUG = Boolean.getBoolean("run.mycode.debug");

    /**
     * Compile Java source files into memory
     *
     * @param files a List of JavaFileObjects containing the source code to
     * compile
     * @param options a List of compiler options (null means none)
     * @return The compiled main class
     * @throws ClassNotFoundException
     */
    public static FromMemoryClassLoader compile(Iterable<? extends JavaFileObject> files, List<String> options)
            throws ClassNotFoundException {

        return compile(files, JavaCodeCompiler.class.getClassLoader(), options);
    }

    /**
     * Compile Java source files into memory
     *
     * @param files a List of JavaFileObjects containing the source code to
     * compile
     * @param urlcl a ClassLoader that contains dependencies of the files being
     * compiled
     * @param options a List of compiler options (null means none)
     *
     * @return A classloader containing the compiled classes and their dependencies
     * 
     * @throws ClassNotFoundException
     */
    public static FromMemoryClassLoader compile(Iterable<? extends JavaFileObject> files, ClassLoader urlcl,
            List<String> options) throws ClassNotFoundException {

        final FromMemoryClassLoader classLoader = new FromMemoryClassLoader(urlcl);
        // get system compiler:
        // Use the standard Java compiler provided by the JDK.  This avoids ECJ
        // attempting to resolve in-memory sources on disk which resulted in
        // "File ... is missing" errors during tests.
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // create a diagnostic listener for compilation diagnostic message processing on
        // compilation WARNING/ERROR
        final CompileDiagnosticListener diag = new CompileDiagnosticListener();
        final StandardJavaFileManager stdfileManager = compiler.getStandardFileManager(diag, Locale.ENGLISH, null);

        InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(stdfileManager, classLoader);
        for (JavaFileObject file : files) {
            fileManager.addSource(file);
            if (DEBUG) {
                System.out.println("Compiling source " + file.getName());
            }
        }

        // specify options for compiler
        if (options == null) {
            options = new ArrayList<>();
        }

        // Build the classpath from the current folder and the system classpath
        StringBuilder classpathBuilder =
                new StringBuilder("." +
                    System.getProperty("path.separator") +
                    System.getProperty("java.class.path"));

        // Add any included jar files from the lib folder to the classpath (wildcard isn't working)
        try {
            Files.list(new File("/var/task/lib/").toPath())
                    .filter(s -> s.toString().endsWith(".jar") || s.toString().endsWith(".JAR"))
                    .forEach(f -> {
                        classpathBuilder.append(System.getProperty("path.separator"));
                        classpathBuilder.append(f.toString());
                     });
        } 
        catch (NoSuchFileException ex) {
            // if the /var/task/lib folder doesn't exist, no problem.
            //Logger.getLogger(JavaCodeCompiler.class.getName()).log(Level.INFO, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(JavaCodeCompiler.class.getName()).log(Level.SEVERE, null, ex);
        }

        String classpath = classpathBuilder.toString();
//        System.out.println(">> " + classpath + " <<");

        // Set the classpath and java version for the compiler
        options.addAll(Arrays.asList("-classpath",
                classpath));
        options.addAll(Arrays.asList("-17"));

        Writer out = new PrintWriter(System.out);
        
        // Compile the code
        JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diag, options, null, files);
        if (DEBUG) {
            System.out.println("Starting compilation with mem URIs");
        }
        boolean result = task.call();
                
        // Return the classloader containing the compiled classes
        return classLoader;
    }

}
