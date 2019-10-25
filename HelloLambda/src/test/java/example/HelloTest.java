package example;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import run.myCode.CompileResponse;

import run.myCode.Hello;
import run.myCode.TestResult;
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
        
        System.out.println("Working Directory: " + System.getProperty("java.io.tmpdir"));
        ObjectMapper mapper = new ObjectMapper();
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        CompileResponse resp = null;
        
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("jsonDataFiles/simpleLocal.json")) {
            handler.handleRequest(input, outContent, createContext());
            resp = mapper.readValue(outContent.toString(), CompileResponse.class);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    	
        assertFalse("Compiler returned no response", resp == null);
        assertTrue("Compilation failed.\n" + resp.getResult(), resp.getSucceeded());

        System.out.println("Result: " + resp.getResult());
    }
}
