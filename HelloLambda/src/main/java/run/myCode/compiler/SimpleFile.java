package run.myCode.compiler;

/**
 * An immutable representation of a file
 * @author bdahl
 */
public class SimpleFile {
    private final String name;
    private final String data;

    public SimpleFile(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }
}
