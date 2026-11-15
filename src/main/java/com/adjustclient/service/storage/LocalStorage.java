package com.adjustclient.service.storage;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class LocalStorage {
    public byte[] key;
    public byte[] iv;
    public File directory;
    public List<Runnable> pendingOperations = new ArrayList<>();
    public Object field20;
    public static final LocalStorage INSTANCE;

    static { INSTANCE = new LocalStorage(); }

    public LocalStorage() {
        directory = new File(resolveStoragePath());
        ensureDirectory(directory);
        initOfflineKey();
    }

    /** Initializes local AES/CTR key material without calling {@link Logger}, so persistence works offline from startup. */
    private void initOfflineKey() {
        if (key == null) {
            byte[] derived = new byte[16];
            try {
                byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                        .digest("adjust-local-storage-v1".getBytes(StandardCharsets.UTF_8));
                System.arraycopy(digest, 0, derived, 0, 16);
            } catch (java.security.NoSuchAlgorithmException fallback) {
                int state = 0xCAFF31E;
                ByteBuffer buffer = ByteBuffer.wrap(derived);
                for (int i = 0; i < 4; i++) {
                    buffer.putInt(i * 4, state);
                    state = state * 31 + 0x9E3779B9;
                }
            }
            iv = new byte[16];
            ByteBuffer.wrap(iv).putInt(0, 65).putInt(4, 100).putInt(8, 106).putInt(12, 117);
            key = derived;
        }
        pendingOperations = null;
    }

    /** Ensures {@code dir} exists without triggering {@link Logger} static initialization before it is ready. */
    private static void ensureDirectory(File dir) {
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            System.err.println("Could not create local storage directory: " + dir.getAbsolutePath());
        }
    }

    public void runKeyQueue(int seed) {
        byte[] derived = new byte[16];
        int delta = Math.abs(seed >> 15);
        int state = (seed >> 12) ^ 0xCAFF31E;
        ByteBuffer buffer = ByteBuffer.wrap(derived);
        for (int i = 0; i < 4; i++) {
            int hash = com.adjustclient.util.HashUtil.hash(i + "+" + seed + "_" + i);
            buffer.putInt(i, hash ^ state);
            state = i + delta + (state ^ (97 * i));
        }
        if (key != null) {
            if (!Arrays.equals(key, derived)) {
                Logger.log(LogLevel.ERROR, true, "Internal data corruption. Local storage encryption key mismatch.", new Object[0]);
            }
            return;
        }
        iv = new byte[16];
        ByteBuffer.wrap(iv).putInt(0, 65).putInt(4, 100).putInt(8, 106).putInt(12, 117);
        key = derived;
        List<Runnable> queue = pendingOperations;
        if (queue == null) return;
        pendingOperations = null;
        for (Runnable task : queue) {
            try {
                task.run();
            } catch (Exception exception) {
                Logger.log(LogLevel.ERROR, true, "Failed to run local storage key queue.", new Object[]{exception});
            }
        }
    }

    @SuppressWarnings("unused")
    private void vendorRunKeyQueue(int seed) {
        byte[] derived = new byte[16];
        int delta = Math.abs(seed >> 15);
        int state = (seed >> 12) ^ 0xCAFF31E;
        ByteBuffer buffer = ByteBuffer.wrap(derived);
        for (int i = 0; i < 4; i++) {
            int hash = com.adjustclient.util.HashUtil.hash(i + "+" + seed + "_" + i);
            buffer.putInt(i, hash ^ state);
            state = i + delta + (state ^ (97 * i));
        }
        if (key != null) {
            if (!Arrays.equals(key, derived)) {
                Logger.log(LogLevel.ERROR, true, "Internal data corruption. Local storage encryption key mismatch.", new Object[0]);
                System.exit(0);
            }
            return;
        }
        iv = new byte[16];
        ByteBuffer.wrap(iv).putInt(0, 65).putInt(4, 100).putInt(8, 106).putInt(12, 117);
        key = derived;
        List<Runnable> queue = pendingOperations;
        if (queue == null) return;
        pendingOperations = null;
        for (Runnable task : queue) {
            try {
                task.run();
            } catch (Exception exception) {
                Logger.log(LogLevel.ERROR, true, "Failed to run local storage key queue.", new Object[]{exception});
            }
        }
    }

    public void saveNow(String name, byte[] data) {
        try {
            File target = new File(directory, name);
            ensureDirectory(target.getParentFile());
            Files.write(crypt(name, data), target);
        } catch (Exception exception) {
            Logger.log(LogLevel.WARN, true, "Failed to save local storage file {}! Reason: {}", new Object[]{name, exception});
        }
    }

    /** Reads and decrypts {@code name} synchronously from local storage, returning {@code null} when absent or unreadable. */
    public byte[] readNow(String name) {
        if (name == null) return null;
        File file = new File(directory, name);
        if (!file.isFile()) return null;
        try {
            return crypt(name, Files.toByteArray(file));
        } catch (Exception exception) {
            Logger.log(LogLevel.WARN, true, "Failed to read local storage file {}! Reason: {}", new Object[]{name, exception});
            return null;
        }
    }

    /** Deletes {@code name} from local storage when present. */
    public boolean deleteNow(String name) {
        if (name == null) return false;
        File file = new File(directory, name);
        return !file.exists() || file.delete();
    }

    public static String resolveStoragePath() {
        String home = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return home + "\\AppData\\Roaming\\.adjustclient";
        if (os.contains("mac")) return home + "/Library/Application Support/.adjustclient";
        if (os.contains("nix") || os.contains("nux")) return home + "/.adjustclient";
        return home + "/.adjustclient";
    }

    public byte[] crypt(String name, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception exception) {
            Logger.log(LogLevel.WARN, true, "Failed to decrypt/encrypt local storage file {}! Reason: {}", new Object[]{name, exception});
            return new byte[0];
        }
    }

    public void save(String name, byte[] data) {
        if (pendingOperations == null) saveNow(name, data);
        else pendingOperations.add(() -> {
            saveNow(name, data);
        });
    }

    public void read(String name, Consumer<byte[]> callback) throws IOException {
        if (pendingOperations == null) {
            File file = new File(directory, name);
            if (!file.isFile()) {
                callback.accept(new byte[0]);
                return;
            }
            callback.accept(crypt(name, Files.toByteArray(file)));
        } else {
            pendingOperations.add(() -> {
                try {
                    File file = new File(directory, name);
                    if (!file.isFile()) {
                        callback.accept(new byte[0]);
                        return;
                    }
                    callback.accept(crypt(name, Files.toByteArray(file)));
                } catch (IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            });
        }
    }
}
