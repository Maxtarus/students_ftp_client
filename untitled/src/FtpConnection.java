import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.StringTokenizer;

public class FtpConnection {

    private Socket socket;
    private BufferedReader reader;
    private Socket dataSocket;
    private BufferedWriter writer;
    private boolean isPassive;
    private String ip;
    private int port;

    public FtpConnection() {
    }

    public void doConnect(String host, int portNumber) throws IOException {
        doConnect(host, portNumber, "anonymous", "anonymous");
    }

    public boolean doConnect(String host, int port, String user, String password) throws IOException {
        if (socket != null) {
            throw new IOException("FTP session already initiated, please disconnect first!");
        }

        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String hostResponse = reader.readLine();

        if (!hostResponse.startsWith("220")) {
            throw new IOException("Received unknown response when connecting: " + hostResponse);
        }

        sendLine("USER " + user);

        hostResponse = reader.readLine();
        if (!hostResponse.startsWith("331")) {
            throw new IOException("Received unknown response providing username: " + hostResponse);
        }

        sendLine("PASS " + password);
        hostResponse = reader.readLine();

        if (!hostResponse.startsWith("230")) {
            if (hostResponse.startsWith("530")) {
                return false;
            } else {
                throw new IOException("Received unknown response when providing password: " + hostResponse);
            }
        }
        return true;
    }

    public synchronized void disconnect() throws IOException {

        try {
            sendLine("QUIT");
        } finally {
            socket.close();
            socket = null;
        }
    }

    public String list() throws IOException {
        if (!isPassive) {
            passv();
        }

        sendLine("LIST");
        String response = reader.readLine();

        if (!response.startsWith("150")) {
            throw new IOException("Cannot list the remote directory");
        }

        BufferedInputStream input = new BufferedInputStream(dataSocket.getInputStream());
        byte[] buffer = new byte[4096];
        int bytesRead;
        String content = null;

        while ((bytesRead = input.read(buffer)) != -1) {
            content = new String(buffer, 0, bytesRead);
        }

//        input.close();
        response = reader.readLine();

        if (response.startsWith("226")) {
            return content;
        } else {
            throw new IOException("Error");
        }
    }

    private void sendLine(String command) throws IOException {
        if (socket == null) {
            throw new IOException("Not connected to a host");
        }

        try {
            writer.write(command + "\r\n");
            writer.flush();
        } catch (IOException e) {
            socket = null;
            e.printStackTrace();
        }
    }

    public boolean passv() throws IOException {

        sendLine("PASV");
        String response = reader.readLine();

        if (!response.startsWith("227 ")) {
            throw new IOException("Could not request PASSIVE mode: " + response);
        }

        ip = null;
        port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);

        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("Received bad data information: " + response);
            }
        }
        dataSocket = new Socket(ip, port);
        isPassive = true;

        return true;
    }

    public String pwd() throws IOException {

        sendLine("PWD");
        String dir = null;
        String response = reader.readLine();

        if (response.startsWith("257 ")) {
            int firstQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', firstQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(firstQuote + 1, secondQuote);
            }
        }
        return dir;
    }

    public boolean retr(String fileName) throws IOException {

        if (!isPassive) {
            passv();
        }

        String fullPath = pwd() + "/" + fileName;
        String response = reader.readLine();
        sendLine("RETR " + fullPath);

        if (!response.startsWith("150")) {
            throw new IOException("Unable to download file from the remote server");
        }

        BufferedInputStream input = new BufferedInputStream(dataSocket.getInputStream());
        BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(new File(fileName).toPath()));

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            output.flush();
        }

        output.close();
        input.close();
        response = reader.readLine();

        return checkFileOperationsStatus(response);

    }

    private boolean checkFileOperationsStatus(String response) throws IOException {
        if (!response.startsWith("226")) {
            throw new IOException("Error");
        } else {
            isPassive = false;
            return response.startsWith("226 ");
        }

    }
}
