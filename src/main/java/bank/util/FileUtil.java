package bank.util;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileUtil {

    String readFile(String filePath) throws IOException;
}
