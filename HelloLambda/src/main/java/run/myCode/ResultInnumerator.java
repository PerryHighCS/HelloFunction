package run.myCode;

import java.io.ByteArrayOutputStream;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ResultInnumerator extends RunListener {
	private TestResult testResults;
	private ByteArrayOutputStream output;
	private boolean testFailed = false;

	public ResultInnumerator(ByteArrayOutputStream baos) {
		this.output = baos;
		this.testResults = new TestResult();
	}


	/**
	 * Called before a test run is begun
	 */
	@Override
	public void testRunStarted(Description description) throws Exception {
		super.testRunStarted(description);
		testResults = new TestResult();
	}

	/**
	 * Called before a test case is begun
	 */
	@Override
	public void testStarted(Description description) throws Exception {
		super.testStarted(description);

		testFailed = false;
		output.reset();
	}

	/**
	 * Called when a test case is run but assumptions the test requires to be true are not met
	 * 
	 * @see org.junit.runner.notification.RunListener#testAssumptionFailure(org.junit.runner.notification.Failure)
	 */
	@Override
	public void testAssumptionFailure(Failure failure) {
		super.testAssumptionFailure(failure);
		
		String description = "Current execution state does not meet assumptions made by test.\n";
		description += failure.getMessage();
		
		String body = output.toString();
		body += stackTrace(failure.getException().getStackTrace(),
				failure.getDescription().getClassName() + "." + failure.getDescription().getMethodName());
		
		testResults.addTest(description, body, false);
		testFailed = true;
		
	}

	/**
	 * Called when a test case is run and fails
	 */
	@Override
	public void testFailure(Failure failure) throws Exception {
		super.testFailure(failure);
		
		String description = failure.getDescription().toString();
		
		Throwable realException = failure.getException();
		if (realException.getCause() != null) {
			realException = realException.getCause();
		}
		
		String body = output.toString();
		body += realException.getMessage();
		body += "\n-----\n";
		body += "Call Stack:\n";
		body += stackTrace(realException.getStackTrace(), 
				failure.getDescription().getClassName() + "." + failure.getDescription().getMethodName());
		
		testResults.addTest(description, body, false);
		testFailed = true;
	}

	/**
	 * Called when a test case completes, on either success or failure
	 */
	@Override
	public void testFinished(Description description) throws Exception {
		super.testFinished(description);

		if (!testFailed) {
			String body = output.toString();
			
			testResults.addTest(description.getDisplayName(), body, true);
		}
		
	}

	/**
	 * Called after all tests in a test run are complete
	 */
	@Override
	public void testRunFinished(Result result) throws Exception {
		super.testRunFinished(result);
	}

	public TestResult retrieveResults() {
		return testResults;
	}

	private String stackTrace(StackTraceElement[] frames, String stackBottom) {
		String trace = "";

		// Add the stack frames to the trace until the method "stackBottom" is reached
		for (StackTraceElement frame : frames) {
			trace += "\t";
			trace += frame.toString();
			trace += "\n";
			
			if ((frame.getClassName() + "." + frame.getMethodName()).equals(stackBottom)) {
				break;
			}
		}
		return trace;
	}
}
