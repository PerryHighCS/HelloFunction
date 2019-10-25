package run.myCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import run.myCode.compiler.InMemoryJavaFileObject;

/**
 *
 * @author bdahl
 */
public class FileManager {
    /**
     * Create a list of source files from the initial request
     *
     * @param req a RequestClass
     * @return
     */
    public static List<InMemoryJavaFileObject> createSourceFileObjects(CompileRequest req) {

        List<InMemoryJavaFileObject> objects = new ArrayList<>(req.getSourceFiles().size());

        // Pull all of the source code files in the request
        req.getSourceFiles().forEach((f) -> {
            String contents = "";
            for (String s : f.getContents()) {
                // Save the file's contents
                contents += s + '\n';
            }

            // Add them to a list of files
            objects.add(new InMemoryJavaFileObject(f.getName(), contents));
        });
        return objects;
    }
    
    public static void saveData(DataRequest data) {
        if (data == null) {
            return;
        }
        
        data.getDataFiles().forEach((file) -> {
            File f = (new File(file.getName()));
        
            try (PrintWriter out = new PrintWriter(f.getAbsoluteFile())) {
                file.getContents().forEach((text) -> {
                    out.println(text);
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.err.println("Couldn't write file: " + file.getName());
            }
        });
    }

    public static void cleanDir(File dir) {
        File[] dirfiles = dir.listFiles();

        if (dirfiles != null) {
            for (File f : dirfiles) {
                if (f.isDirectory()) {
                    cleanDir(f);
                }
                f.delete();
            }
        }
    }    
}
