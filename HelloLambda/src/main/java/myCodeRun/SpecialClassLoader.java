package myCodeRun;

import java.util.Map;
import java.util.WeakHashMap;

class SpecialClassLoader extends ClassLoader {   
	private Map<String,MemoryByteCode> m = new WeakHashMap<String, MemoryByteCode>();

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
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

	public void addClass(String name, MemoryByteCode mbc) { 
		//System.out.println("Added class:" + name);
		m.put(name, mbc);   
	}
}