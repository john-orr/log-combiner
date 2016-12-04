package com.dev;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Main {

    private static final String DIRECTORY = "resources/";
    private static final String FILENAME_FILTER = "^spo_admin.log.\\d$";
    private static final String DATE_FORMAT = "dd MMM yyyy HH:mm:ss";
    private static final String OUTPUT_FILE = "spo_admin.log";

    private static File[] logFiles;
    private static List<LogLine> logLines;

    public static void main(String[] args) throws IOException, ParseException {
        findFiles();
        readLogs();
        writeLogs();
    }

    private static void findFiles() {
        File directory = new File(DIRECTORY);
        System.out.println(directory.getAbsolutePath());
        logFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(FILENAME_FILTER);
            }
        });
    }

    private static void readLogs() throws IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

        logLines = new ArrayList<>();
        for (File logFile : logFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            System.out.println("Reading from file: " + logFile);
            String line;
            while ((line = reader.readLine()) != null) {
                Date date = simpleDateFormat.parse(line);
                logLines.add(new LogLine(date, line));
            }
        }
    }

    private static void writeLogs() throws IOException {
        Collections.sort(logLines);

        FileWriter writer = new FileWriter(OUTPUT_FILE);
        for (LogLine logLine : logLines) {
            writer.write(logLine.toString());
        }
        writer.close();
    }
}
