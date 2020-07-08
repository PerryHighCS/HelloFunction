//TODO: Add custom securitymanager
package run.myCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import static run.myCode.FileManager.*;
import run.myCode.compiler.CodeRunner;
import run.myCode.compiler.SimpleFile;

public class Hello implements RequestStreamHandler {

    public static final int REQUEST_HANDLER_VERSION = 1;
    public static final int COMPILE_REQUEST_HANDLER_VERSION = 1;
    public static final int DATA_HANDLER_VERSION = 1;
    public static final int RESPONDER_VERSION = 1;
    public static final long MAX_ZOMBIETIME = 1000000000L * 4;

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {

        long startTime = System.nanoTime();

        CodeRunner runner = new CodeRunner();

        ObjectMapper mapper = new ObjectMapper();
        LambdaAPIRequest apiReq = mapper.readValue(input, LambdaAPIRequest.class);

        // System.err.println(System.getProperty("user.dir"));
        Request req = apiReq.getReqBody();

        TestResult testResults = null;
        String result = "";
        boolean success;

        // Create a stream to hold system output for reporting back with the result
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(boas);
        PrintStream old = System.out;
        InputStream in = System.in;

        System.setOut(ps);
        System.setIn(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)));

        // Set the working directory for data files
        POSIX posix = POSIXFactory.getJavaPOSIX();
        String workingDir = System.getProperty("java.io.tmpdir"); //"/tmp";
        String startDir = System.getProperty("user.dir");

        // Create and move to a working directory
        // posix.mkdir(workingDir, 777);
        // posix.chdir(workingDir);
        // System.setProperty("user.dir", workingDir);
        // Finally... handle the request
        if (req != null) {
            CompileRequest cReq = req.getCompileRequest();
            if (cReq != null) {
                if (cReq.getVersion() > REQUEST_HANDLER_VERSION) {
                    String msg = "Request version (" + req.getVersion() + ") is > (" + REQUEST_HANDLER_VERSION
                            + ") output may be incorrect.";
                    result += msg;
                    System.err.println(msg);
                }

                if (cReq.getVersion() > COMPILE_REQUEST_HANDLER_VERSION) {
                    String msg = "Compile Request version (" + cReq.getVersion() + ") is > ("
                            + COMPILE_REQUEST_HANDLER_VERSION + ") output may be incorrect.";
                    result += msg;
                    System.err.println(msg);
                }

                DataRequest data = req.getData();

                if (data != null) {
                    if (data.getVersion() > DATA_HANDLER_VERSION) {
                        String msg = "Request data version (" + data.getVersion() + ") is > (" + DATA_HANDLER_VERSION
                                + ") output may be incorrect.";
                        result += msg;
                        System.err.println(msg);
                    }
                }

                // Construct in-memory java source files from the request dynamic code
                final Iterable<? extends JavaFileObject> files = createSourceFileObjects(cReq);

                // Execute the appropriate compile/run request
                if (req.getTestType().equalsIgnoreCase("run")) {
                    saveData(data);
                    // Compile and run the source files
                    success = runner.runIt(files, cReq.getMainClass());

                } else if (req.getTestType().equalsIgnoreCase("junit")) {
                    saveData(data);
                    TestRequest tReq = req.getTestRequest();

                    // Compile and test the source files
                    testResults = runner.testIt(files, tReq.getTestClasses());
                    success = testResults.getSuccess();

                } else if (req.getTestType().equalsIgnoreCase("zombieland")
                        || req.getTestType().equalsIgnoreCase("zscript")
                        || req.getTestType().equalsIgnoreCase("ultrazscript")) {
                    // System.err.println("Num threads running: " +
                    // ManagementFactory.getThreadMXBean().getThreadCount());

                    boolean allowUltraZombie =
                            (req.getTestType().equalsIgnoreCase("zombieland") ||
                            req.getTestType().equalsIgnoreCase("ultrazscript"));

                    // Get the myZombie source file
                    String myZombieSource = "";
                    for (JavaFileObject file : files) {
                        if (file.getName().equals("/MyZombie.java")) {
                            try {
                                myZombieSource = (String) file.getCharContent(true);
                                break;
                            } catch (IOException e) {
                            }
                        } else {
                            result += "UNKNOWN FILE: " + file.getName();
                        }
                    }

                    List<SimpleFile> scenarios = new ArrayList<>();

                    // If there are data files they will be scenarios to test
                    if (data != null) {
                        data.getDataFiles().forEach(file -> {
                            String scenarioData = "";
                            scenarioData = file.getContents().stream()
                                    .map(line -> line + "\n")
                                    .reduce(scenarioData, String::concat);
                            scenarios.add(new SimpleFile(file.getName(), scenarioData));
                        });
                    }

                    // long prep = System.nanoTime() - startTime;
                    // System.err.printf("Zombie prep time: %.2f\n", prep / 1.0e9);
                    // Compile and test MyZombie.java in all the scenarios
                    testResults = runner.zombieDo(myZombieSource, scenarios,
                            allowUltraZombie);
                    success = testResults.getSuccess();

                    // long test = System.nanoTime() - startTime - prep;
                    // System.err.printf("Zombie test time: %.2f\n", test / 1.0e9);
                    // System.err.println(
                    // "Num threads still running: " +
                    // ManagementFactory.getThreadMXBean().getThreadCount());
                } else {
                    System.err.println("Nothing to do");
                    result += "Nothing to do.";
                    success = false;
                }

                // Retrieve the output as a string
                System.out.flush();
                System.setOut(old);
                result += boas.toString();
            } else {
                result = "Nothing to do";
                success = false;
            }
        } else {
            result = "Missing Request";
            success = false;
        }

        System.setIn(in);

        // Move back to the starting dir
        posix.chdir(startDir);
        System.setProperty("user.dir", startDir);

        // Remove the working directory if created
        File dir = new File(workingDir).getAbsoluteFile();
        cleanDir(dir);

        if (dir.list().length > 0) {
            System.err.println("Working Directory not emptied.");
        }

        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        NumberFormat format = NumberFormat.getInstance();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();

        // Log memory usage
        long freeMemory = runtime.freeMemory();
        sb.append("free memory: ").append(format.format(freeMemory / 1024)).append("\t");
        sb.append("allocated memory: ").append(format.format(allocatedMemory / 1024)).append("\t");
        sb.append("max memory: ").append(format.format(maxMemory / 1024)).append("\t");
        sb.append("total free memory: ").append(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024)).append("\t");
        sb.append("request duration: ").append(format.format((System.nanoTime() - startTime) / 1.0e9)).append("sec");
        System.err.println(sb.toString());

        CompileResponse resp = new CompileResponse();

        resp.setResult(result);
        resp.setTestResults(testResults);
        resp.setSucceeded(success);
        resp.setVersion(RESPONDER_VERSION);

        mapper.writeValue(output, resp);
    }

    public Hello() {

    }

    public static void main(String args[]) {

    }
}
