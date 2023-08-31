import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        FtpConnection ftpConnection = new FtpConnection();
        ftpConnection.doConnect("localhost", 2121, "admin", "admin");
//        System.out.println(ftpConnection.list());
        ftpConnection.retr("students.txt");

        String inputFileName = "students.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
