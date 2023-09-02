package com.infotecsacademy.java;

import java.util.*;

public class RequestsHandler {
    private final List<Map<String, Object>> studentsMapsList;

    public RequestsHandler(String[] jsonObjects) {
        studentsMapsList = JsonParser.parseJson(jsonObjects);
    }

    public List<Map<String, Object>> getStudentsMapsList() {
        return studentsMapsList;
    }

    public List<String> getStudentsByName() {
        List<String> studentsList = new ArrayList<>();

        for (Map<String, Object> studentMap : studentsMapsList) {
            studentsList.add((String) studentMap.get("name"));
        }

        Collections.sort(studentsList);
        return studentsList;
    }

    public String getStudentInfoById(int id) {
        String studentInfo = null;
        for (Map<String, Object> studentMap : studentsMapsList) {
            if ((int)studentMap.get("id") == id) {
                studentInfo = String.valueOf(studentMap);
            }
        }
        return studentInfo;
    }

    public void addStudent(String studentInfo) {
        String[] studentInfoItems = studentInfo.split(",");
        Map<String, Object> studentMap = new LinkedHashMap<>();
        Map<String, Object> lastStudentMap = studentsMapsList.get(studentsMapsList.size()-1);
        int lastStudentId = (int) lastStudentMap.get("id");
        studentMap.put("id", lastStudentId+1);

        for (String studentKeyValuePair : studentInfoItems) {
            String[] keyValue = studentKeyValuePair.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            Object parsedValue = JsonParser.parseValue(value);
            studentMap.put(key, parsedValue);
        }

        studentsMapsList.add(lastStudentId, studentMap);
    }

    public void deleteStudentInfoById(int id) {
        for (Map<String, Object> studentMap : studentsMapsList) {
            if ((int)studentMap.get("id") == id) {
                studentsMapsList.remove(studentMap);
                break;
            }
        }
    }
}
