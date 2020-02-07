package kyiv.tigers;

import com.google.common.io.Resources;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileToString {
    public static String stringFromFile(final String fileName) {
        try {
            return Resources.toString(Resources.getResource(fileName), UTF_8);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
