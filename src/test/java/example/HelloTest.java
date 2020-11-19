package example;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import run.myCode.CompileResponse;

import run.myCode.Hello;
import run.myCode.ZombieResult;

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
        CompileResponse resp = doTest("jsonDataFiles/simpleLocal.json");
    	
        assertFalse("Compiler returned no response", resp == null);
        assertTrue("Compilation failed.\n" + resp.getResult(), resp.getSucceeded());

        System.out.println("Result: " + resp.getResult());
    }
    
    @Test
    public void testHello2() {
        CompileResponse resp = doTest("jsonDataFiles/twoClassesLocal.json");
    	
        assertFalse("Compiler returned no response", resp == null);
        assertTrue("Compilation failed.\n" + resp.getResult(), resp.getSucceeded());

        System.out.println("Result: " + resp.getResult());
    }
    
    @Test
    public void testHelloMS() {
        CompileResponse resp = doTest("jsonDataFiles/mergeSortLocal.json");
    	
        assertFalse("Compiler returned no response", resp == null);
        assertTrue("Compilation failed.\n" + resp.getResult(), resp.getSucceeded());

        System.out.println("Result: " + resp.getResult());
    }
    
    @Test
    public void testHelloZL() {
        CompileResponse resp = doTest("jsonDataFiles/zombieLandLocal.json");
    	
        assertFalse("Compiler returned no response", resp == null);
        assertTrue("Compilation failed.\n" + resp.getResult(), resp.getSucceeded());
        assertTrue("Compilation returned wrong test type.", resp.getTestResults() instanceof ZombieResult);
        
        System.out.println("Result: " + resp.getResult());
    }
    
    private CompileResponse doTest(String resourceName) {
        Hello handler = new Hello();
        
        System.out.println("Working Directory: " + System.getProperty("java.io.tmpdir"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        CompileResponse resp = null;
        
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
            handler.handleRequest(input, outContent, createContext());
            resp = mapper.readValue(outContent.toString(), CompileResponse.class);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
        
        return resp;
    }
}
