package com.infotecsacademy.java;

import java.util.*;

public class JsonParser {

    public static List<Map<String, Object>> parseJson(String[] jsonObjects) {

        List<Map<String, Object>> jsonMapsList = new ArrayList<>();

        for (String jsonObject : jsonObjects) {
            Map<String, Object> jsonMap = new LinkedHashMap<>();
            String jsonString = jsonObject.replaceAll("\\s", "");

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
            jsonMapsList.add(jsonMap);
        }


        return jsonMapsList;
    }

    public static Object parseValue(String value) {
        // Проверяем, является ли значение boolean
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }

        // Проверяем, является ли значение строкой
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
        }

        // Проверяем, является ли значение числом
        if (value.contains(".")) {
            return Double.parseDouble(value);
        } else {
            return Integer.parseInt(value);
        }

    }
}
