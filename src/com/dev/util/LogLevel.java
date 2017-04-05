package com.dev.util;

public enum LogLevel {

    INFO(0),
    WARN(1),
    ERROR(2);

    int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
