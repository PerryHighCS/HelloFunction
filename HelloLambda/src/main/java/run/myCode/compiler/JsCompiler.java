package run.myCode.compiler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.tools.JavaFileObject;

/**
 *
 * @author dahlem.brian
 */
public class JsCompiler {
    private FromMemoryClassLoader classLoader;
    private boolean compiled;
    private String messages = "";
    
    public JsCompiler(Iterable<? extends JavaFileObject> files, String mainClass) {
        
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(boas);
        PrintStream old = System.out;
        
        System.setOut(ps);
        
        try {
            classLoader = JavaCodeCompiler.compile(files, null);
        }
        catch (ClassNotFoundException | NullPointerException e) {
            // Handle exceptions caused by the code being compiled
            
            // (Log and) Display the exception
            messages = "Main class: " + mainClass + " not found in source files, could not compile.";
            System.err.println(messages);
            
            compiled = false;
        }
        
        String compilerMsgs = boas.toString();
        
        if (!compilerMsgs.isEmpty()) {
            if (!messages.isEmpty()) {
                messages += "\n";
            }
            messages += compilerMsgs;
        }
        
        boas.reset();
        
        System.setOut(old);
        
        // TODO: compile the files in classLoader to javascript
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Determine if compilation succeeded
     * @return true if compilation was successful
     */
    public boolean succeeded() {
        return compiled;        
    }

    /**
     * Get the compiled files as a JSON array
     * @return A String containing the JSON representation of the compiled files
     */
    public String toJSON() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Get any compiler messages
     * @return A String containing the compiler messages
     */
    public String getMessages() {
        return messages;
    }    
}
