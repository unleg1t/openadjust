package com.adjustclient.ui.account.importer;

import com.adjustclient.Adjust;
import com.adjustclient.auth.LoginResult;
import com.adjustclient.auth.LoginStatus;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountFileDropHandler;
import com.adjustclient.ui.account.LocalAccountStore;
import com.adjustclient.ui.account.repository.AccountRepository;
import com.adjustclient.ui.account.login.AddAccountScreen;
import com.adjustclient.ui.account.login.auth.MicrosoftCookieAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftCredentialAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftRefreshTokenAccount;
import com.adjustclient.util.ClipboardUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class BulkImportScreen extends GuiScreen {
    private final GuiScreen parent;
    private ImportMode mode;
    private final AddAccountScreen addScreen;
    private volatile boolean busy;
    private volatile int imported;
    private volatile int failed;
    private volatile String message = "";

    public BulkImportScreen(GuiScreen parent, ImportMode mode, AddAccountScreen addScreen) {
        this.parent = parent;
        this.mode = mode == null ? ImportMode.COOKIE : mode;
        this.addScreen = addScreen;
    }

    @Override public void updateScreen() {
        for (GuiButton button : buttonList) button.enabled = !busy;
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (!button.enabled || busy) return;
        if (button.id == 1) mc.displayGuiScreen(parent);
        else if (button.id == 4) {
            mode = ImportMode.values()[(mode.ordinal() + 1) % ImportMode.values().length];
            initGui();
        } else if (button.id == 2) {
            if (mode == ImportMode.COOKIE) openFiles();
            else importClipboard();
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 && !busy) mc.displayGuiScreen(parent);
    }

    @Override protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Bulk Import: " + mode.displayName, width / 2, 17, 0xFFFFFF);
        String hint = mode == ImportMode.COOKIE ? "Select cookie files or .zip archive"
                : mode == ImportMode.MICROSOFT ? "john.doe@example.org:password" : "username:token";
        drawCenteredString(fontRendererObj, hint, width / 2, 48, 0xA0A0A0);
        if (mode == ImportMode.COOKIE) drawCenteredString(fontRendererObj, "(zip files should contain cookie files)", width / 2, 62, 0xA0A0A0);
        drawCenteredString(fontRendererObj, "Imported " + imported + " accounts.", width / 2, 86, 0xFFFFFF);
        if (failed != 0) drawCenteredString(fontRendererObj, "Failed to import " + failed + " accounts.", width / 2, 100, 0xBE3232);
        drawCenteredString(fontRendererObj, message, width / 2, 114, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(4, width / 2 - 100, height - 80, 200, 20, mode.displayName));
        buttonList.add(new GuiButton(2, width / 2 - 100, height - 56, 200, 20,
                mode == ImportMode.COOKIE ? "File/Zip Import" : "Clipboard Import"));
        buttonList.add(new GuiButton(1, width / 2 - 100, height - 32, 200, 20, "Back"));
    }

    private void importAccounts(List<AccountEntry> accounts) {
        if (busy) return;
        AccountRepository repository = Adjust.INSTANCE.accountManager.currentRepository;
        if (repository == null) { message = "No repository selected"; busy = false; return; }
        busy = true;
        AccountEntry.EXECUTOR.execute(() -> {
            for (AccountEntry account : accounts) {
                try {
                    LoginResult result = account.login();
                    if (result.status != LoginStatus.SUCCESS || result.session == null) {
                        failed++;
                        continue;
                    }
                    account.username = result.session.getUsername();
                    account.token = result.session.getToken();
                    account.setUuid(result.session.getPlayerID());
                    // Serialize before publishing; retain the refreshed login credentials.
                    account.serialize();
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        try { LocalAccountStore.INSTANCE.add(repository, account); imported++; }
                        catch (RuntimeException error) { failed++; message = "Failed to save imported account"; }
                    }).get();
                } catch (Exception error) { failed++; }
            }
            message = "Finished importing " + imported + " alts";
            busy = false;
        });
    }

    private void importClipboard() {
        String clipboard = ClipboardUtil.getClipboard();
        ImportMode inputMode = mode;
        busy = true;
        AccountEntry.EXECUTOR.execute(() -> {
            try {
                List<AccountEntry> accounts = new ArrayList<>();
                for (String line : clipboard.split("\\R")) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    Pair<String, String> pair = ClipboardUtil.splitPair(line);
                    if (inputMode == ImportMode.MICROSOFT) {
                        if (pair != null) accounts.add(new MicrosoftCredentialAccount(pair.getLeft(), pair.getRight(), false));
                        else failed++;
                    } else {
                        accounts.add(new MicrosoftRefreshTokenAccount(pair == null ? line : pair.getRight(), true, false));
                    }
                }
                Minecraft.getMinecraft().addScheduledTask(() -> { busy = false; importAccounts(accounts); });
            } catch (Exception error) {
                message = "Failed to read clipboard";
                busy = false;
            }
        });
    }

    private void openFiles() {
        busy = true;
        AccountEntry.EXECUTOR.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(2);
                filters.put(stack.UTF8("*.txt")).put(stack.UTF8("*.zip")).flip();
                String selected = TinyFileDialogs.tinyfd_openFileDialog("Open Cookie Files", System.getProperty("user.home"),
                        filters, "Cookie Files (*.txt;*.zip)", true);
                if (selected == null) { busy = false; return; }
                List<File> files = new ArrayList<>();
                for (String path : selected.split("\\|")) files.add(new File(path));
                List<AccountEntry> accounts = new ArrayList<>();
                for (byte[] data : AccountFileDropHandler.INSTANCE.readFiles(files))
                    accounts.add(new MicrosoftCookieAccount(new String(data, StandardCharsets.UTF_8), false));
                Minecraft.getMinecraft().addScheduledTask(() -> { busy = false; importAccounts(accounts); });
            } catch (Exception error) { message = "Error importing files"; busy = false; }
        });
    }

    public void importCookieData(List<byte[]> files) {
        List<AccountEntry> accounts = new ArrayList<>();
        for (byte[] bytes : files) accounts.add(new MicrosoftCookieAccount(new String(bytes, StandardCharsets.UTF_8), false));
        importAccounts(accounts);
    }

    @Override public void handleMouseInput() throws IOException { super.handleMouseInput(); }

    public int getMaxFps() { return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate); }
}
