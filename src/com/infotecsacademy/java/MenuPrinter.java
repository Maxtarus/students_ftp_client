package com.infotecsacademy.java;

public class MenuPrinter {
    public static final String[] options = {
            "1.\tПолучить список студентов по имени;",
            "2.\tПолучение информации о студенте по id;",
            "3.\tДобавить информацию о новом студенте;",
            "4.\tУдаление студента по id;",
            "5.\tЗавершение работы."
    };

    public static void printMenu() {
        for (String option : options) {
            System.out.println(option);
        }
        System.out.print("Выберите действие: ");
    }
}
