package com.example.bookstore.config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;

public class LoggingConfig {

    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            return;
        }

        // Get root logger
        Logger rootLogger = Logger.getLogger("");

        // Remove existing handlers
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Create console handler with custom formatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomFormatter());
        consoleHandler.setLevel(Level.ALL);

        // Add handler to root logger
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.INFO);

        initialized = true;
    }

    // Custom formatter for better log output
    private static class CustomFormatter extends Formatter {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();
            builder.append(dateFormat.format(new Date(record.getMillis())));
            builder.append(" [");
            builder.append(record.getLevel());
            builder.append("] ");
            builder.append(record.getSourceClassName());
            builder.append(".");
            builder.append(record.getSourceMethodName());
            builder.append(": ");
            builder.append(formatMessage(record));
            builder.append("\n");

            if (record.getThrown() != null) {
                builder.append("Exception: ");
                builder.append(record.getThrown().toString());
                builder.append("\n");
                for (StackTraceElement element : record.getThrown().getStackTrace()) {
                    builder.append("\tat ");
                    builder.append(element.toString());
                    builder.append("\n");
                }
            }

            return builder.toString();
        }
    }
}
