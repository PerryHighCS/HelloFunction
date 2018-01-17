package run.myCode;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestResult {
	public static class CaseResult {
		public String description;
		public String body;
		public boolean passed;

		public CaseResult() {
		}

		@JsonProperty("body")
		public String getBody() {
			return body;
		}

		@JsonProperty("body")
		public void setBody(String body) {
			this.body = body;
		}

		@JsonProperty("passed")
		public boolean isPassed() {
			return passed;
		}

		@JsonProperty("passed")
		public void setPassed(boolean passed) {
			this.passed = passed;
		}

		@JsonProperty("description")
		public String getDescription() {
			return description;
		}

		@JsonProperty("description")
		public void setDescription(String description) {
			this.description = description;
		}
	}

	private int numTestsRun;
	private int numPassed;
	private int numFailed;

	private List<CaseResult> results;

	public TestResult() {
		// Record the statics on tests run : tests passed
		this.numTestsRun = 0;
		this.numPassed = 0;
		this.numFailed = 0;

		results = new ArrayList<CaseResult>();
	}

	public void addFailedTest(String description, String output) {
		this.numTestsRun++;
		this.numFailed++;

		addTest(description, output, false);
	}

	public void addPassedTest(String output) {
		this.numTestsRun++;
		this.numPassed++;

		addTest("Test Passed", output, true);
	}

	@JsonProperty("score")
	public double getScore() {
		if (numTestsRun == 0) {
			return 0;
		}

		return (double) numPassed / numTestsRun;
	}

	@JsonProperty("success")
	public boolean getSuccess() {
		return (numTestsRun > 0) && (numPassed == numTestsRun);
	}

	@JsonProperty("numTests")
	public int getNumTestsRun() {
		return numTestsRun;
	}

	@JsonProperty("numPassed")
	public int getNumPassed() {
		return numPassed;
	}

	@JsonProperty("numFailed")
	public int getNumFailed() {
		return numFailed;
	}

	public void addTest(String description, String body, boolean success) {
		CaseResult test = new CaseResult();
		test.description = description;
		test.body = body;
		test.passed = success;

		numTestsRun++;
		if (success) {
			numPassed++;
		} else {
			numFailed++;
		}

		results.add(test);
	}

	public void addCase(CaseResult testCase) {
		results.add(testCase);

		numTestsRun++;
		if (testCase.isPassed()) {
			numPassed++;
		} else {
			numFailed++;
		}
	}

	@JsonProperty("details")
	public List<CaseResult> getDetails() {
		return results;
	}

	public void addResults(TestResult other) {
		results.addAll(other.results);

		this.numPassed += other.numPassed;
		this.numFailed += other.numFailed;
		this.numTestsRun += other.numTestsRun;
	}
}
