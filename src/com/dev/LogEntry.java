package com.dev;

import com.dev.util.LogCombinerException;
import com.dev.util.LogLevel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry implements Comparable {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");

    static Date FROM_DATE;
    static Date TO_DATE;
    static Pattern EXCLUDE_PATTERN;
    static LogLevel LOG_LEVEL;

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

    private static Date parseDateFromLine(String line) throws LogCombinerException {
        try {
            return DATE_FORMAT.parse(line);
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
        return parseLogLevelFromLine(line).getLevel() <= LOG_LEVEL.getLevel();
    }

    private boolean shouldExclude(String content) {
        return EXCLUDE_PATTERN != null && EXCLUDE_PATTERN.matcher(content).find();
    }

    private boolean outsideRange(Date date) {
        return FROM_DATE != null && date.before(FROM_DATE)
                || TO_DATE != null && date.after(TO_DATE);
    }

    public boolean isExclude() {
        return exclude;
    }

    @Override
    public int compareTo(Object o) {
        LogEntry that = (LogEntry) o;
        if (this.date.before(that.date)) {
            return -1;
        } else if (this.date.after(that.date)) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return cluster + " " + content + "\n";
    }

    public void append(String line) {
        content.append("\n").append(line);
        if (shouldExclude(line)) {
            exclude = true;
        }
    }
}
