package zss.compiler;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import javax.tools.ToolProvider;

/**
 * Compiles in-memory source files into byte code using the standard JDK
 * compiler. This avoids relying on the Eclipse compiler which attempted to
 * resolve sources on disk.
 */
public class MemoryCompiler {
	/**
	 * Compile Java source files into memory
	 *
	 * @param files
	 *            a list of JavaFileObjects containing the source code to compile
	 * @param urlcl
	 *            a ClassLoader to use in finding dependencies of the project source
	 * 
	 * @return A ClassLoader containing the class and its dependencies
	 * @throws ClassNotFoundException
	 */
	public static FromMemoryClassLoader compile(Iterable<? extends JavaFileObject> files, URLClassLoader urlcl)
			throws ClassNotFoundException {
		// Create a memory file classloader that resolves classes using the url
		// classloader
		final FromMemoryClassLoader classLoader = new FromMemoryClassLoader(urlcl);

                // Use the standard JDK compiler to avoid ECJ attempting to read
                // temporary files from disk which caused "File ... is missing"
                // errors when compiling in memory.
                final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		// create a diagnostic listener for compilation diagnostic message processing on
		// compilation WARNING/ERROR
		final CompileDiagnosticListener diag = new CompileDiagnosticListener();
		final StandardJavaFileManager stdfileManager = compiler.getStandardFileManager(diag, Locale.ENGLISH, null);

		InMemoryJavaFileManager fileManager = new InMemoryJavaFileManager(stdfileManager, classLoader);

		// specify options for compiler
		List<String> options = new ArrayList<>();
		options.addAll(
				Arrays.asList("-classpath", MemoryCompiler.class.getProtectionDomain().getCodeSource().getLocation()
						+ ":" + System.getProperty("java.class.path")));
                options.addAll(Arrays.asList("--release", "17", "-nowarn"));


		Writer out = new PrintWriter(System.out);
		JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diag, options, null, files);

		Boolean result = task.call();
		if (result == true) {
			return classLoader;
		}
		return null;
	}
}
