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

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * An instance of the Eclipse Compiler for Java that compiles memory files into
 * memory byte code
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

		// get system compiler:
		final JavaCompiler compiler = new EclipseCompiler();

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
		options.addAll(Arrays.asList("-1.8", "-nowarn"));

		Writer out = new PrintWriter(System.out);
		JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diag, options, null, files);

		Boolean result = task.call();
		if (result == true) {
			return classLoader;
		}
		return null;
	}
}
