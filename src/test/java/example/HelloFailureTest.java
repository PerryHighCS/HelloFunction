package example;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import run.myCode.CompileResponse;
import run.myCode.Hello;

/**
 * Tests compilation failures for the Hello lambda.
 */
public class HelloFailureTest {
    private Context createContext() {
        TestContext ctx = new TestContext();

        ctx.setFunctionName("HelloFunction");

        return ctx;
    }

    @Test
    public void testCompilationFailureShowsDiagnostics() {
        CompileResponse resp = doTest("jsonDataFiles/brokenLocal.json");

        assertFalse("Compiler returned no response", resp == null);
        assertFalse("Compilation unexpectedly succeeded", resp.getSucceeded());
        assertTrue("Missing line number in diagnostics", resp.getResult().contains("Line Number"));
        assertTrue("Missing message in diagnostics", resp.getResult().contains("Message->"));

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
