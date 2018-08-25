package run.myCode.compiler;

import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class JavaCodeCompiler {

	/**
	 * Compile Java source files into memory
	 * 
	 * @param files
	 *            a List of JavaFileObjects containing the source code to compile
	 * @param options
	 *            a List of compiler options (null means none)
	 * @return The compiled main class
	 * @throws ClassNotFoundException
	 */
	public static FromMemoryClassLoader compile(Iterable<? extends JavaFileObject> files, List<String> options)
			throws ClassNotFoundException {

		// Setup a default list of dependencies, including junit testing
		URL[] urls = null;
		try {
			urls = new URL[] { new URL("file:///var/task/lib/junit-4.12.jar") };
		} catch (MalformedURLException e) {
			System.err.println(e);
		}

		final URLClassLoader urlcl = new URLClassLoader(urls, JavaCodeCompiler.class.getClassLoader());

		return compile(files, urlcl, options);
	}

	/**
	 * Compile Java source files into memory
	 * 
	 * @param files
	 *            a List of JavaFileObjects containing the source code to compile
	 * @param urlcl
	 *            a ClassLoader that contains dependencies of the files being
	 *            compiled
	 * @param options
	 *            a List of compiler options (null means none)
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
			options = new ArrayList<String>();
		}

		options.addAll(Arrays.asList("-classpath",
				".:" + System.getProperty("java.class.path") + ":/var/task/lib/junit-4.12.jar"));
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
