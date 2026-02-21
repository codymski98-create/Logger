package com.logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JsonLogger {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private LogLevel minLogLevel;
    private String filePath;
    private boolean consoleOutput;

    public JsonLogger() {
        this.minLogLevel = LogLevel.INFO;
        this.consoleOutput = true;
        this.filePath = null;
    }

    public void setMinLogLevel(LogLevel level) {
        this.minLogLevel = level;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setConsoleOutput(boolean enabled) {
        this.consoleOutput = enabled;
    }

    public void log(LogLevel level, String message) {
        log(level, message, null);
    }

    public void log(LogLevel level, String message, Map<String, Object> context) {
        if (level.getLevel() < minLogLevel.getLevel()) {
            return;
        }

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now().format(formatter));
        logEntry.put("level", level.getName());
        logEntry.put("message", message);

        if (context != null && !context.isEmpty()) {
            logEntry.put("context", context);
        }

        String jsonOutput = gson.toJson(logEntry);

        if (consoleOutput) {
            System.out.println(jsonOutput);
        }

        if (filePath != null) {
            writeToFile(jsonOutput);
        }
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    private void writeToFile(String jsonOutput) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(jsonOutput);
            writer.write("\n");
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}