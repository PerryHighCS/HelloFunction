package myCodeRun;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileObject.Kind;

@SuppressWarnings("unused")
class MemoryByteCode extends SimpleJavaFileObject {   
	private ByteArrayOutputStream baos;   
	public MemoryByteCode(String name) {       
		super(URI.create("byte:///" + name + ".class"), Kind.CLASS);   
	}   
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {       
		throw new IllegalStateException();   
	}   
	@Override
	public OutputStream openOutputStream() {       
		baos = new ByteArrayOutputStream();       
		return baos;   
	}   
	@Override
	public InputStream openInputStream() {       
		throw new IllegalStateException();   
	}   
	public byte[] getBytes() {       
		return baos.toByteArray();   
	}
}