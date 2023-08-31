import java.io.*;
import java.util.*;

import static java.lang.System.exit;

public class NewMain {
    private static String readFile(BufferedReader reader) {
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

    public static void main(String[] args) throws FileNotFoundException {
        String inputFileName = "students.txt";
        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
        String jsonString = readFile(reader);
        Map<String, Object> students = parseJson(jsonString);

        String[] options = {
                "1.\tПолучить список студентов по имени",
                "2.\tПолучение информации о студенте по id",
                "3.\tДобавить нового студента",
                "4.\tУдаление студента по id",
                "5.\tЗавершение работы"
        };

        Scanner scanner = new Scanner(System.in);
        int option;
//        while (true) {
//            printMenu(options);
//            option = scanner.nextInt();
//            switch (option){
//                case 1:
//                    List<String> studentsList = getStudentsByName(students);
//                    System.out.println(studentsList);
//                    break;
//                case 2:
//                    System.out.print("Введите id студента: ");
//                    int getId = scanner.nextInt();
//                    if (getId > students.size()) {
//                        System.out.println("Студента с таким id не существует!");
//                        break;
//                    } else {
//                        String student = getStudentById(students, getId);
//                        System.out.println("Имя студента с id=" + getId + ": " + student);
//                    }
//                    break;
//                case 3:
//                    System.out.print("Введите имя нового студента: ");
//                    String studentName = scanner.next();
//                    addStudent(students, studentName);
//                    break;
//                case 4:
//                    System.out.print("Введите id студента: ");
//                    int delId = scanner.nextInt();
//                    if (delId > students.size()) {
//                        System.out.println("Студента с таким id не существует!");
//                        break;
//                    } else {
//                        deleteStudentById(students, delId);
//                        System.out.println("Студент с id=" + delId + "успешно удалён");
//                        System.out.println(students);
//                    }
//                    break;
//                case 5:
//                    exit(0);
//            }
//        }
    }

    public static void printMenu(String[] options){
        for (String option : options){
            System.out.println(option);
        }
        System.out.print("Выберите действие: ");
    }

    public static Map<Integer, String> JSONtoMap(String file) {
        Map<Integer, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("\"id\"")) {
                    Integer id = Integer.parseInt(trimmedLine.substring(6, trimmedLine.length()-1));
                    line = reader.readLine().trim();
                    String name = line.substring(9, line.length()-1);
                    map.put(id, name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Object> parseJson(String jsonString) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonString = jsonString.replaceAll("\\s", "");

        // Удаляем начальные и конечные фигурные скобки (если есть)
        if (jsonString.startsWith("{")) {
            jsonString = jsonString.substring(1);
        }
        if (jsonString.endsWith("}")) {
            jsonString = jsonString.substring(0, jsonString.length()-1);
        }

        String[] keyValuePairs = jsonString.split(",");
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].replaceAll("\"", "");
            String value = keyValue[1];

            // Рекурсивно обрабатываем значение
            Object parsedValue = parseValue(value);
            jsonMap.put(key, parsedValue);
        }

        return jsonMap;
    }

    public static Object parseValue(String value) {
        // Проверяем, является ли значение строкой
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        }

        // Проверяем, является ли значение числом
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // Не является числом
        }

        // Проверяем, является ли значение boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }

        // Если не является ни строкой, ни числом, ни boolean,
        // то обрабатываем как вложенный JSON объект
        return parseJson(value);
    }

    public static List<String> getStudentsByName(Map<Integer, String> students) {
        List<String> studentsList = new ArrayList<>(students.values());
        Collections.sort(studentsList);
        return studentsList;
    }

    public static String getStudentById(Map<Integer, String> students, int id) {
        return students.get(id);
    }
    public static void addStudent(Map<Integer, String> students, String studentName) {
        students.put(students.size()+1, studentName);
    }

    public static void deleteStudentById(Map<Integer, String> students, int id) {
        students.remove(id);
    }

}
