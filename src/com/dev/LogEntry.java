package com.dev;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntry implements Comparable {

    private static final String FROM_DATE = "16/03/2017";
    private static final String TO_DATE = "";

    private Date date;
    private StringBuilder content;
    private String cluster;
    private boolean exclude;

    LogEntry(Date date, String content, String cluster) throws ParseException {
        this.date = date;
        this.content = new StringBuilder(content);
        this.cluster = cluster;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        Date excludeBeforeDate = dateFormatter.parse(FROM_DATE);
        Date excludeAfterDate = new Date();
        if (!TO_DATE.equals("")) {
            dateFormatter.parse(TO_DATE);
        }
        if (date.before(excludeBeforeDate) || date.after(excludeAfterDate)) {
            exclude = true;
        }
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude() {
        exclude = true;
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
    }
}
