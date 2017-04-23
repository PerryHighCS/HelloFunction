package myCodeRun;

import java.util.Map;
import java.util.WeakHashMap;

class SpecialClassLoader extends ClassLoader {   
	private Map<String,MemoryByteCode> m = new WeakHashMap<String, MemoryByteCode>();
	private ClassLoader parent;
	
	public SpecialClassLoader(ClassLoader parent) {
		this.parent = parent;
	}
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			//System.out.println("Class search:" + name);
			MemoryByteCode mbc = m.get(name);       
			if (mbc==null){           
				mbc = m.get(name.replace(".","/"));           
				if (mbc==null){               
					return super.findClass(name);           
				}       
			}       
			return defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
		}
		catch (ClassNotFoundException e) {
			if (this.parent != null) {
				return parent.loadClass(name);
			}
			else {
				throw e;
			}
		}
	}

	public void addClass(String name, MemoryByteCode mbc) { 
		//System.out.println("Added class:" + name);
		m.put(name, mbc);   
	}
}