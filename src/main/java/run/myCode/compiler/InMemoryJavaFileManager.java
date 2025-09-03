package run.myCode.compiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileManager.Location;

@SuppressWarnings({"unused", "rawtypes"})
public class InMemoryJavaFileManager extends ForwardingJavaFileManager {

    private final FromMemoryClassLoader xcl;
    private final Map<String, JavaFileObject> sources = new HashMap<>();
    private final Map<String, JavaFileObject> sourcePaths = new HashMap<>();

    @SuppressWarnings("unchecked")
    public InMemoryJavaFileManager(StandardJavaFileManager sjfm, FromMemoryClassLoader xcl) {
        super(sjfm);
        this.xcl = xcl;
    }

    public void addSource(JavaFileObject file) {
        // Store by binary name derived from file name
        String path = file.getName();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        sourcePaths.put(path, file);
        if (path.endsWith(".java")) {
            String className = path.substring(0, path.length() - 5);
            sources.put(className, file);
        }
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        String name = className;
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        JavaFileObject file = sources.get(name);
        if (file != null) {
            return file;
        }
        return super.getJavaFileForInput(location, className, kind);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        String path = packageName == null || packageName.isEmpty() ? relativeName : packageName + "/" + relativeName;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        JavaFileObject file = sourcePaths.get(path);
        if (file != null) {
            return file;
        }
        return super.getFileForInput(location, packageName, relativeName);
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
