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

    private static final String RESOURCES_NODE1 = "resources/node1";
    private static final String RESOURCES_NODE2 = "resources/node2";
    private static final String FILENAME_FILTER = "^spo_admin.log(\\.\\d)?$";
    private static final String DATE_FORMAT = "dd MMM yyyy HH:mm:ss,SSS";
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2} \\w{3} \\d{4}");
    private static final String OUTPUT_FILE = "spo_admin.log";

    private static File[] logFiles;
    private static List<LogEntry> logEntries;

    public static void main(String[] args) throws IOException, ParseException {
        findFiles();
        readLogs();
        writeLogs();
    }

    private static void findFiles() {
        File resourcesNode1 = new File(RESOURCES_NODE1);
        LOG.info(resourcesNode1.getAbsolutePath());
        File[] node1Logs = resourcesNode1.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(FILENAME_FILTER);
            }
        });
        File resourcesNode2 = new File(RESOURCES_NODE2);
        LOG.info(resourcesNode2.getAbsolutePath());
        File[] node2Logs = resourcesNode2.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(FILENAME_FILTER);
            }
        });
        logFiles = new File[node1Logs.length + node2Logs.length];
        System.arraycopy(node1Logs, 0, logFiles, 0, node1Logs.length);
        System.arraycopy(node2Logs, 0, logFiles, node1Logs.length, node2Logs.length);
    }

    private static void readLogs() throws IOException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

        logEntries = new ArrayList<>();
        Map<String, Date> firstDateForCluster = new HashMap<>();
        Map<String, Date> lastDateForCluster = new HashMap<>();
        for (File logFile : logFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            LOG.info("Reading from file: " + logFile);
            String cluster = logFile.getParent().substring(logFile.getParent().length()-1);
            String line;
            LogEntry logEntry = null;
            Date date = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = DATE_PATTERN.matcher(line);
                if (matcher.find()) {
                    if (logEntry != null) {
                        // commit entry to list
                        logEntries.add(logEntry);
                    }
                    // start new log entry
                    date = simpleDateFormat.parse(line);
                    logEntry = new LogEntry(date, line, cluster);
                    if (!firstDateForCluster.containsKey(cluster) || firstDateForCluster.get(cluster).after(date)) {
                        firstDateForCluster.put(cluster, date);
                    }
                } else if (logEntry != null) {
                    // continuation of current entry
                    logEntry.append(line);
                } else {
                    throw new IllegalStateException("Line could not be processed: " + line);
                }
            }
            if (!lastDateForCluster.containsKey(cluster) || lastDateForCluster.get(cluster).before(date)) {
                lastDateForCluster.put(cluster, date);
            }
            // Write final LogEntry
            logEntries.add(logEntry);
        }
        LOG.info("These files cover the period\n\t\t\t\t\t\t" + simpleDateFormat
                .format(latestFirstDate(firstDateForCluster)) + " to\n\t\t\t\t\t\t" + simpleDateFormat
                .format(earliestLastDate(lastDateForCluster)));
    }

    private static Date earliestLastDate(Map<String, Date> lastDateForCluster) {
        Date earliestLastDate = null;
        for (Date lastDateOfCluster : lastDateForCluster.values()) {
            if (earliestLastDate == null || earliestLastDate.after(lastDateOfCluster)) {
                earliestLastDate = lastDateOfCluster;
            }
        }
        return earliestLastDate;
    }

    private static Date latestFirstDate(Map<String, Date> firstDateForCluster) {
        Date latestFirstDate = null;
        for (Date firstDateOfCluster : firstDateForCluster.values()) {
            if (latestFirstDate == null || latestFirstDate.before(firstDateOfCluster)) {
                latestFirstDate = firstDateOfCluster;
            }
        }
        return latestFirstDate;
    }

    private static void writeLogs() throws IOException {
        Collections.sort(logEntries);

        FileWriter writer = new FileWriter(OUTPUT_FILE);
        for (LogEntry logEntry : logEntries) {
            writer.write(logEntry.toString());
        }
        writer.close();
    }
}
