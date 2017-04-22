package example;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

import myCodeRun.Hello;
/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class HelloTest {
    private Context createContext() {
        TestContext ctx = new TestContext();

        ctx.setFunctionName("HelloFunction");

        return ctx;
    }

    @Test
    public void testHello() {
	
        Hello handler = new Hello();
        
        try {
        	for (int i = 0; i < 10; i++) {
            	InputStream input = this.getClass().getClassLoader().getResourceAsStream("mergeSortBigLocal.json");
            	handler.handleRequest(input, System.out, createContext());
            	input.close();
        	}
        	System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        
    }
}
