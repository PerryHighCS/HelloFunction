//TODO: Add custom securitymanager
package run.myCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

import run.myCode.compiler.CodeRunner;
import static run.myCode.FileManager.*;
import run.myCode.compiler.SimpleFile;

public class HelloLocal extends Hello {
    @Override
    public void handleRequest(InputStream input, OutputStream output, 
            Context context) throws IOException {

        long startTime = System.nanoTime();
        
        CodeRunner runner = new CodeRunner();
        
        ObjectMapper mapper = new ObjectMapper();
        Request req = null;
        
        try {
            req = mapper.readValue(input, Request.class);
        }
        catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        
        TestResult testResults = null;
        String result = "";
        boolean success;

        POSIX posix = POSIXFactory.getJavaPOSIX();
        String workingDir = System.getProperty("java.io.tmpdir");
        
        if (!workingDir.endsWith(System.getProperty("file.separator")))
            workingDir += System.getProperty("file.separator");
        
        workingDir += "HelloLocal" + (int)(Math.random() * Integer.MAX_VALUE);
        String startDir = System.getProperty("user.dir");

        // Create and move to a working directory
        posix.mkdir(workingDir, 777);
        posix.chdir(workingDir);
        System.setProperty("user.dir", workingDir);
        
        if (req != null) {
            CompileRequest cReq = req.getCompileRequest();
            if (cReq != null) {
                if (cReq.getVersion() > REQUEST_HANDLER_VERSION) {
                    String msg = "Request version (" + req.getVersion() + 
                            ") is > (" + REQUEST_HANDLER_VERSION +
                            ") output may be incorrect.";
                    result += msg;
                    System.out.println(msg);
                }

                if (cReq.getVersion() > COMPILE_REQUEST_HANDLER_VERSION) {
                    String msg = "Compile Request version (" + 
                            cReq.getVersion() + ") is > (" + 
                            COMPILE_REQUEST_HANDLER_VERSION + 
                            ") output may be incorrect.";
                    result += msg;
                    System.out.println(msg);
                }

                DataRequest data = req.getData();

                if (data != null) {
                    if (data.getVersion() > DATA_HANDLER_VERSION) {
                        String msg = "Request data version (" + 
                                data.getVersion() + ") is > (" + 
                                DATA_HANDLER_VERSION +
                                ") output may be incorrect.";
                        result += msg;
                        System.out.println(msg);
                    }
                }

                // Construct in-memory java source files from the request dynamic code
                final Iterable<? extends JavaFileObject> files = 
                        createSourceFileObjects(cReq);

                if (req.getTestType().equalsIgnoreCase("run")) {
                    saveData(data);
                    // Compile and run the source files
                    success = runner.runIt(files, cReq.getMainClass());
                }
                else if (req.getTestType().equalsIgnoreCase("junit")) {
                    saveData(data);
                    TestRequest tReq = req.getTestRequest();
                    testResults = runner.testIt(files, tReq.getTestClasses());
                    success = testResults.getSuccess();

                }
                else if (req.getTestType().equalsIgnoreCase("zombieland")) {
                    // System.err.println("Num threads running: " +
                    // ManagementFactory.getThreadMXBean().getThreadCount());

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
                    
                    boolean allowUltraZombie = 
                            (req.getTestType().equalsIgnoreCase("zombieland") ||
                            req.getTestType().equalsIgnoreCase("ultrazscript"));
                    
                    // long prep = System.nanoTime() - startTime;
                    // System.err.printf("Zombie prep time: %.2f\n", prep / 1.0e9);
                    testResults = runner.zombieDo(myZombieSource, scenarios,
                            allowUltraZombie);
                    success = testResults.getSuccess();

                    // long test = System.nanoTime() - startTime - prep;
                    // System.err.printf("Zombie test time: %.2f\n", test / 1.0e9);
                    // System.err.println(
                    // "Num threads still running: " +
                    // ManagementFactory.getThreadMXBean().getThreadCount());
                } else {
                    System.out.println("Nothing to do");
                    result += "Nothing to do.";
                    success = false;
                }
            } else {
                result = "Nothing to do";
                success = false;
            }
        } else {
            result = "Missing Request";
            success = false;
        }

        // Move back to the starting dir
        posix.chdir(startDir);
        System.setProperty("user.dir", startDir);

        // Remove the working directory if created
        File dir = new File(workingDir).getAbsoluteFile();
        cleanDir(dir);

        if (dir.list().length > 0) {
            System.out.println("Working Directory " + workingDir + " not emptied.");
        }
        else {
            dir.delete();
        }

        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        NumberFormat format = NumberFormat.getInstance();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();

        // Log memory usage
//        long freeMemory = runtime.freeMemory();
//        sb.append("free memory: ").append(format.format(freeMemory / 1024)).append("\t");
//        sb.append("allocated memory: ").append(format.format(allocatedMemory / 1024)).append("\t");
//        sb.append("max memory: ").append(format.format(maxMemory / 1024)).append("\t");
//        sb.append("total free memory: ").append(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024)).append("\t");
//        sb.append("request duration: ").append(format.format((System.nanoTime() - startTime) / 1.0e9)).append("sec");
//        System.out.println(sb.toString());

        if (output != null) {
            CompileResponse resp = new CompileResponse();

            resp.setResult(result);
            resp.setTestResults(testResults);
            resp.setSucceeded(success);
            resp.setVersion(RESPONDER_VERSION);

            mapper.writeValue(output, resp);
        }
    }

    public HelloLocal() {

    }

    public static void main(String args[]) {

    }
}
