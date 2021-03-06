package zss.compiler;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

/**
 * A manager that handles a list of in memory files for a classloader
 * 
 * @author bdahl
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class InMemoryJavaFileManager extends ForwardingJavaFileManager {
	private final FromMemoryClassLoader xcl;

	public InMemoryJavaFileManager(StandardJavaFileManager sjfm, FromMemoryClassLoader xcl) {
		super(sjfm);
		this.xcl = xcl;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind,
			FileObject sibling) throws IOException {
		MemoryByteCode mbc = new MemoryByteCode(name);
		xcl.addClass(name, mbc);
		return mbc;
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		return xcl;
	}
}
