package zss.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * A classloader that handles classes compiled into memory in addition to on
 * disk
 */
public class FromMemoryClassLoader extends ClassLoader {

	private final Map<String, MemoryByteCode> m;
	private final ClassLoader parent;

	public FromMemoryClassLoader(ClassLoader parent) {
		this.m = new HashMap<>();
		this.parent = parent;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
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

	void addClass(String name, MemoryByteCode mbc) {
		m.put(name, mbc);
	}
}
