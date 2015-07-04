package playasophy.wonderdome.input;


import java.io.FileInputStream;
import java.io.InputStream;


public class FileSystemHandler {

    public final String root;


    public FileSystemHandler(String root) {
        this.root = root;
    }


    public String sketchPath(String fileName) {
        return root + "/" + fileName;
    }


    public InputStream createInput(String fileName) throws Exception {
        return new FileInputStream(sketchPath(fileName));
    }

}
