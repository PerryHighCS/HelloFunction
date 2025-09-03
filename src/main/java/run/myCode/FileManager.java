package run.myCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

            String fileName = f.getName();
            // If the source declares a package, prefix the file name with its
            // package path so the compiler's file manager can resolve the
            // class correctly.  ECJ expects the source path to mirror the
            // package declaration, otherwise it will try to locate the file on
            // disk and fail with "File ... is missing".
            Pattern pkgPattern = Pattern.compile("(?m)^\\s*package\\s+([\\w\\.]+)\\s*;");
            Matcher m = pkgPattern.matcher(contents);
            if (m.find()) {
                fileName = m.group(1).replace('.', '/') + "/" + fileName;
            }

            // Add them to a list of files
            objects.add(new InMemoryJavaFileObject(fileName, contents));
        });
        return objects;
    }
    
    public static void saveData(DataRequest data) {
        if (data == null) {
            return;
        }
        
        Base64.Decoder decoder = Base64.getDecoder();
        
        data.getDataFiles().forEach((file) -> {
            File f = (new File(file.getName()));
        
            StringBuilder sb = new StringBuilder();
            
            file.getContents().forEach((text)->{
                sb.append(text);
            });
            
            String fileContent = sb.toString();
            
            try (OutputStream out = new FileOutputStream(f.getAbsoluteFile())) {
                byte[] contents;
                
                // Base64 decode the file contents if necessary
                try {
                    contents = decoder.decode(fileContent);
                }
                catch (IllegalArgumentException e) {
                    contents = fileContent.getBytes();
                }
                out.write(contents);                
            } catch (IOException e) {
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
