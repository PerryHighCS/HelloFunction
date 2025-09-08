package zss.compiler;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.junit.Test;

import javax.tools.JavaFileObject;

public class MemoryCompilerTest {

    @Test
    public void compileInvalidSourcePrintsDiagnostics() throws Exception {
        String sourceCode = "public class Broken { public static void main(String[] args) { System.out.println(\"hi\") } }";
        JavaFileObject source = new InMemoryJavaFileObject("Broken.java", sourceCode);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            URLClassLoader parent = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
            FromMemoryClassLoader loader = MemoryCompiler.compile(List.of(source), parent);
            assertNull(loader);
        } finally {
            System.setOut(originalOut);
        }

        String output = baos.toString();
        assertTrue(output.contains("Line Number"));
        assertTrue(output.contains("Message->"));
    }
}
