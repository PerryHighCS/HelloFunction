package zss.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * Collects diagnostics reported during compilation so that callers can display
 * meaningful error messages.
 */
public class CompileDiagnosticListener implements DiagnosticListener<JavaFileObject> {

    private final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<>();

    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        diagnostics.add(diagnostic);
    }

    /**
     * Retrieve all diagnostics that were reported during compilation.
     *
     * @return a list of diagnostics
     */
    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
        return diagnostics;
    }

    /**
     * Format collected diagnostics into human readable messages that include the
     * source line and a caret pointing to the error column when available.
     *
     * @return list of formatted diagnostic strings
     */
    public List<String> getFormattedDiagnostics() {
        List<String> messages = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            messages.add(formatDiagnostic(d));
        }
        return messages;
    }

    private String formatDiagnostic(Diagnostic<? extends JavaFileObject> d) {
        StringBuilder sb = new StringBuilder();
        String sourceName = d.getSource() == null ? "Unknown Source"
                : new File(d.getSource().getName()).getName();

        boolean appendedSource = false;
        try {
            if (d.getSource() != null) {
                CharSequence content = d.getSource().getCharContent(true);
                String[] lines = content.toString().split("\r?\n");
                long lineNo = d.getLineNumber();
                if (lineNo > 0 && lineNo <= lines.length) {
                    String line = lines[(int) lineNo - 1];
                    sb.append(line).append(System.lineSeparator());
                    long col = d.getColumnNumber();
                    if (col > 0) {
                        for (int i = 1; i < col; i++) {
                            sb.append(' ');
                        }
                        sb.append('^');
                    }
                    sb.append(System.lineSeparator());
                    appendedSource = true;
                }
            }
        } catch (IOException e) {
            // ignore - if we can't read the source, just return the basic message
        }

        if (!appendedSource && sb.length() > 0) {
            sb.append(System.lineSeparator());
        }

        sb.append(sourceName).append(':').append(d.getLineNumber()).append(':')
                .append(' ').append(d.getKind().toString().toLowerCase(Locale.ENGLISH))
                .append(':').append(' ').append(d.getMessage(Locale.ENGLISH));

        return sb.toString();
    }
}
