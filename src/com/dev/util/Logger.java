package com.dev.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");

    private final String className;

    public Logger(Class logClass) {
        this.className = logClass.getName();
    }

    public void info(String message) {
        System.out.println(DATE_FORMATTER.format(new Date()) + " INFO: " + className + " " + message);
    }
}
