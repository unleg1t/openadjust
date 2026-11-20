package com.adjustclient.core.logging;

import com.adjustclient.managers.storage.LocalStorage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

public final class LogFile implements AutoCloseable {
    public File directory;
    public File file;
    public BufferedWriter writer;
    public Object field16;

    public LogFile() {
        try {
            directory = new File(LocalStorage.resolveStoragePath(), "logs");
            if (!directory.exists() && !directory.mkdirs()) {
                Logger.log(LogLevel.WARN, true, "Unable to create logs folder");
            }
            file = new File(directory, "latest.log");
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException exception) {
            Logger.log(LogLevel.WARN, false, "Unable to open Adjust logs file: {}", exception);
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException exception) {
                Logger.log(LogLevel.WARN, false, "Unable to close Adjust logs file: {}", exception);
            }
            writer = null;
        }
        if (file == null || directory == null) {
            return;
        }
        try {
            String date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            File archive;
            int index = 0;
            do {
                archive = new File(directory, date + "-" + ++index + ".log.gz");
            } while (archive.exists());
            byte[] buffer = new byte[1024];
            try (GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(archive));
                 FileInputStream input = new FileInputStream(file)) {
                int count;
                while ((count = input.read(buffer, 0, 1024)) >= 1) {
                    output.write(buffer, 0, count);
                }
                output.finish();
            }
        } catch (Exception exception) {
            Logger.log(LogLevel.WARN, false, "Unable to GZIP Adjust logs file: {}", exception);
        }
    }

    public void write(String line) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(line + "\n");
            writer.flush();
        } catch (IOException exception) {
            Logger.log(LogLevel.WARN, false, "Failed to write to Adjust logs file: {}", exception);
        }
    }
}
