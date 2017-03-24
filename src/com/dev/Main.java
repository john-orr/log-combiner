package com.dev;

import com.dev.util.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Logger LOG = new Logger(Main.class);

    private static final List<String> LOCATIONS = Arrays.asList("resources/node1", "resources/node2");
    private static final String FILENAME_FILTER = "^spo_admin.log(\\.\\d)?$";
    private static final String DATE_FORMAT = "dd MMM yyyy HH:mm:ss,SSS";
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2} \\w{3} \\d{4}");
    private static final String OUTPUT_FILE = "spo_admin.log";
    private static final Pattern EXCLUDE_PATTERN =
            Pattern.compile("InternationalPlanCountryCodesInitializer");

    private static List<File> logFiles;
    private static List<LogEntry> logEntries;

    public static void main(String[] args) throws IOException, ParseException {
        findFiles();
        readLogs();
        writeLogs();
    }

    private static void findFiles() {
        logFiles = new ArrayList<>();
        for (String location : LOCATIONS) {
            logFiles.addAll(getFiles(location));
        }
    }

    private static List<File> getFiles(String directory) {
        File directoryFile = new File(directory);
        LOG.info(directoryFile.getAbsolutePath());
        File[] files = directoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(FILENAME_FILTER);
            }
        });
        return Arrays.asList(files);
    }

    private static void readLogs() throws IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

        logEntries = new ArrayList<>();
        for (File logFile : logFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            LOG.info("Reading from file: " + logFile);
            String cluster = getCluster(logFile);
            String line;
            LogEntry logEntry = null;
            while ((line = reader.readLine()) != null) {
                Matcher datePatternMatcher = DATE_PATTERN.matcher(line);
                if (datePatternMatcher.find()) {
                    if (logEntry != null) {
                        // commit entry to list
                        logEntries.add(logEntry);
                    }
                    // start new log entry
                    Date date = simpleDateFormat.parse(line);
                    logEntry = new LogEntry(date, line, cluster);
                } else if (logEntry != null) {
                    // continuation of current entry
                    logEntry.append(line);
                } else {
                    throw new IllegalStateException("Line could not be processed: " + line);
                }
                Matcher excludeMatcher = EXCLUDE_PATTERN.matcher(line);
                if (excludeMatcher.find()) {
                    logEntry.setExclude();
                }
            }
            // Write final LogEntry
            logEntries.add(logEntry);
        }
        LOG.info(String.format("Read %d lines", logEntries.size()));
    }

    private static String getCluster(File logFile) {
        return logFile.getParent().substring(logFile.getParent().length()-1);
    }

    private static void writeLogs() throws IOException {
        Collections.sort(logEntries);

        int excludeCount = 0;
        FileWriter writer = new FileWriter(OUTPUT_FILE);
        for (LogEntry logEntry : logEntries) {
            if (!logEntry.isExclude()) {
                writer.write(logEntry.toString());
            } else {
                excludeCount++;
            }
        }
        LOG.info(String.format("Excluded %d lines", excludeCount));
        writer.close();
    }
}
