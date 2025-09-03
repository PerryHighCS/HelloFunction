package run.myCode.compiler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.tools.SimpleJavaFileObject;

import javax.tools.JavaFileObject.Kind;

/**
 * java File Object represents an in-memory java source file so there is no need
 * to put the source file on hard disk
 */
public class InMemoryJavaFileObject extends SimpleJavaFileObject {

    private static final boolean DEBUG = Boolean.getBoolean("run.mycode.debug");

    private String contents = null;

    /**
     * Create a Java file object in memory with a name and text contents.
     *
     * @param fileName the name of the file, with extension
     * @param contents the contents of the file as a single string object
     */
    public InMemoryJavaFileObject(String fileName, String contents) {
        // Use a custom URI scheme so the Eclipse compiler does not resolve the
        // file on disk.  The URI's path is the provided filename which keeps
        // the compiler from looking for a physical file like "/MyClass.java".
        //
        // Using a non-file scheme is important because ECJ will otherwise try
        // to open the path returned by getName() from the filesystem which
        // causes "File ... is missing" errors when compiling in memory.
        super(URI.create("mem:///" + fileName), Kind.SOURCE);

        // Save the file's contents
        this.contents = contents;

        if (DEBUG) {
            System.out.println("Created in-memory source " + toUri());
        }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return contents;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        if (DEBUG) {
            System.out.println("Opening in-memory source " + getName());
        }
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        String path = toUri().getPath();
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    @Override
    public String toString() {
        String s = this.getName() + ":\n";
        s += this.contents;

        return s;
    }
}
