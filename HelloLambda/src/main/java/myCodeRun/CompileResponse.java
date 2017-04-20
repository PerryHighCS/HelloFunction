package myCodeRun;

public class CompileResponse {
	private String result;
	private int version;
	private boolean succeeded;
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public int getVersion() {
		return this.version;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void setSucceeded(boolean success) {
		this.succeeded = success;
	}
	
	public boolean getSucceeded() {
		return this.succeeded;
	}
	
	public CompileResponse(String result, boolean success, int responderVersion) {
		this.result = result;
		this.version = responderVersion;
		this.succeeded = success;
	}
	
	public CompileResponse() {}
}
