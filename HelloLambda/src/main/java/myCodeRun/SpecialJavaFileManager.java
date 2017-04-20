package myCodeRun;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileManager.Location;

@SuppressWarnings({ "unused", "rawtypes" })
public class SpecialJavaFileManager extends ForwardingJavaFileManager {   
	private SpecialClassLoader xcl;   
	
	@SuppressWarnings("unchecked")
	public SpecialJavaFileManager(StandardJavaFileManager sjfm, SpecialClassLoader xcl) {       
		super(sjfm);       
		this.xcl = xcl;   
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {       
		MemoryByteCode mbc = new MemoryByteCode(name);       
		xcl.addClass(name, mbc);       
		return mbc;   
	}

	@Override
	public ClassLoader getClassLoader(Location location) {       
		return xcl;   
	}
}