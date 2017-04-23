package myCodeRun;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MyJUnit {
	private List<RunListener> listeners;

	public MyJUnit() {
		listeners = new ArrayList<RunListener>();
	}
	
	public void addListener(RunListener listener) {
		listeners.add(listener);
	}

	public Result run(Class<?>[] tests) {
		Result r = new Result();
		
		List<RunListener> testListeners = new ArrayList<RunListener>();
		testListeners.addAll(listeners);
		testListeners.add(r.createListener());
		
		for (Class<?> test : tests) {
			Description runDesc = Description.createSuiteDescription(test);
			
			for (RunListener listener: testListeners) {
				try {
					listener.testRunStarted(runDesc);
				}
				catch (Exception b) {
				}
			}
			
			for (Method method : test.getDeclaredMethods()) {
				if (method.getName().indexOf("test") == 0) {
					Description testDesc = Description.createTestDescription(test, method.getName());
					
					for (RunListener listener: testListeners) {
						try {
							listener.testStarted(testDesc);
						}
						catch (Exception b) {
						}
					}
								
					try {
						method.invoke(null);
					}
					catch (Exception e){
						Failure f = new Failure(testDesc, e);
						
						for (RunListener listener: testListeners) {
							try {
								listener.testFailure(f);
							}
							catch (Exception b) {
							}
						}
					}
										
					for (RunListener listener: testListeners) {
						try {
							listener.testFinished(testDesc);
						}
						catch (Exception b) {
						}
					}
				}
	        }
		}
		return r;
	}
}
