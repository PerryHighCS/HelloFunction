package zss.compiler;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class CompileDiagnosticListener implements DiagnosticListener<JavaFileObject> {

	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {

		// System.err.println("Line Number->" + diagnostic.getLineNumber());
		// System.err.println("code->" + diagnostic.getCode());
		// System.err.println("Message->" + diagnostic.getMessage(Locale.ENGLISH));
		// System.err.println("Source->" + diagnostic.getSource());
		// System.err.println(" ");

	}
}
