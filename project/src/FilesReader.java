import java.io.BufferedReader;
import java.io.IOException;

public class FilesReader {
    public static String readFile(BufferedReader reader) {
        StringBuilder text = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                text.append(trimmedLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }
}
