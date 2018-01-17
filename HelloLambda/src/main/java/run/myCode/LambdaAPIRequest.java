package run.myCode;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class LambdaAPIRequest {
	private HashMap<String, HashMap<String, String>> params;
	private HashMap<String, String> stageVars;
	private HashMap<String, String> context;
	private Request reqBody;

	
	@JsonProperty("params")
	public HashMap<String, HashMap<String, String>> getParams() {
		return params;
	}

	@JsonProperty("params")
	public void setParams(HashMap<String, HashMap<String, String>> params) {
		this.params = params;
	}

	@JsonProperty("stage-variables")
	public HashMap<String, String> getStageVars() {
		return stageVars;
	}

	@JsonProperty("stage-variables")
	public void setStageVars(HashMap<String, String> stageVars) {
		this.stageVars = stageVars;
	}

	@JsonProperty("context")
	public HashMap<String, String> getContext() {
		return context;
	}
	
	@JsonProperty("context")
	public void setContext(HashMap<String, String> context) {
		this.context = context;
	}

	@JsonProperty("body")
	public Request getReqBody() {
		return reqBody;
	}

	@JsonProperty("body")
	public void setReqBody(Request reqBody) {
		this.reqBody = reqBody;
	}

}
