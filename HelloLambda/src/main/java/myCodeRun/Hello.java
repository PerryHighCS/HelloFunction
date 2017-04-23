package myCodeRun;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.runner.JUnitCore;

public class Hello implements RequestStreamHandler {
	public static final int REQUEST_HANDLER_VERSION = 1;
	public static final int COMPILE_REQUEST_HANDLER_VERSION = 1;
	public static final int RESPONDER_VERSION = 1;
		
	/**
	 * Compile Java source files into memory
	 * @param files a list of JavaFileObjects containing the source code to compile
	 * @return The compiled main class
	 * @throws ClassNotFoundException
	 */
	public SpecialClassLoader compile(Iterable<? extends JavaFileObject> files) throws ClassNotFoundException
	{
		final SpecialClassLoader classLoader = new SpecialClassLoader(this.getClass().getClassLoader());   
		//get system compiler:
		final JavaCompiler compiler = new EclipseCompiler();

		// create a diagnostic listener for compilation diagnostic message processing on compilation WARNING/ERROR
		final MyDiagnosticListener diag = new MyDiagnosticListener();
		final StandardJavaFileManager stdfileManager = compiler.getStandardFileManager(diag,
				Locale.ENGLISH,
				null);
        
		SpecialJavaFileManager fileManager = new SpecialJavaFileManager(stdfileManager, classLoader);           

		//specify options for compiler
		List<String> options = new ArrayList<String>();
		options.addAll(Arrays.asList("-classpath",".:"+System.getProperty("java.class.path")+":/var/task/lib/junit-4.12.jar"));
		options.addAll(Arrays.asList("-1.8"));
		
		Writer out = new PrintWriter(System.out);
		JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager,
				diag, options, null,
				files);
		
		Boolean result = task.call();
		if (result == true)
		{
			return classLoader;
		}
		return null;
	}

	/**
	 * Compile and Run submitted java files
	 * @param files the list of java source files to compile
	 * @return true if compilation and running completed without exceptions
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean runIt(Iterable<? extends JavaFileObject> files, String mainClass) {
		
		// Compile the files using the JavaCompiler
		try {
			SpecialClassLoader classLoader = compile(files);
						
			Class compiledClass = classLoader.findClass(mainClass);
			
			// Call main method of compiled class by reflection
			compiledClass.getMethod("main",String[].class).invoke(null, new Object[]{null}); 
			compiledClass = null;
			classLoader = null;
			
		// Handle exceptions caused by the code being compiled
		} catch (ClassNotFoundException | NullPointerException e) {
			System.err.println(e.toString());
			System.out.println("Main class: " + mainClass + " not found in source files, could not execute.");
			return false;
		} catch (NoSuchMethodException | IllegalArgumentException e) {
			System.err.println(e.toString());
			System.out.println("Main class: " + mainClass + " does not contain a \"main\" method, or main method has incorrect parameter list.");
			return false;
		} catch (IllegalAccessException e) {
			System.err.println(e.toString());
			System.out.println("Main class: " + mainClass + " \"main\" method is inaccessable.  Is it \"public\"?");
			return false;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				cause = e;
			}
			
			StackTraceElement[] frames = cause.getStackTrace();
			
			// Show the reason for the exception
			System.err.println(cause.toString());
			System.out.println(cause.toString());
			System.out.println("Call Stack:");
			
			// Show the call stack for the exception... but do not include any of stack containing this method or below
			System.out.println(stackTrace(frames, "myCodeRun.Hello.runIt"));
			
			return false;
		} catch (OutOfMemoryError | SecurityException | ExceptionInInitializerError e) {
			// If there is an out of memory, the gc should have run, but call it again Sam
			System.gc();
			
			// Display the exception and call stack
			StackTraceElement[] frames = e.getStackTrace();
			
			System.err.println(e.toString());
			System.out.println(e.toString());
			System.out.println("Call Stack:");
			
			// Show only the part of the call stack above this method
			System.out.println(stackTrace(frames, "myCodeRun.Hello.runIt"));
			
			return false;
		}

		return true;
	}
	
	/**
	 * Compile and Run submitted java files
	 * @param files
	 * @return true if compilation and running completed without exceptions
	 */
	public TestResult testIt(Iterable<? extends JavaFileObject> files, List<String> tests) {
		TestResult score = new TestResult();
		
		SpecialClassLoader classLoader;
		// Compile the files using the JavaCompiler
		try {
			classLoader = compile(files);
		} catch (ClassNotFoundException | NullPointerException e) {
			System.err.println(e.toString());
			System.out.println(e.toString());
			return score;
		}
	
		// Create a stream to hold system output during tests
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		
		JUnitCore junit = new JUnitCore();
		//MyJUnit junit = new MyJUnit();
		ResultInnumerator allResults = new ResultInnumerator(baos, "myCodeRun.Hello.testIt");
		junit.addListener(allResults);
		
		// Have JUnit run the test cases specified
		try {
			
			List<Class<?>> classes = new ArrayList<Class<?>>();

			for (String test : tests) {
				classes.add(classLoader.findClass(test));
			}
			//Result result = junit.run(classes.toArray(new Class<?>[0]));
			junit.run(classes.toArray(new Class<?>[0]));
			
			TestResult tr = allResults.retrieveResults();
			
			score.addResults(tr);
			
		} catch (ClassNotFoundException | NullPointerException e) {
			// Log the error
			System.err.println(e.toString());

			// Add a failed test to the results
			score.addFailedTest("Could not run test, not found.", null);
		} catch (OutOfMemoryError e) {
			// If there is an out of memory, the gc should have run, but call it again Sam
			System.gc();
							
			// Log the error
			System.err.println(e.toString());
			
			// Add a failed test to the results
			score.addFailedTest(e.toString(), baos.toString() + stackTrace(e.getStackTrace(), "myCodeRun.Hello.testIt"));
		}

		// Clear the output and restore the original
		System.out.flush();
		System.setOut(old);
		
		return score;
	}

	private String stackTrace(StackTraceElement[] frames, String stackBottom) {
		String trace = "";

		// Add the stack frames to the trace until the method "stackBottom" is reached
		for (StackTraceElement frame : frames) {
			if (!(frame.getClassName() + "." + frame.getMethodName()).equals(stackBottom)) {
				trace += "\t";
				trace += frame.toString();
				trace += "\n";
			}
			else {
				break;
			}
		}
		return trace;
	}

	/**
	 * Create a list of source files from the initial request
	 * 
	 * @param req a RequestClass 
	 * @return
	 * @throws Exception
	 */
	private List<InMemoryJavaFileObject> createSourceFileObjects(CompileRequest req) {
		
		List<InMemoryJavaFileObject> objects = new ArrayList<InMemoryJavaFileObject>(req.getSourceFiles().size());

		// Pull all of the source code files in the request
		for (CompileRequest.FileClass f: req.getSourceFiles()) {
			String contents = "";
			for (String s : f.getContents()) {
				// Save the file's contents
				contents += s + '\n';
			}
			
			// Add them to a list of files
			objects.add(new InMemoryJavaFileObject(f.getName(), contents));
		}
		return objects;
	}
		
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		LambdaAPIRequest apiReq = mapper.readValue(input, LambdaAPIRequest.class);
		
		Request req = apiReq.getReqBody();

		TestResult testResults = null;
		String result = "";
		boolean success;	
		
	
		// Create a stream to hold system output
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(boas);
		PrintStream old = System.out;
		System.setOut(ps);
		
		if (req != null) {
			CompileRequest cReq = req.getCompileRequest();
			if (cReq != null) {
				if (cReq.getVersion() > REQUEST_HANDLER_VERSION) {
					String msg = "Request version (" + req.getVersion() + ") is > (" + REQUEST_HANDLER_VERSION + ") output may be incorrect.";
					result += msg;
					System.err.println(msg);
				}
				
				if (cReq.getVersion() > COMPILE_REQUEST_HANDLER_VERSION) {
					String msg = "Compile Request version (" + cReq.getVersion() + ") is > (" + COMPILE_REQUEST_HANDLER_VERSION + ") output may be incorrect.";
					result += msg;
					System.err.println(msg);
				}
				
				// Construct in-memory java source files from the request dynamic code
				final Iterable<? extends JavaFileObject> files = createSourceFileObjects(cReq);
			
				if (req.getTestType().equalsIgnoreCase("run")) {
					// Compile and run the source files
					success = runIt(files, cReq.getMainClass());
				}
				else if (req.getTestType().equalsIgnoreCase("junit")) {
					TestRequest tReq = req.getTestRequest();
					testResults = testIt(files, tReq.getTestClasses());
					success = testResults.getSuccess();
				}
				else {
					System.err.println("Nothing to do");
					result += "Nothing to do.";
					success = false;
				}
					
				// Retrieve the output as a string
				System.out.flush();
				System.setOut(old);
				result += boas.toString();
			}
			else {
				result = "Nothing to do";
				success = false;
			}
		}
		else {
			result = "Missing Request";
			success = false;
		}
		
		Runtime runtime = Runtime.getRuntime();
		StringBuilder sb = new StringBuilder();
		NumberFormat format = NumberFormat.getInstance();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		
		// Log memory usage
		long freeMemory = runtime.freeMemory();
		sb.append("free memory: " + format.format(freeMemory / 1024) + "\t");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\t");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "\t");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\t");
		System.err.println(sb.toString());

		CompileResponse resp = new CompileResponse();
		
		resp.setResult(result);
		resp.setTestResults(testResults);
		resp.setSucceeded(success);
		resp.setVersion(RESPONDER_VERSION);
		
		mapper.writeValue(output, resp);
	}
	
	public static void main (String args[]) {
		
	}
}
