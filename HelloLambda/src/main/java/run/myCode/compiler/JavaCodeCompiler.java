package run.myCode.compiler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class JavaCodeCompiler {

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
     * @return The compiled main class
     * @throws ClassNotFoundException
     */
    public static FromMemoryClassLoader compile(Iterable<? extends JavaFileObject> files, ClassLoader urlcl,
            List<String> options) throws ClassNotFoundException {

        final FromMemoryClassLoader classLoader = new FromMemoryClassLoader(urlcl);
        // get system compiler:
        final JavaCompiler compiler = new EclipseCompiler();

        // create a diagnostic listener for compilation diagnostic message processing on
        // compilation WARNING/ERROR
        final CompileDiagnosticListener diag = new CompileDiagnosticListener();
        final StandardJavaFileManager stdfileManager = compiler.getStandardFileManager(diag, Locale.ENGLISH, null);

        InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(stdfileManager, classLoader);

        // specify options for compiler
        if (options == null) {
            options = new ArrayList<>();
        }

        StringBuilder classpathBuilder = 
                new StringBuilder("." 
                    + System.getProperty("path.separator") 
                    + System.getProperty("java.class.path"));

        try {
            Files.list(new File("/var/task/lib/").toPath())
                    .filter(s -> s.toString().endsWith(".jar") || s.toString().endsWith(".JAR"))
                    .forEach(f -> {
                        classpathBuilder.append(System.getProperty("path.separator"));
                        classpathBuilder.append(f.toString());
                     });
        } catch (IOException ex) {
            Logger.getLogger(JavaCodeCompiler.class.getName()).log(Level.SEVERE, null, ex);
        }

        String classpath = classpathBuilder.toString();
        System.out.println(">> " + classpath + " <<");
        
        options.addAll(Arrays.asList("-classpath",
                classpath));
        options.addAll(Arrays.asList("-1.8"));

        Writer out = new PrintWriter(System.out);
        JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diag, options, null, files);

        boolean result = task.call();
        // if (result == true) {
        return classLoader;
        // }
        // return null;
    }

}
