package run.myCode;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HTTPResponse {
	private String body;
	private String statusCode;
	private Map<String, String> headers;
	
	public HTTPResponse(String statusCode, Map<String, String> headers, String body) {
		this.body = body;
		this.statusCode = statusCode;
		this.headers = headers;
	}
	
	public HTTPResponse() {}
	
	@JsonProperty("body")
	public String getBody() {
		return body;
	}

	@JsonProperty("body")
	public void setBody(String body) {
		this.body = body;
	}

	@JsonProperty("statusCode")
	public String getStatusCode() {
		return statusCode;
	}

	@JsonProperty("statusCode")
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	@JsonProperty("header")
	public Map<String, String> getHeaders() {
		return headers;
	}

	@JsonProperty("header")
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

}
