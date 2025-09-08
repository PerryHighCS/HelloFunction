package run.myCode.compiler;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.Test;

import javax.tools.JavaFileObject;

public class JavaCodeCompilerTest {

    @Test
    public void compileInvalidSourcePrintsDiagnostics() {
        String sourceCode = "public class Broken { public static void main(String[] args) { System.out.println(\"hi\") } }";
        JavaFileObject source = new InMemoryJavaFileObject("Broken.java", sourceCode);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            ClassNotFoundException ex = assertThrows(ClassNotFoundException.class,
                () -> JavaCodeCompiler.compile(List.of(source), null));
            assertEquals("Compilation failed", ex.getMessage());
        } finally {
            System.setOut(originalOut);
        }

        String output = baos.toString();
        assertTrue(output.contains("Line Number"));
        assertTrue(output.contains("Message->"));
    }
}
