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

    private String contents = null;

    /**
     * Create a Java file object in memory with a name and text contents.
     *
     * @param fileName the name of the file, with extension
     * @param contents the contents of the file as a single string object
     */
    public InMemoryJavaFileObject(String fileName, String contents) {
        // Create a file object with a classname instead of a filename by
        // removing the file's extension and convert the . separators into slashes
        super(URI.create("string:///" + fileName), Kind.SOURCE);

        // Save the file's contents
        this.contents = contents;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return contents;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        String s = this.getName() + ":\n";
        s += this.contents;

        return s;
    }
}
