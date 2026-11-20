package com.adjustclient.core.gui.account.importer;

import com.adjustclient.Adjust;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.gui.account.AccountEntry;
import com.adjustclient.core.gui.account.AccountFileDropHandler;
import com.adjustclient.core.gui.account.DragDropOverlay;
import com.adjustclient.core.gui.account.DroppedCookieFile;
import com.adjustclient.core.gui.account.bulk.check.BulkCheckThread;
import com.adjustclient.core.gui.account.bulk.check.CheckEntry;
import com.adjustclient.core.gui.account.bulk.check.CheckList;
import com.adjustclient.core.gui.account.login.AddAccountScreen;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.core.gui.mainmenu.AdjustMainMenu;
import com.adjustclient.utilities.ClipboardUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class BulkImportScreen extends GuiScreen {
    public final GuiScreen parent;
    public CheckList uncheckedList;
    public CheckList checkedList;
    private GuiOptionButton importButton;
    public BulkCheckThread checkThread;
    private GuiOptionButton checkButton;
    private GuiOptionButton backButton;
    public final ImportMode mode;
    private final AddAccountScreen addScreen;

    public BulkImportScreen(GuiScreen parent, ImportMode mode) {
        this(parent, mode, Adjust.INSTANCE.accountManager.addAccountScreen);
    }

    public BulkImportScreen(GuiScreen parent, ImportMode mode, AddAccountScreen addScreen) {
        this.parent = parent;
        this.mode = mode == null ? ImportMode.COOKIE : mode;
        this.addScreen = addScreen != null ? addScreen : Adjust.INSTANCE.accountManager.addAccountScreen;
    }

    @Override
    public void updateScreen() {
        boolean idle = this.checkThread == null;
        if (!idle) {
            this.importButton.enabled = false;
            this.checkButton.displayString = "Stop Checking";
            this.backButton.enabled = false;
            this.checkButton.enabled = !this.checkThread.stopped || !this.uncheckedList.entries.isEmpty();
        } else {
            this.importButton.enabled = !this.addScreen.fileDialogOpen;
            this.checkButton.displayString = "Start Checking";
            this.backButton.enabled = true;
            this.checkButton.enabled = !this.uncheckedList.entries.isEmpty();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        switch (button.id) {
            case 1:
                if (this.checkThread == null) {
                    this.mc.displayGuiScreen(this.parent);
                }
                break;
            case 2:
                if (this.mode == ImportMode.MICROSOFT || this.mode == ImportMode.REFRESH) {
                    importClipboard();
                } else {
                    this.addScreen.fileDialogOpen = true;
                    AccountEntry.EXECUTOR.execute(this::openFiles);
                }
                break;
            case 3:
                if (this.checkThread != null) {
                    this.checkThread.stopChecking();
                } else {
                    importAccounts();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE && this.checkThread == null) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.uncheckedList.mouseClicked(mouseX, mouseY, mouseButton);
        this.checkedList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.theWorld != null) {
            Gui.drawRect(0, 0, this.width, this.height, 0xC0101010);
        } else {
            this.drawBackground(0);
        }
        this.uncheckedList.drawScreen(mouseX, mouseY, partialTicks);
        this.checkedList.drawScreen(mouseX, mouseY, partialTicks);

        String title = "Bulk Import: " + ChatColor.GRAY + this.mode.displayName;
        float titleWidth = this.fontRendererObj.getStringWidth(title, 1.0F);
        this.fontRendererObj.drawStringWithShadow(title, (float) (this.width / 2) - (float) (int) titleWidth / 2.0F, 16.0F, 0xFFFFFF);

        DragDropOverlay.INSTANCE.render();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.importButton.isMouseOver() && this.importButton.enabled) {
            switch (this.mode) {
                case REFRESH:
                    this.drawHoveringText(
                            List.of(ChatColor.WHITE + "username" + ChatColor.GRAY + ":" + ChatColor.WHITE + "token"),
                            mouseX,
                            mouseY
                    );
                    break;
                case MICROSOFT:
                    this.drawHoveringText(
                            List.of(ChatColor.WHITE + "john.doe@example.org" + ChatColor.GRAY + ":" + ChatColor.WHITE + "password"),
                            mouseX,
                            mouseY
                    );
                    break;
                case COOKIE:
                    this.drawHoveringText(
                            List.of(
                                    ChatColor.WHITE + "Select cookie files or .zip archive",
                                    ChatColor.GRAY + "(zip files should contain cookie files)"
                            ),
                            mouseX,
                            mouseY
                    );
                    break;
            }
        }
    }

    @Override
    public void initGui() {
        if (this.uncheckedList == null) {
            this.uncheckedList = new CheckList(Minecraft.getMinecraft(), 200, this.height, 32, this.height - 51, 26, "Unchecked Accounts");
            this.uncheckedList.registerScrollButtons(7, 8);
        }
        this.uncheckedList.setDimensions(200, this.height, 32, this.height - 51);
        this.uncheckedList.left = this.width / 2 - 204;
        this.uncheckedList.right = this.width / 2 - 4;

        if (this.checkedList == null) {
            this.checkedList = new CheckList(Minecraft.getMinecraft(), 200, this.height, 32, this.height - 51, 26, "Checked Accounts");
            this.checkedList.registerScrollButtons(7, 8);
        }
        this.checkedList.setDimensions(200, this.height, 32, this.height - 51);
        this.checkedList.left = this.width / 2 + 4;
        this.checkedList.right = this.width / 2 + 204;

        String importLabel = this.mode == ImportMode.COOKIE ? "File/Zip Import" : "Clipboard Import";
        this.importButton = new GuiOptionButton(2, this.width / 2 - 204, this.height - 48, 96, 20, importLabel);
        this.buttonList.add(this.importButton);

        this.checkButton = new GuiOptionButton(3, this.width / 2 - 100, this.height - 48, 96, 20, "");
        this.buttonList.add(this.checkButton);

        this.backButton = new GuiOptionButton(1, this.width / 2 + 4, this.height - 48, 200, 20, "Back");
        this.buttonList.add(this.backButton);

        updateScreen();
    }

    private void importAccounts() {
        this.checkThread = new BulkCheckThread(this);
        this.checkThread.start();
    }

    private void importClipboard() {
        try {
            String clipboard = ClipboardUtil.getClipboard();
            for (String line : clipboard.split("\n")) {
                Pair<String, String> pair = ClipboardUtil.splitPair(line);
                if (pair != null) {
                    this.uncheckedList.entries.add(new CheckEntry(pair.getLeft(), pair.getRight()));
                }
            }
        } catch (Exception e) {
            Logger.log(LogLevel.WARN, true, "Failed to read clipboard", e);
        }
    }

    private void openFiles() {
        try {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(2);
                filters.put(stack.UTF8("*.txt"));
                filters.put(stack.UTF8("*.zip"));
                filters.flip();

                String startPath = this.addScreen.selectedFilePath != null
                        ? this.addScreen.selectedFilePath
                        : System.getProperty("user.home");
                String selected = TinyFileDialogs.tinyfd_openFileDialog(
                        "Open Cookie Files",
                        startPath,
                        filters,
                        "Cookie Files (*.txt;*.zip)",
                        true
                );
                if (selected != null) {
                    String[] paths = selected.split("\\|");
                    if (paths.length == 0) {
                        this.addScreen.fileDialogOpen = false;
                        return;
                    }
                    File first = new File(paths[0]);
                    if (first.exists()) {
                        this.addScreen.selectedFilePath = first.getParent();
                    }
                    List<File> files = new ArrayList<>();
                    for (String path : paths) {
                        File file = new File(path);
                        if (file.exists() && !file.isDirectory()) {
                            files.add(file);
                        }
                    }
                    List<DroppedCookieFile> droppedFiles = AccountFileDropHandler.INSTANCE.readFiles(files);
                    for (DroppedCookieFile dropped : droppedFiles) {
                        this.uncheckedList.entries.add(new CheckEntry(dropped.name, new String(dropped.contents, StandardCharsets.UTF_8)));
                    }
                } else {
                    this.addScreen.fileDialogOpen = false;
                }
            }
        } catch (Exception e) {
            NotificationBuilder toast = new NotificationBuilder("Bulk Import");
            toast.format("Error importing files");
            toast.lifetimeSteps = 144;
            toast.show();
            System.out.format("Error importing files: " + e.getMessage());
        } finally {
            this.addScreen.fileDialogOpen = false;
            NotificationBuilder toast = new NotificationBuilder("Bulk Import");
            toast.format("Finished importing {} alts", this.uncheckedList.entries.size());
            toast.lifetimeSteps = 144;
            toast.show();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.uncheckedList.handleMouseInput();
        this.checkedList.handleMouseInput();
    }

    public int getMaxFps() {
        if (!AdjustMainMenu.windowActive) {
            return 60;
        }
        return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate);
    }
}
