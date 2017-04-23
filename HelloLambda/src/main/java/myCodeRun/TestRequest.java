package myCodeRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * A class that describes an API Request to run JUnit tests on source code
 */
public class TestRequest {		
	private int version = 1;
	private List<String> testClasses;
	
	/*
	 * Set the version - identifying the format of this request
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/*
	 * Retrieve the version of this request's format
	 */
	public int getVersion() {
		return this.version;
	}
	
	/*
	 * Specify the names of the classes containing JUnit tests
	 * - Names must be fully qualified with package as appropriate
	 */
	public void setTestClasses(String[] classes) {
		testClasses = new ArrayList<String>(Arrays.asList(classes));
	}
	
	/*
	 * Retrieve the list of test cases contained in this request
	 */
	public List<String> getTestClasses() {
		return testClasses;
	}
	
	/*
	 * Add the name of a class containing test cases
	 */
	public void addTestClass(String name) {
		if (testClasses == null) {
			testClasses = new ArrayList<String>();
		}
		
		testClasses.add(name);
	}
		
	public TestRequest(){}
}
