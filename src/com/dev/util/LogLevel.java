package com.dev.util;

public enum LogLevel {

    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3);

    int level;

    LogLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
