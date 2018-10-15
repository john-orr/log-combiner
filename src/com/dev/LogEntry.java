package com.dev;

import com.dev.util.LogCombinerException;
import com.dev.util.LogLevel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry implements Comparable<LogEntry> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");

    static Date fromDate;
    static Date toDate;
    static Pattern excludePattern;
    static Pattern includePattern;
    static LogLevel logLevel;

    private Date date;
    private StringBuilder content;
    private String cluster;
    private boolean exclude;

    LogEntry(String content, String cluster) throws LogCombinerException {
        this.date = parseDateFromLine(content);
        this.content = new StringBuilder(content);
        this.cluster = cluster;
        if (outsideRange(date) || logLevelTooLow(content) || shouldExclude(content)) {
            exclude = true;
        }
    }

    private Date parseDateFromLine(String line) throws LogCombinerException {
        try {
            return dateFormat.parse(line);
        } catch (ParseException e) {
            throw new LogCombinerException("Error parsing date from " + line, e);
        }
    }

    private static LogLevel parseLogLevelFromLine(String line) throws LogCombinerException {
        Matcher extractLevelMatcher = Pattern.compile(",\\d{3} (.+?):").matcher(line);
        if (extractLevelMatcher.find()) {
            return LogLevel.valueOf(extractLevelMatcher.group(1).trim());
        } else {
            throw new LogCombinerException("Error parsing log level from " + line);
        }
    }

    private boolean logLevelTooLow(String line) throws LogCombinerException {
        return parseLogLevelFromLine(line).getLevel() < logLevel.getLevel();
    }

    private boolean shouldExclude(String content) {
        return excludePattern != null && excludePattern.matcher(content).find();
    }

    private boolean outsideRange(Date date) {
        return fromDate != null && date.before(fromDate)
                || toDate != null && date.after(toDate);
    }

    boolean isExclude() {
        return exclude;
    }

    @Override
    public int compareTo(LogEntry o) {
        if (this.date.before(o.date)) {
            return -1;
        } else if (this.date.after(o.date)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LogEntry logEntry = (LogEntry) o;
        return exclude == logEntry.exclude &&
                Objects.equals(dateFormat, logEntry.dateFormat) &&
                Objects.equals(date, logEntry.date) &&
                Objects.equals(content, logEntry.content) &&
                Objects.equals(cluster, logEntry.cluster);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFormat, date, content, cluster, exclude);
    }

    @Override
    public String toString() {
        return cluster + " " + content + "\n";
    }

    void append(String line) {
        content.append("\n").append(line);
        if (shouldExclude(line)) {
            exclude = true;
        }
    }
}
