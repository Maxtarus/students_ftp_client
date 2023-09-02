import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.StringTokenizer;

public class FtpConnection {
    private Socket socket;
    private BufferedReader reader;
    private Socket dataSocket;
    private ServerSocket serverSocket;
    private BufferedWriter writer;
    private boolean isPassive;
    private String ip;
    private int port;

    public FtpConnection(boolean isPassive) {
        this.isPassive = isPassive;
        this.port = 21;
    }

    public void connect(String address, String user, String password) throws IOException {
        if (socket != null) {
            throw new IOException("Сеанс FTP уже запущен, пожалуйста, сначала отключитесь!");
        }

        socket = new Socket(address, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String hostResponse = reader.readLine();

        if (!hostResponse.startsWith("220")) {
            throw new IOException("Получен неизвестный ответ при подключении: " + hostResponse);
        }

        sendCommand("USER " + user);
        hostResponse = reader.readLine();

        if (!hostResponse.startsWith("331")) {
            throw new IOException("Получен неизвестный ответ при подключении: " + hostResponse);
        }

        sendCommand("PASS " + password);
        hostResponse = reader.readLine();

        if (!hostResponse.startsWith("230")) {
            throw new IOException("Получен неизвестный ответ при подключении: " + hostResponse);
        }
    }

    public void disconnect() throws IOException {
        try {
            sendCommand("QUIT");
        } finally {
            socket.close();
            socket = null;
        }
    }

    private void sendCommand(String command) throws IOException {
        if (socket == null) {
            throw new IOException("Нет подключения к хосту.");
        }

        try {
            writer.write(command + "\r\n");
            writer.flush();
        } catch (IOException e) {
            socket = null;
            e.printStackTrace();
        }
    }

    public void enterLocalPassiveMode() throws IOException {

        sendCommand("PASV");
        String response = reader.readLine();

        if (!response.startsWith("227 ")) {
            throw new IOException("Не удалось запросить пассивный режим: " + response);
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

    }

    public void enterLocalActiveMode() throws IOException {
        InetAddress address = InetAddress.getLocalHost();
        int index = address.toString().indexOf('/');
        String ip = address.toString().substring(index+1);
        ip = ip.replace(".", ",");
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        String binaryPort = String.format("%16s", Integer.toBinaryString(port)).replaceAll(" ", "0");
        String firstByte = binaryPort.substring(0,8);
        String secondByte = binaryPort.substring(8,16);
        int firstByteNumber = Integer.parseInt(firstByte, 2);
        int secondByteNumber = Integer.parseInt(secondByte, 2);
        StringBuilder ipAndPort = new StringBuilder()
                .append(ip).append(",")
                .append(firstByteNumber)
                .append(",")
                .append(secondByteNumber);
        sendCommand("PORT " + ipAndPort);
        String response = reader.readLine();

        if (!response.startsWith("200 ")) {
            throw new IOException("Could not request ACTIVE mode: " + response);
        }
    }

    public String getCurrentDirectory() throws IOException {

        sendCommand("PWD");
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

    public void retrieve(String fileName) throws IOException {

        if (isPassive) {
            enterLocalPassiveMode();
        } else {
            enterLocalActiveMode();
        }

        String fullPath = getCurrentDirectory() + fileName;
        sendCommand("RETR " + fullPath);
        if (!isPassive) {
            dataSocket = serverSocket.accept();
        }

        String response = reader.readLine();

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

        if (!response.startsWith("226")) {
            throw new IOException("Ошибка");
        }
    }
}
