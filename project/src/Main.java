import java.io.*;
import java.util.*;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Выберите режим подключения к FTP-серверу (1 - пассивный; 2 - активный): " );
        String choose = scanner.next();

        while (!(choose.equals("1") || choose.equals("2"))) {
            System.out.print("Введите 1 или 2: " );
            choose = scanner.next();
        }

        FtpConnection ftpConnection = new FtpConnection(Integer.valueOf(choose) != 2);
        ftpConnection.connect(FtpConnectionData.address, FtpConnectionData.user, FtpConnectionData.password);
        ftpConnection.retrieve(FtpConnectionData.fileName);
        BufferedReader reader = new BufferedReader(new FileReader(FtpConnectionData.fileName));
        String jsonString = FilesReader.readFile(reader);
        jsonString = jsonString.replaceAll("\\s", "");
        jsonString = jsonString.substring(13, jsonString.length() - 2);
        String[] jsonObjects = jsonString.split("\\},");
        RequestsHandler requestsHandler = new RequestsHandler(jsonObjects);
        int option;

        while (true) {
            try {
                MenuPrinter.printMenu();
                option = scanner.nextInt();

                switch (option) {
                    case 1:
                        List<String> studentsList = requestsHandler.getStudentsByName();
                        System.out.println(studentsList);
                        break;
                    case 2:
                        try {
                            System.out.print("Введите id студента: ");
                            int getId = scanner.nextInt();
                            List<Map<String, Object>> studentsMapsList = requestsHandler.getStudentsMapsList();

                            if (getId > (int) studentsMapsList.get(studentsMapsList.size() - 1).get("id") ||
                                    getId < (int) studentsMapsList.get(0).get("id")) {
                                System.out.println("Студента с таким id не существует!");
                                break;
                            } else {
                                String student = requestsHandler.getStudentInfoById(getId);
                                System.out.println("Информация о студенте с id=" + getId + ": " + student);
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("Студента с таким id не существует!");
                            scanner.next();
                        }
                        break;
                    case 3:
                        try {
                            System.out.println("Введите данные о новом студенте в формате \"key=value\" через запятую без пробела: ");
                            String studentInfo = scanner.next();
                            requestsHandler.addStudent(studentInfo);
                            System.out.println("Данные о новом студенте успешно добавлены.");
                        } catch (Exception e) {
                            System.out.println("Неправильный формат ввода данных!");
                        }
                        break;
                    case 4:
                        try {
                            System.out.print("Введите id студента: ");
                            int delId = scanner.nextInt();
                            List<Map<String, Object>> studentsMapsList = requestsHandler.getStudentsMapsList();

                            if (delId > (int) studentsMapsList.get(studentsMapsList.size() - 1).get("id") ||
                                    delId < (int) studentsMapsList.get(0).get("id")) {
                                System.out.println("Студента с таким id не существует!");
                                break;
                            } else {
                                requestsHandler.deleteStudentInfoById(delId);
                                System.out.println("Студент с id=" + delId + " успешно удалён.");
                            }
                        } catch (InputMismatchException e) {
                            System.out.println("Студента с id не существует!");
                            scanner.next();
                        }
                        break;
                    case 5:
                        exit(0);
                        ftpConnection.disconnect();
                }
            } catch (InputMismatchException e) {
                System.out.println("Введите число от 1 до 5!");
                scanner.next();
            }
        }
    }
}

