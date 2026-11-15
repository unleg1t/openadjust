package com.adjustclient.lunar;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.Inflater;

public final class LunarVersionFetcher {
    public final LunarConfig config;
    public Object field8;
    private static final Gson GSON;

    static { GSON = new Gson(); }

    public LunarVersionFetcher(LunarConfig config) { this.config = config; }

    public void fetchVersionAsync() {
        Thread thread = new Thread(() -> {
            fetchVersion();
        }, "Moonlight-VersionFetch");
        thread.setDaemon(true);
        thread.start();
    }

    private long contentLength(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("HEAD");
        connection.setRequestProperty("User-Agent", "LunarClient/3.3.3");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
        return connection.getContentLengthLong(); // content-length
    }

    private static int findEndOfCentralDirectory(byte[] bytes) {
        for (int i = bytes.length - 22; i >= 0; i--) {
            if (bytes[i] == 80 && bytes[i + 1] == 75 && bytes[i + 2] == 5 && bytes[i + 3] == 6) return i;
        }
        return -1;
    }

    private Properties readBuildData(String url, long size) throws Exception {
        byte[] tail = readRange(url, Math.max(0L, size - 65536L), size - 1);
        int end = findEndOfCentralDirectory(tail);
        if (end < 0) {
            Logger.log(LogLevel.WARN, true, "ZIP EOCD not found in lunar.jar, using fallback", new Object[0]);
            return null;
        }
        ByteBuffer eocd = ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN);
        eocd.position(end + 12);
        int directorySize = eocd.getInt();
        int directoryOffset = eocd.getInt();
        ByteBuffer directory = ByteBuffer.wrap(readRange(url, directoryOffset,
                directoryOffset + (long) directorySize - 1)).order(ByteOrder.LITTLE_ENDIAN);
        while (directory.remaining() >= 47 && directory.getInt() == 0x02014b50) {
            directory.position(directory.position() + 6);
            int compression = Short.toUnsignedInt(directory.getShort());
            directory.position(directory.position() + 8);
            int compressedSize = directory.getInt();
            directory.position(directory.position() + 4);
            int nameLength = Short.toUnsignedInt(directory.getShort());
            int extraLength = Short.toUnsignedInt(directory.getShort());
            int commentLength = Short.toUnsignedInt(directory.getShort());
            directory.position(directory.position() + 8);
            int localOffset = directory.getInt();
            byte[] name = new byte[nameLength];
            directory.get(name);
            if (new String(name, StandardCharsets.UTF_8).equals("lunarBuildData.txt"))
                return readEntry(url, localOffset, compressedSize, compression);
            directory.position(directory.position() + extraLength + commentLength);
        }
        Logger.log(LogLevel.WARN, true, "lunarBuildData.txt not found in lunar.jar, using fallback", new Object[0]);
        return null;
    }

    private Properties readEntry(String url, int offset, int compressedSize, int compression) throws Exception {
        byte[] local = readRange(url, offset, offset + (long) compressedSize + 286);
        ByteBuffer header = ByteBuffer.wrap(local).order(ByteOrder.LITTLE_ENDIAN);
        header.position(26);
        int nameLength = Short.toUnsignedInt(header.getShort());
        int extraLength = Short.toUnsignedInt(header.getShort());
        byte[] compressed = new byte[compressedSize];
        System.arraycopy(local, 30 + nameLength + extraLength, compressed, 0, compressedSize);
        byte[] data = compressed;
        if (compression == 8) {
            Inflater inflater = new Inflater(true);
            inflater.setInput(compressed);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                output.write(buffer, 0, count);
            }
            inflater.end();
            data = output.toByteArray();
        }
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(data));
        return properties;
    }

    public void fetchVersion() {
        try {
            String url = fetchArtifactUrl();
            if (url == null) return;
            long length = contentLength(url);
            if (length < 1) {
                Logger.log(LogLevel.WARN, true, "Could not determine lunar.jar size, using fallback", new Object[0]);
                return;
            }
            Properties properties = readBuildData(url, length);
            if (properties == null) return;
            String version = properties.getProperty("lunarVersion");
            String commit = properties.getProperty("fullGitHash");
            if (version == null || commit == null) {
                Logger.log(LogLevel.WARN, true, "lunarBuildData.txt missing version fields, using fallback", new Object[0]);
                return;
            }
            config.version = version;
            config.gitCommit = commit;
            config.fetchedAt = System.currentTimeMillis();
            config.save();
            LunarClientService.INSTANCE.setLunarVersion(version, commit);
        } catch (Exception error) {
            Logger.log(LogLevel.WARN, true, "Version fetch failed ({}), using fallback", new Object[]{error.getMessage()});
        }
    }

    private static String fetchArtifactUrl() throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("hwid", "0");
        request.addProperty("hwid_private", "0");
        request.addProperty("os", operatingSystem());
        request.addProperty("arch", architecture());
        request.addProperty("launcher_version", "3.3.3");
        request.addProperty("version", "1.8.9");
        request.addProperty("branch", "master");
        request.addProperty("launch_type", "OFFLINE");
        request.addProperty("installation_id", UUID.randomUUID().toString());
        request.addProperty("os_release", System.getProperty("os.version", "6.1.0"));
        request.addProperty("module", "lunar");
        byte[] bytes = request.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL("https://api.lunarclientprod.com/launcher/launch").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "LunarClient/3.3.3");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        OutputStream output = connection.getOutputStream();
        output.write(bytes);
        output.close();
        if (connection.getResponseCode() != 200) {
            Logger.log(LogLevel.WARN, true, "Launch API returned {}, using fallback", new Object[]{connection.getResponseCode()});
            return null;
        }
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject response = GSON.fromJson(reader, JsonObject.class);
            JsonObject launch = response.getAsJsonObject("launchTypeData");
            if (launch == null || launch.getAsJsonArray("artifacts") == null) return null;
            for (JsonElement entry : launch.getAsJsonArray("artifacts")) {
                JsonObject artifact = entry.getAsJsonObject();
                String name = artifact.has("name") ? artifact.get("name").getAsString() : "";
                if ("lunar.jar".equals(name)) return artifact.get("url").getAsString();
            }
        }
        Logger.log(LogLevel.WARN, true, "lunar.jar not found in launch API response, using fallback", new Object[0]);
        return null;
    }

    private static String operatingSystem() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) return "darwin";
        if (os.contains("win")) return "win32";
        return "linux";
    }

    private static String architecture() {
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return arch.contains("aarch64") || arch.contains("arm64") ? "arm64" : "x64";
    }

    private byte[] readRange(String url, long first, long last) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "LunarClient/3.3.3");
        connection.setRequestProperty("Range", "bytes=" + first + "-" + last);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.connect();
        try (InputStream input = connection.getInputStream()) {
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int length;
            while ((length = input.read(buffer)) != -1) output.write(buffer, 0, length);
            return output.toByteArray();
        }
    }
}

