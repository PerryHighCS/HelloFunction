package myCodeRun;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {	
	private CompileRequest compRequest;
	private String testType;
	private int version;
	private TestRequest test;
	
	public Request() {};
	
	@JsonProperty("compile")
	public void setCompileRequest(CompileRequest cr) {
		this.compRequest = cr;
	}
	
	@JsonProperty("compile")
	public CompileRequest getCompileRequest() {
		return compRequest;
	}
	
	@JsonProperty("test-type")
	public void setTestType(String tt) {
		this.testType = tt;
	}
	
	@JsonProperty("test-type")
	public String getTestType() {
		return this.testType;
	}

	@JsonProperty("test")
	public void setTestRequest(TestRequest tReq) {
		this.test = tReq;
	}
	
	@JsonProperty("test")
	public TestRequest getTestRequest() {
		return this.test;
	}

	@JsonProperty("version")
	public int getVersion() {
		return version;
	}

	@JsonProperty("version")
	public void setVersion(int version) {
		this.version = version;
	}
}
