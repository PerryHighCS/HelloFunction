//TODO: Add custom securitymanager

package run.myCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;

import org.junit.runner.JUnitCore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import run.myCode.compiler.FromMemoryClassLoader;
import run.myCode.compiler.InMemoryJavaFileObject;
import run.myCode.compiler.JavaCodeCompiler;
import zss.Tester.ZombieLandTester;

public class Hello implements RequestStreamHandler {
	public static final int REQUEST_HANDLER_VERSION = 1;
	public static final int COMPILE_REQUEST_HANDLER_VERSION = 1;
	public static final int DATA_HANDLER_VERSION = 1;
	public static final int RESPONDER_VERSION = 1;
	public static final long MAX_ZOMBIETIME = 1000000000L * 4;

	/**
	 * Compile and Run submitted java files
	 * 
	 * @param files
	 *            the list of java source files to compile
	 * @return true if compilation and running completed without exceptions
	 */
	public boolean runIt(Iterable<? extends JavaFileObject> files, String mainClass) {
		// Compile the files using the JavaCompiler
		try {
			FromMemoryClassLoader classLoader = JavaCodeCompiler.compile(files, null);

			Class<?> compiledClass = classLoader.findClass(mainClass);

			// Call main method of compiled class by reflection
			compiledClass.getMethod("main", String[].class).invoke(null, new Object[] { null });
			compiledClass = null;
			classLoader = null;

			// Handle exceptions caused by the code being compiled
		} catch (ClassNotFoundException | NullPointerException e) {
			System.err.println(e.toString());
			System.out.println("Main class: " + mainClass + " not found in source files, could not execute.");
			return false;
		} catch (NoSuchMethodException | IllegalArgumentException e) {
			System.err.println(e.toString());
			System.out.println("Main class: " + mainClass
					+ " does not contain a \"main\" method, or main method has incorrect parameter list.");
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

			// Show the call stack for the exception... but do not include any of stack
			// containing this method or below
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
	 * 
	 * @param files
	 * @return true if compilation and running completed without exceptions
	 */
	public TestResult testIt(Iterable<? extends JavaFileObject> files, List<String> tests) {
		TestResult score = new TestResult();

		FromMemoryClassLoader classLoader;
		// Compile the files using the JavaCompiler
		try {
			classLoader = JavaCodeCompiler.compile(files, null);
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
		ResultInnumerator allResults = new ResultInnumerator(baos);
		junit.addListener(allResults);

		// Have JUnit run the test cases specified
		try {
			List<Class<?>> classes = new ArrayList<Class<?>>();

			for (String test : tests) {
				classes.add(classLoader.loadClass(test));
			}

			// Result result = junit.run(classes.toArray(new Class<?>[0]));
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
			score.addFailedTest(e.toString(),
					baos.toString() + stackTrace(e.getStackTrace(), "myCodeRun.Hello.testIt"));
		}

		// Clear the output and restore the original
		System.out.flush();
		System.setOut(old);

		return score;
	}

	public ZombieResult zombieDo(String myZombieSource, List<String> scenarios) {
		ZombieResult res = new ZombieResult();

		List<zss.Tester.Result> zr = ZombieLandTester.doScenario(scenarios.toArray(new String[0]), myZombieSource,
				MAX_ZOMBIETIME);

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

	private String stackTrace(StackTraceElement[] frames, String stackBottom) {
		String trace = "";

		// Add the stack frames to the trace until the method "stackBottom" is reached
		for (StackTraceElement frame : frames) {
			if (!(frame.getClassName() + "." + frame.getMethodName()).equals(stackBottom)) {
				trace += "\t";
				trace += frame.toString();
				trace += "\n";
			} else {
				break;
			}
		}
		return trace;
	}

	/**
	 * Create a list of source files from the initial request
	 * 
	 * @param req
	 *            a RequestClass
	 * @return
	 * @throws Exception
	 */
	private List<InMemoryJavaFileObject> createSourceFileObjects(CompileRequest req) {

		List<InMemoryJavaFileObject> objects = new ArrayList<InMemoryJavaFileObject>(req.getSourceFiles().size());

		// Pull all of the source code files in the request
		for (CompileRequest.FileClass f : req.getSourceFiles()) {
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

		long startTime = System.nanoTime();

		ObjectMapper mapper = new ObjectMapper();
		LambdaAPIRequest apiReq = mapper.readValue(input, LambdaAPIRequest.class);

		// System.err.println(System.getProperty("user.dir"));
		Request req = apiReq.getReqBody();

		TestResult testResults = null;
		String result = "";
		boolean success;

		// Create a stream to hold system output
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(boas);
		PrintStream old = System.out;
		System.setOut(ps);

		POSIX posix = POSIXFactory.getJavaPOSIX();
		String workingDir = "/tmp";
		String startDir = System.getProperty("user.dir");

		// Create and move to a working directory
		// posix.mkdir(workingDir, 777);
		// posix.chdir(workingDir);
		// System.setProperty("user.dir", workingDir);

		if (req != null) {
			CompileRequest cReq = req.getCompileRequest();
			if (cReq != null) {
				if (cReq.getVersion() > REQUEST_HANDLER_VERSION) {
					String msg = "Request version (" + req.getVersion() + ") is > (" + REQUEST_HANDLER_VERSION
							+ ") output may be incorrect.";
					result += msg;
					System.err.println(msg);
				}

				if (cReq.getVersion() > COMPILE_REQUEST_HANDLER_VERSION) {
					String msg = "Compile Request version (" + cReq.getVersion() + ") is > ("
							+ COMPILE_REQUEST_HANDLER_VERSION + ") output may be incorrect.";
					result += msg;
					System.err.println(msg);
				}

				DataRequest data = req.getData();

				if (data != null) {
					if (data.getVersion() > DATA_HANDLER_VERSION) {
						String msg = "Request data version (" + data.getVersion() + ") is > (" + DATA_HANDLER_VERSION
								+ ") output may be incorrect.";
						result += msg;
						System.err.println(msg);
					}
				}

				// Construct in-memory java source files from the request dynamic code
				final Iterable<? extends JavaFileObject> files = createSourceFileObjects(cReq);

				if (req.getTestType().equalsIgnoreCase("run")) {
					saveData(data);
					// Compile and run the source files
					success = runIt(files, cReq.getMainClass());

				} else if (req.getTestType().equalsIgnoreCase("junit")) {
					saveData(data);
					TestRequest tReq = req.getTestRequest();
					testResults = testIt(files, tReq.getTestClasses());
					success = testResults.getSuccess();

				} else if (req.getTestType().equalsIgnoreCase("zombieland")) {
					// System.err.println("Num threads running: " +
					// ManagementFactory.getThreadMXBean().getThreadCount());

					// Get the myZombie source file
					String myZombieSource = "";
					for (JavaFileObject file : files) {
						if (file.getName().equals("/MyZombie.java")) {
							try {
								myZombieSource = (String) file.getCharContent(true);
								break;
							} catch (IOException e) {
							}
						} else {
							result += "UNKNOWN FILE: " + file.getName();
						}
					}

					List<String> scenarios = new ArrayList<>();

					for (DataRequest.DataFile file : data.getDataFiles()) {
						String scenario = "";
						for (String line : file.getContents()) {
							scenario += line + "\n";
						}
						scenarios.add(scenario);
					}

					// long prep = System.nanoTime() - startTime;
					// System.err.printf("Zombie prep time: %.2f\n", prep / 1.0e9);

					testResults = zombieDo(myZombieSource, scenarios);
					success = testResults.getSuccess();

					// long test = System.nanoTime() - startTime - prep;
					// System.err.printf("Zombie test time: %.2f\n", test / 1.0e9);
					// System.err.println(
					// "Num threads still running: " +
					// ManagementFactory.getThreadMXBean().getThreadCount());
				} else {
					System.err.println("Nothing to do");
					result += "Nothing to do.";
					success = false;
				}

				// Retrieve the output as a string
				System.out.flush();
				System.setOut(old);
				result += boas.toString();
			} else {
				result = "Nothing to do";
				success = false;
			}
		} else {
			result = "Missing Request";
			success = false;
		}

		// Move back to the starting dir
		posix.chdir(startDir);
		System.setProperty("user.dir", startDir);

		// Remove the working directory if created
		File dir = new File(workingDir).getAbsoluteFile();
		cleanDir(dir);

		if (dir.list().length > 0) {
			System.err.println("Working Directory not emptied.");
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
		sb.append("request duration: " + format.format((System.nanoTime() - startTime) / 1.0e9) + "sec");
		System.err.println(sb.toString());

		CompileResponse resp = new CompileResponse();

		resp.setResult(result);
		resp.setTestResults(testResults);
		resp.setSucceeded(success);
		resp.setVersion(RESPONDER_VERSION);

		mapper.writeValue(output, resp);
	}

	private void saveData(DataRequest data) {
		if (data == null) {
			return;
		}

		for (DataRequest.DataFile file : data.getDataFiles()) {

			File f = (new File(file.getName()));

			PrintWriter out = null;

			try {
				out = new PrintWriter(f.getAbsoluteFile());

				for (String text : file.getContents()) {
					out.println(text);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("Couldn't write file: " + file.getName());
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}

	private void cleanDir(File dir) {
		File[] dirfiles = dir.listFiles();

		for (File f : dirfiles) {
			if (f.isDirectory()) {
				cleanDir(f);
			}
			f.delete();
		}
	}

	public Hello() {

	}

	public static void main(String args[]) {

	}
}
