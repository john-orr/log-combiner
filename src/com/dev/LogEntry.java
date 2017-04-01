package com.dev;

import java.util.Date;
import java.util.regex.Pattern;

public class LogEntry implements Comparable {

    static Date FROM_DATE;
    static Date TO_DATE;
    static Pattern EXCLUDE_PATTERN;

    private Date date;
    private StringBuilder content;
    private String cluster;
    private boolean exclude;

    LogEntry(Date date, String content, String cluster) {
        this.date = date;
        this.content = new StringBuilder(content);
        this.cluster = cluster;
        if (outsideRange(date) || shouldExclude(content)) {
            exclude = true;
        }
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
