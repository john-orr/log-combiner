package com.dev;

import java.util.Date;

public class LogEntry implements Comparable {

    private Date date;
    private StringBuilder content;
    private String cluster;

    LogEntry(Date date, String content, String cluster) {
        this.date = date;
        this.content = new StringBuilder(content);
        this.cluster = cluster;
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
        return cluster + "_" + content + "\n";
    }

    public void append(String line) {
        content.append("\n").append(line);
    }
}
