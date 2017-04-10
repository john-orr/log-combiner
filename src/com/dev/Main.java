package com.dev;

import com.dev.util.LogCombinerException;
import com.dev.util.LogLevel;
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
    private static final String FILENAME_FILTER = "^spo_admin(\\.\\d+)*.log(\\.\\d)?$";
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2} \\w{3} \\d{4}");
    private static final String OUTPUT_FILE = "spo_admin.log";

    private static Properties PROPERTIES = new Properties();

    private static List<File> logFiles;
    private static List<LogEntry> logEntries;

    public static void main(String[] args) throws LogCombinerException {
        init();
        findFiles();
        readLogs();
        writeLogs();
    }

    private static void init() throws LogCombinerException {
        loadProperties();
        setExcludePattern();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        parseDateFrom(simpleDateFormat);
        parseDateTo(simpleDateFormat);
        setLogLevel();
    }

    private static void setLogLevel() throws LogCombinerException {
        String logLevel = PROPERTIES.getProperty("exclude.level");
        if (logLevel != null && !logLevel.isEmpty()) {
            LogEntry.LOG_LEVEL = LogLevel.valueOf(logLevel.toUpperCase());
        } else {
            throw new LogCombinerException("Property exclude.level not found");
        }
    }

    private static void setExcludePattern() throws LogCombinerException {
        String regex = PROPERTIES.getProperty("exclude.regex");
        if (regex != null) {
            if (!regex.isEmpty()) {
                LogEntry.EXCLUDE_PATTERN = Pattern.compile(regex);
            }
        } else {
            throw new LogCombinerException("Property exclude.regex not found");
        }
    }

    private static void parseDateTo(SimpleDateFormat simpleDateFormat) throws LogCombinerException {
        String dateTo = PROPERTIES.getProperty("date.to");
        if (dateTo != null) {
            if (!dateTo.isEmpty()) {
                try {
                    LogEntry.TO_DATE = simpleDateFormat.parse(dateTo);
                } catch (ParseException e) {
                    throw new LogCombinerException("Error parsing property date.to", e);
                }
            }
        } else {
            throw new LogCombinerException("Property date.to not found");
        }
    }

    private static void parseDateFrom(SimpleDateFormat simpleDateFormat) throws LogCombinerException {
        String dateFrom = PROPERTIES.getProperty("date.from");
        if (dateFrom != null) {
            if (!dateFrom.isEmpty()) {
                try {
                    LogEntry.FROM_DATE = simpleDateFormat.parse(dateFrom);
                } catch (ParseException e) {
                    throw new LogCombinerException("Error parsing property date.from", e);
                }
            }
        } else {
            throw new LogCombinerException("Property date.from not found");
        }
    }

    private static void loadProperties() throws LogCombinerException {
        FileInputStream input = getFileInputStream();
        try {
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new LogCombinerException("Error loading properties", e);
        }
    }

    private static FileInputStream getFileInputStream() throws LogCombinerException {
        FileInputStream input;
        try {
            input = new FileInputStream("resources/config.properties");
        } catch (FileNotFoundException e) {
            throw new LogCombinerException("Error opening properties file", e);
        }
        return input;
    }

    private static void findFiles() {
        logFiles = new ArrayList<>();
        for (String location : LOCATIONS) {
            logFiles.addAll(getFiles(location));
        }
    }

    private static List<File> getFiles(String directory) {
        File directoryFile = new File(directory);
        LOG.info("Looking for files in " + directoryFile.getAbsolutePath());
        File[] files = directoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(FILENAME_FILTER);
            }
        });
        return Arrays.asList(files);
    }

    private static void readLogs() throws LogCombinerException {

        logEntries = new ArrayList<>();
        for (File logFile : logFiles) {
            BufferedReader reader = getBufferedReader(logFile);
            LOG.info("Reading from file: " + logFile);
            String cluster = getCluster(logFile);
            String line;
            LogEntry logEntry = null;
            while ((line = readLine(reader)) != null) {
                Matcher datePatternMatcher = DATE_PATTERN.matcher(line);
                if (datePatternMatcher.find()) {
                    if (logEntry != null) {
                        // commit entry to list
                        logEntries.add(logEntry);
                    }
                    // start new log entry
                    logEntry = new LogEntry(line, cluster);
                } else if (logEntry != null) {
                    // continuation of current entry
                    logEntry.append(line);
                } else {
                    throw new LogCombinerException("Line could not be processed: " + line);
                }
            }
            // Write final LogEntry
            logEntries.add(logEntry);
        }
        LOG.info(String.format("Read %d lines", logEntries.size()));
    }

    private static String readLine(BufferedReader reader) throws LogCombinerException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new LogCombinerException("Error reading line", e);
        }
    }

    private static BufferedReader getBufferedReader(File logFile) throws LogCombinerException {
        try {
            return new BufferedReader(new FileReader(logFile));
        } catch (FileNotFoundException e) {
            throw new LogCombinerException("Error opening " +  logFile, e);
        }
    }

    private static String getCluster(File logFile) {
        return logFile.getParent().substring(logFile.getParent().length()-1);
    }

    private static void writeLogs() throws LogCombinerException {
        Collections.sort(logEntries);

        int excludeCount = 0;
        FileWriter writer = getFileWriter();
        for (LogEntry logEntry : logEntries) {
            if (!logEntry.isExclude()) {
                write(writer, logEntry);
            } else {
                excludeCount++;
            }
        }
        LOG.info(String.format("Excluded %d lines", excludeCount));
        close(writer);
    }

    private static void close(FileWriter writer) throws LogCombinerException {
        try {
            writer.close();
        } catch (IOException e) {
            throw new LogCombinerException("Error closing writer", e);
        }
    }

    private static void write(FileWriter writer, LogEntry logEntry) throws LogCombinerException {
        try {
            writer.write(logEntry.toString());
        } catch (IOException e) {
            throw new LogCombinerException("Error writing LogEntry " + logEntry, e);
        }
    }

    private static FileWriter getFileWriter() throws LogCombinerException {
        try {
            return new FileWriter(OUTPUT_FILE);
        } catch (IOException e) {
            throw new LogCombinerException("Error opening file " + OUTPUT_FILE, e);
        }
    }
}
