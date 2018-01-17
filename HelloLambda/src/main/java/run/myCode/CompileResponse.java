package run.myCode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class CompileResponse {
	private String result;
	private TestResult testResults;
	private int version;
	private boolean succeeded;

	@JsonProperty("version")
	public void setVersion(int version) {
		this.version = version;
	}

	@JsonProperty("version")
	public int getVersion() {
		return this.version;
	}

	@JsonProperty("result")
	public String getResult() {
		return result;
	}

	@JsonProperty("testResults")
	public TestResult getTestResults() {
		return testResults;
	}

	@JsonProperty("testResults")
	public void setTestResults(TestResult testResults) {
		this.testResults = testResults;
	}

	@JsonProperty("result")
	public void setResult(String result) {
		this.result = result;
	}

	@JsonProperty("succeeded")
	public void setSucceeded(boolean success) {
		this.succeeded = success;
	}

	@JsonProperty("succeeded")
	public boolean getSucceeded() {
		return this.succeeded;
	}

	public CompileResponse(String result, boolean success, int responderVersion) {
		this.result = result;
		this.version = responderVersion;
		this.succeeded = success;
	}

	public CompileResponse() {
	}
}
