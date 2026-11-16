package com.adjustclient.ui.account;

import com.adjustclient.Adjust;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.account.bulk.check.CheckEntry;
import com.adjustclient.ui.account.importer.BulkImportScreen;
import com.adjustclient.ui.account.importer.ImportMode;
import com.adjustclient.ui.account.login.AddAccountScreen;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.MinecraftInstance;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWDropCallbackI;

public final class AccountFileDropHandler implements MinecraftInstance, GLFWDropCallbackI {
    public static final AccountFileDropHandler INSTANCE = new AccountFileDropHandler();

    private List<DroppedCookieFile> readZip(File file) {
        List<DroppedCookieFile> result = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    byte[] data = zip.readAllBytes();
                    if (isCookieFile(data)) {
                        result.add(new DroppedCookieFile(entry.getName(), data, file.getParentFile()));
                    }
                }
                zip.closeEntry();
            }
            return result;
        } catch (Exception e) {
            Logger.log(LogLevel.WARN, true, "Failed to unzip file '{}': {}", file.getAbsoluteFile(), e);
            return null;
        }
    }

    private boolean isCookieFile(byte[] data) {
        if (data.length < 256) {
            return false;
        }
        String text = new String(data, StandardCharsets.UTF_8);
        return text.contains("live.com") && text.contains("MS");
    }

    private boolean isZip(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return raf.readInt() == 0x504B0304;
        } catch (IOException e) {
            return false;
        }
    }

    public List<DroppedCookieFile> readFiles(List<File> files) {
        List<DroppedCookieFile> result = new ArrayList<>();
        for (File file : files) {
            if (isZip(file)) {
                List<DroppedCookieFile> unzipped = readZip(file);
                result.addAll(Objects.requireNonNullElse(unzipped, Collections.emptyList()));
            } else {
                try {
                    byte[] data = Files.readAllBytes(file.toPath());
                    if (isCookieFile(data)) {
                        result.add(new DroppedCookieFile(file.getName(), data, file.getParentFile()));
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return result;
    }

    public void importFiles(List<File> files) {
        List<DroppedCookieFile> dropped = readFiles(files);
        if (dropped.isEmpty()) {
            return;
        }
        Runnable task = () -> {
            if (Minecraft.getMinecraft().currentScreen instanceof BulkImportScreen) {
                showImport(dropped);
                return;
            }
            if (Minecraft.getMinecraft().currentScreen instanceof AddAccountScreen
                    || Minecraft.getMinecraft().currentScreen instanceof AccountManagerScreen) {
                if (dropped.size() == 1) {
                    AddAccountScreen addScreen = Adjust.INSTANCE.accountManager.addAccountScreen;
                    if (mc.currentScreen != addScreen) {
                        mc.displayGuiScreen(addScreen);
                    }
                    addScreen.selectCookieFile(dropped.get(0));
                } else {
                    showImport(dropped);
                }
            }
        };
        if (mc.isCallingFromMinecraftThread()) {
            task.run();
        } else {
            mc.addScheduledTask(task);
        }
    }

    private void showImport(List<DroppedCookieFile> files) {
        BulkImportScreen screen;
        if (mc.currentScreen instanceof BulkImportScreen current) {
            screen = current;
        } else {
            screen = new BulkImportScreen(mc.currentScreen, ImportMode.COOKIE);
            mc.displayGuiScreen(screen);
        }
        for (DroppedCookieFile file : files) {
            try {
                screen.uncheckedList.entries.add(new CheckEntry(file.name, new String(file.contents, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                NotificationBuilder toast = new NotificationBuilder("Bulk Import");
                toast.format("Failed to add account '{}'", file.name);
                toast.show();
            }
        }
        NotificationBuilder toast = new NotificationBuilder("Bulk Import");
        toast.format("Imported {} accounts.", files.size());
        toast.show();
    }

    @Override
    public void invoke(long window, int count, long names) {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            File file = new File(GLFWDropCallback.getName(names, i));
            if (file.exists()) {
                files.add(file);
            }
        }
        importFiles(files);
    }
}
