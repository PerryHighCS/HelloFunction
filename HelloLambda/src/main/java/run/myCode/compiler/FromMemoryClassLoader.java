package run.myCode.compiler;

import java.util.HashMap;
import java.util.Map;

public class FromMemoryClassLoader extends ClassLoader {
	private Map<String, MemoryByteCode> m = new HashMap<String, MemoryByteCode>();
	private ClassLoader parent;

	public FromMemoryClassLoader(ClassLoader parent) {
		this.parent = parent;
		// System.err.println("new loader");
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			// System.out.println("Class search:" + name);
			MemoryByteCode mbc = m.get(name);
			if (mbc == null) {
				mbc = m.get(name.replace(".", "/"));
				if (mbc == null) {
					return super.findClass(name);
				}
			}
			return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
		} catch (ClassNotFoundException e) {
			// System.err.println("Could not find: " + name);
			if (this.parent != null) {
				try {
					return parent.loadClass(name);
				} catch (ClassNotFoundException e1) {
					// System.err.println("Really couldn't find " + name);
					throw e1;
				}
			} else {
				throw e;
			}
		}
	}

	public void addClass(String name, MemoryByteCode mbc) {
		// System.err.println("Added class: " + name);
		// System.out.println("Added class:" + name);
		m.put(name, mbc);
	}
}