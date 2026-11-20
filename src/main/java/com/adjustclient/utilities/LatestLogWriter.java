package com.adjustclient.utilities;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;

public class LatestLogWriter {
    public BufferedWriter writer;
    public String name;

    public LatestLogWriter(BufferedWriter writer, String name) { this.writer = writer; this.name = name; }

    public void close() {
        if (writer == null) return;
        try {
            writer.close();
        } catch (IOException exception) {
            Logger.log(LogLevel.WARN, true, "Unable to close latest.log: {}", exception);
        }
    }

    public void write(String message) {
        if (writer == null) return;
        try {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            writer.write("[" + time + "] [" + name + "/INFO]: " + message + "\r\n");
            writer.flush();
        } catch (IOException exception) {
            Logger.log(LogLevel.WARN, true, "Failed to write log to latest.log: {}", exception);
        }
    }
}
