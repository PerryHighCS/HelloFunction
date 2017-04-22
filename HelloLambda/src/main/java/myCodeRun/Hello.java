package myCodeRun;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

@SuppressWarnings("unused")
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
		final SpecialClassLoader classLoader = new SpecialClassLoader();   
		//get system compiler:
		final JavaCompiler compiler = new EclipseCompiler();

		// create a diagnostic listener for compilation diagnostic message processing on compilation WARNING/ERROR
		final MyDiagnosticListener diag = new MyDiagnosticListener();
		final StandardJavaFileManager stdfileManager = compiler.getStandardFileManager(diag,
				Locale.ENGLISH,
				null);
        
		SpecialJavaFileManager fileManager = new SpecialJavaFileManager(stdfileManager, classLoader);           

		//specify options for compiler
		// Iterable<String> options = Arrays.asList(null);
		Iterable<String> options = Arrays.asList("-source", "1.8");
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
	 * @param files
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
			System.gc();
			
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
			
			System.err.println(cause.toString());
			System.out.println(cause.toString());
			System.out.println("Call Stack:");
			
			boolean reachedRunIt = false;
			for (int i = 0; i < frames.length && !reachedRunIt; i++) {
				if (frames[i].getMethodName() != "runIt") {
					System.out.print("\t");
					System.out.println(frames[i].toString());
				}
				else {
					reachedRunIt = true;
				}
			}
			return false;
		} catch (OutOfMemoryError | SecurityException | ExceptionInInitializerError e) {
			System.gc();
			
			// Display the exception and call stack
			StackTraceElement[] frames = e.getStackTrace();
			
			System.err.println(e.toString());
			System.out.println(e.toString());
			System.out.println("Call Stack:");
			
			boolean reachedRunIt = false;
			for (int i = 0; i < frames.length && !reachedRunIt; i++) {
				if (frames[i].getMethodName() != "runIt") {
					System.out.print("\t");
					System.out.println(frames[i].toString());
				}
				else {
					reachedRunIt = true;
				}
			}
			return false;
		}

		return true;
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
			
				// Compile and run the source files
				success = runIt(files, cReq.getMainClass());
				//System.gc();
				//System.runFinalization ();
				
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
		
		long freeMemory = runtime.freeMemory();
		sb.append("free memory: " + format.format(freeMemory / 1024) + "\t");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\t");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "\t");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\t");
		System.err.println(sb.toString());

		CompileResponse resp = new CompileResponse();
		
		resp.setResult(result);
		resp.setSucceeded(success);
		resp.setVersion(RESPONDER_VERSION);
		
		mapper.writeValue(output, resp);
	}
	
	public static void main (String args[]) {
		
	}

}
