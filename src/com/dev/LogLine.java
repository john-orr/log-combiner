package com.dev;

import java.util.Date;

public class LogLine implements Comparable {

    private Date date;
    private String content;

    LogLine(Date date, String content) {
        this.date = date;
        this.content = content;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof LogLine) {
            LogLine that = (LogLine) o;
            return this.date.before(that.date) ? -1 : 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return content + "\n";
    }
}
