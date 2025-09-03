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

    private static final boolean DEBUG = Boolean.getBoolean("run.mycode.debug");

    private final FromMemoryClassLoader xcl;
    private final Map<String, JavaFileObject> sources = new HashMap<>();
    private final Map<String, JavaFileObject> sourcePaths = new HashMap<>();

    @SuppressWarnings("unchecked")
    public InMemoryJavaFileManager(StandardJavaFileManager sjfm, FromMemoryClassLoader xcl) {
        super(sjfm);
        this.xcl = xcl;
    }

    public void addSource(JavaFileObject file) {
        // The JavaFileObject#getName returns the URI path.  Normalise it so it
        // can be looked up with or without a leading slash and use the path to
        // derive the binary name for the class.
        String path = file.getName();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        sourcePaths.put(path, file);
        if (path.endsWith(".java")) {
            String className = path.substring(0, path.length() - 5)
                                  .replace('/', '.');
            sources.put(className, file);

            // Also record the expected package path (e.g. foo/Bar.java) so
            // lookups using package-qualified names can be resolved.
            String pkgPath = className.replace('.', '/') + ".java";
            sourcePaths.put(pkgPath, file);

            if (DEBUG) {
                System.out.println("Registered source " + file.getName());
            }
        }
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        String name = file.getName();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (name.endsWith(".java")) {
            name = name.substring(0, name.length() - 5);
        }
        return name.replace('/', '.');
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        String name = className;
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        JavaFileObject file = sources.get(name);
        if (DEBUG) {
            System.out.println("getJavaFileForInput(" + className + ") -> " + (file != null ? "memory" : "disk"));
        }
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
        if (DEBUG) {
            System.out.println("getFileForInput(" + path + ") -> " + (file != null ? "memory" : "disk"));
        }
        if (file != null) {
            return file;
        }
        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        MemoryByteCode mbc = new MemoryByteCode(name);
        xcl.addClass(name, mbc);
        if (DEBUG) {
            System.out.println("Storing compiled class " + name);
        }
        return mbc;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return xcl;
    }
}
