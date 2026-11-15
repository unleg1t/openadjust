package com.adjustclient.ui.account;

import com.adjustclient.Adjust;
import com.adjustclient.auth.CookieFileParser;
import com.adjustclient.ui.account.importer.BulkImportScreen;
import com.adjustclient.ui.account.importer.ImportMode;
import com.adjustclient.ui.account.login.AddAccountScreen;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWDropCallbackI;

public final class AccountFileDropHandler implements GLFWDropCallbackI {
    public static final AccountFileDropHandler INSTANCE = new AccountFileDropHandler();

    private List<byte[]> readZip(File file) throws IOException {
        List<byte[]> result = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                if (!entry.isDirectory()) {
                    byte[] data = zip.readAllBytes();
                    if (isCookieFile(data)) result.add(data);
                }
                zip.closeEntry();
            }
        }
        return result;
    }

    private boolean isCookieFile(byte[] data) {
        if (data.length < 256) return false;
        String text = new String(data, StandardCharsets.UTF_8);
        return text.contains("live.com") && text.contains("MS");
    }

    public void importFiles(List<File> files) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!(screen instanceof AccountManagerScreen || screen instanceof AddAccountScreen || screen instanceof BulkImportScreen)) return;
        AccountEntry.EXECUTOR.execute(() -> {
            try {
                List<byte[]> data = readFiles(files);
                Minecraft.getMinecraft().addScheduledTask(() -> showImport(screen, files, data));
            } catch (IOException error) {
                error.printStackTrace();
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    var toast = new com.adjustclient.ui.hud.notification.NotificationBuilder("Bulk Import");
                    toast.format("Error importing files", new Object[0]);
                    toast.show();
                });
            }
        });
    }

    private boolean isZip(File file) throws IOException {
        try (DataInputStream input = new DataInputStream(Files.newInputStream(file.toPath()))) {
            if (file.length() < 4) return false;
            int magic = input.readInt();
            return magic == 0x504B0304 || magic == 0x504B0506 || magic == 0x504B0708;
        }
    }

    public List<byte[]> readFiles(List<File> files) throws IOException {
        List<byte[]> result = new ArrayList<>();
        for (File file : files) {
            if (!file.isFile()) continue;
            if (isZip(file)) result.addAll(readZip(file));
            else {
                byte[] data = Files.readAllBytes(file.toPath());
                if (isCookieFile(data)) result.add(data);
            }
        }
        return result;
    }

    private void showImport(GuiScreen previous, List<File> files, List<byte[]> data) {
        Minecraft game = Minecraft.getMinecraft();
        if (data.isEmpty() || game.currentScreen != previous) return;
        if (!(previous instanceof BulkImportScreen) && data.size() == 1 && files.size() == 1
                && !files.get(0).getName().toLowerCase(Locale.ROOT).endsWith(".zip")) {
            File file = files.get(0);
            AddAccountScreen add = previous instanceof AddAccountScreen ? (AddAccountScreen) previous
                    : Adjust.INSTANCE.accountManager.addAccountScreen;
            if (game.currentScreen != add) game.displayGuiScreen(add);
            add.selectCookieFile(new DroppedCookieFile(file.getName(), data.get(0), file.getParentFile()));
            return;
        }
        BulkImportScreen importer;
        if (previous instanceof BulkImportScreen) importer = (BulkImportScreen) previous;
        else {
            importer = new BulkImportScreen(previous, ImportMode.COOKIE, null);
            game.displayGuiScreen(importer);
        }
        importer.importCookieData(data);
    }

    @Override public void invoke(long window, int count, long names) {
        List<File> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            File file = new File(GLFWDropCallback.getName(names, i));
            if (file.exists()) files.add(file);
        }
        importFiles(files);
    }
}
