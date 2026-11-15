package com.adjustclient.ui.account.login;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatColor;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import com.adjustclient.auth.LoginResult;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.account.login.auth.MicrosoftCookieAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftCredentialAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftRefreshTokenAccount;
import com.adjustclient.ui.account.login.auth.SessionTokenAccount;
import com.adjustclient.util.BrowserUtil;
import com.adjustclient.util.ClipboardUtil;
import com.adjustclient.auth.RandomNames;
import com.adjustclient.util.Timer;
import com.adjustclient.Adjust;
import com.adjustclient.backend.api.BackendRequest;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.error.AccountLoginError;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.EmptyResponseBody;
import com.adjustclient.backend.api.response.EnumResponseBody;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountType;
import com.adjustclient.ui.account.AddAccountType;
import com.adjustclient.ui.account.DragDropOverlay;
import com.adjustclient.ui.account.MaskedTextField;
import com.adjustclient.ui.account.view.CreateProfileScreen;

public final class AddAccountScreen extends GuiScreen {

    private boolean needsRebuild;
    private boolean lastManagerFlag;
    private boolean busy;
    private MaskedTextField textField64;
    private GuiTextField textField68;
    private MaskedTextField textField72;
    private MaskedTextField textField76;
    private GuiButton typeButton;
    private GuiButton primaryActionButton;
    private GuiButton secondaryActionButton;
    private GuiButton doneButton;
    private GuiButton cancelButton;
    private String statusMessage;
    private Timer statusTimer;
    private String selectedFilePath;
    private com.adjustclient.ui.account.importer.ImportMode bulkImportMode;
    private AddAccountType accountType;
    private String browserRefreshToken;
    private com.adjustclient.ui.account.DroppedCookieFile selectedFile;
    private boolean fileDialogOpen;
    private Object field132;

    /** Tick: expires the status message, refreshes button states / layout, blinks the text fields, shows the cookie file name. */
    @Override
    public void updateScreen() {
        if (this.statusTimer != null) {
            if (this.statusTimer.lastMs + 4000L <= System.currentTimeMillis()) {
                this.statusTimer = null;
                this.statusMessage = null;
            }
        }
        updateButtons();
        boolean managerFlag = true;
        if (managerFlag != this.lastManagerFlag) {
            this.lastManagerFlag = managerFlag;
            this.needsRebuild = true;
        }
        if (this.needsRebuild) {
            buildButtons();
            this.needsRebuild = false;
        }
        this.textField64.updateCursorCounter();
        this.textField68.updateCursorCounter();
        this.textField72.updateCursorCounter();
        this.textField76.updateCursorCounter();
        if (this.accountType != AddAccountType.COOKIE /* cookie file */) {
            return;
        }
        if (this.selectedFile == null) {
            this.textField68.setText("Select file...");
        } else if (StreamerModule.nameMap != null && StreamerModule.INSTANCE.isEnabled()) {
            this.textField68.setText("Hidden...");
        } else {
            this.textField68.setText(this.selectedFile.name);
        }
        this.textField68.setFocused(false);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        MicrosoftRefreshTokenAccount.stopServer();
    }

    private void loginAndAdd(AccountEntry account) {
        try {
            LoginResult result = account.login();
            switch (result.status) {
                case SUCCESS:
                    account.username = result.session.getUsername();
                    account.token = result.session.getToken();
                    account.setUuid(result.session.getPlayerID());
                    addToRepository(account, Adjust.INSTANCE.accountManager.currentRepository);
                    updateButtons();
                    break;
                case NO_PROFILE:
                    account.token = result.session.getToken();
                    this.mc.addScheduledTask(() -> this.mc.displayGuiScreen(new CreateProfileScreen(this, account)));
                    this.busy = false;
                    break;
                default:
                    this.statusMessage = result.status.message;
                    this.statusTimer = new Timer();
                    this.busy = false;
                    break;
            }
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, true, "Failed to login into account {}: {}", new Object[] { account.username, e });
            this.statusMessage = "An error occurred while logging into the account.";
            this.statusTimer = new Timer();
            this.busy = false;
        }
    }

    /** A cookie file was chosen (file drop / dialog): switch to the cookie account type and remember the file. */
    public void selectCookieFile(com.adjustclient.ui.account.DroppedCookieFile file) {
        setAccountType(AddAccountType.COOKIE);
        this.selectedFile = file;
        this.selectedFilePath = new File(file.directory, file.name).getAbsolutePath();
    }

    /** Selects an account type: remembers it globally, updates the button label and clears all input fields. */
    private void setAccountType(AddAccountType type) {
        this.accountType = type == null ? AddAccountType.MICROSOFT : type;
        UserConfiguration.INSTANCE.addAccountType = this.accountType;
        if (this.typeButton != null) {
            this.typeButton.displayString = "Account type: " + ChatColor.GRAY + this.accountType.displayName + ChatColor.RESET;
        }
        this.textField68.setText("");
        this.textField64.setText("");
        this.textField72.setText("");
        this.textField76.setText("");
        this.needsRebuild = true;
    }

    /** Button handler (ids: 0 Done, 1 Cancel, 2 account type, 3 select file / open link, 4 clipboard import / copy link, 5 Random, 6 Bulk Import...). */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        if (button.id != 4) {
            if (button.id == 5) {
                this.textField68.setText(RandomNames.randomUsername());
            } else if (this.typeButton == button) {
                cycleAccountType();
            } else if (button.id >= 3) {
                if (button.id == 3) {
                    if (this.accountType == AddAccountType.COOKIE /* cookie file */) {
                        this.fileDialogOpen = true;
                        AccountEntry.EXECUTOR.execute(this::openFileDialog);
                    } else {
                        String url = MicrosoftRefreshTokenAccount.startServer(token -> {
                            this.browserRefreshToken = token;
                            if (!this.busy) {
                                submit();
                            }
                            this.statusMessage = ChatColor.GREEN + "Received refresh token from browser!";
                            this.statusTimer = new Timer();
                        });
                        if (url != null) {
                            BrowserUtil.openUrl(url);
                        }
                    }
                } else if (button.id == 6) {
                    this.mc.displayGuiScreen(new com.adjustclient.ui.account.importer.BulkImportScreen(
                            this, this.bulkImportMode, this));
                }
            } else if (button.id != 0) {
                if (button.id == 1) {
                    this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);   // back to the account manager
                }
            } else {
                submit();
            }
            return;
        }
        // id 4: "Clipboard Import" / "Copy Link to Clipboard"
        int typeOrdinal = this.accountType.ordinal();
        if (typeOrdinal == 2) {
            String url = MicrosoftRefreshTokenAccount.startServer(token -> {
                this.browserRefreshToken = token;
                if (!this.busy) {
                    submit();
                }
                this.statusMessage = ChatColor.GREEN + "Received refresh token from browser!";
                this.statusTimer = new Timer();
            });
            if (url != null) {
                ClipboardUtil.copyToClipboard(url);
            }
        } else if (typeOrdinal == 3) {
            String clipboard = readClipboard();
            String[] parts = ClipboardUtil.splitThreeFields(clipboard);
            if (parts == null) {
                this.textField68.setText(clipboard);
                this.textField64.setText(clipboard);
                this.textField72.setText(clipboard);
            } else {
                this.textField68.setText(parts[0]);
                this.textField64.setText(parts[1]);
                this.textField72.setText(parts[2]);
            }
        } else {
            Pair<String, String> pair = ClipboardUtil.splitPair(readClipboard());
            if (pair != null) {
                this.textField68.setText(pair.getLeft());
                this.textField64.setText(pair.getRight());
            }
        }
    }

    private String readClipboard() {
        return ClipboardUtil.getClipboard();
    }

    /** Key input: Escape cancels, other keys go to the text fields of the current account type. */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != 1) {
            switch (this.accountType.ordinal()) {
                case 0:
                    this.textField68.textboxKeyTyped(typedChar, keyCode);
                    this.textField64.textboxKeyTyped(typedChar, keyCode);
                    break;
                case 3:
                    this.textField68.textboxKeyTyped(typedChar, keyCode);
                    this.textField64.textboxKeyTyped(typedChar, keyCode);
                    this.textField72.textboxKeyTyped(typedChar, keyCode);
                    break;
                case 4:
                    this.textField76.textboxKeyTyped(typedChar, keyCode);
                    break;
                default:
                    break;
            }
        } else if (this.cancelButton.enabled) {
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
        }
    }

    public void addToRepository(AccountEntry account, Object repository) {
        if (repository == null) {
            this.statusMessage = "Internal Adjust error: repository unknown (please try again)";
            this.statusTimer = new Timer();
            this.busy = false;
            return;
        }
        try {
            byte[] serialized = account.i();
            java.util.Objects.requireNonNull(account.username, "Username is null");
            java.util.Objects.requireNonNull(account.token, "Session token is null");
            UUID uuid;
            if (account.uuid == null || account.uuid.isEmpty()) {
                if (account.token != null && !account.token.isEmpty()) {
                    this.statusMessage = "Player UUID is not valid.";
                    this.statusTimer = new Timer();
                    this.busy = false;
                    return;
                }
                uuid = null;
            } else {
                try {
                    uuid = UUIDTypeAdapter.fromString(account.uuid);
                } catch (IllegalArgumentException e) {
                    if (!account.uuid.isEmpty() || (account.token != null && !account.token.isEmpty())) {
                        this.statusMessage = "Player UUID is not valid.";
                        this.statusTimer = new Timer();
                        this.busy = false;
                        return;
                    }
                    uuid = null;
                }
            }
            this.mc.addScheduledTask(() -> {
                try {
                    com.adjustclient.ui.account.LocalAccountStore.INSTANCE.add(
                            (com.adjustclient.ui.account.repository.AccountRepository) repository, account);
                    this.busy = false;
                    if (this.mc.currentScreen == this) this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
                } catch (RuntimeException error) {
                    this.busy = false;
                    this.statusMessage = "Failed to save account: " + error.getMessage();
                    this.statusTimer = new Timer();
                }
            });
        } catch (RuntimeException e) {
            this.statusMessage = "Internal Adjust error: " + e.getMessage();
            this.statusTimer = new Timer();
            this.busy = false;
        }
    }

    private void onAddAccountResponse(BackendResponse response) {
        this.busy = false;
        if (response.status == ResponseStatus.OK /* ref 8013633: success */ && this.mc.currentScreen == this) {
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
            return;
        }
        Object error = response.body;
        if (error instanceof EnumResponseBody && ((EnumResponseBody) error).value instanceof AccountLoginError) {
            this.statusMessage = ((AccountLoginError) ((EnumResponseBody) error).value).field16;   // message of the error code constant
        } else {
            this.statusMessage = "Failed to add account. (are you online?)";
        }
        this.statusTimer = new Timer();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        switch (this.accountType.ordinal()) {
            case 0:
                this.textField68.mouseClicked(mouseX, mouseY, mouseButton);
                this.textField64.mouseClicked(mouseX, mouseY, mouseButton);
                break;
            case 3:
                this.textField68.mouseClicked(mouseX, mouseY, mouseButton);
                this.textField64.mouseClicked(mouseX, mouseY, mouseButton);
                this.textField72.mouseClicked(mouseX, mouseY, mouseButton);
                break;
            case 4:
                this.textField76.mouseClicked(mouseX, mouseY, mouseButton);
                break;
            default:
                break;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /** Draws the background, title, the labels and inputs of the current account type, the (fading) status message and the buttons. */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        String title = "Add account to repository";
        float alphaScale = 1.0F;
        if (this.mc.theWorld == null) {
            drawDefaultBackground();
        } else {
            Gui.drawRect(0, 0, this.width, this.height, 0xC0101010);
        }
        drawCenteredString(this.fontRendererObj, title, this.width / 2, 17, 0xFFFFFF);
        switch (this.accountType.ordinal()) {
            case 0:
                this.textField68.setEnabled(true);
                this.textField64.maskText = true;
                this.textField64.drawTextBox((float) mouseX, (float) mouseY);
                this.textField64.maskText = false;
                this.textField68.drawTextBox((float) mouseX, (float) mouseY);
                drawLabel("Email", 54.0F);
                drawLabel("Password", 94.0F);
                break;
            case 1:
                drawLabel("Cookie file", 54.0F);
                this.textField68.setEnabled(false);
                this.textField68.drawTextBox((float) mouseX, (float) mouseY);
                break;
            case 3:
                this.textField68.setEnabled(true);
                this.textField64.drawTextBox((float) mouseX, (float) mouseY);
                this.textField68.drawTextBox((float) mouseX, (float) mouseY);
                drawLabel("Session token", 134.0F);
                this.textField72.maskText = true;
                this.textField72.drawTextBox((float) mouseX, (float) mouseY);
                this.textField72.maskText = false;
                drawLabel("Username", 54.0F);
                drawLabel("UUID", 94.0F);
                break;
            case 4:
                drawLabel("Refresh token", 54.0F);
                this.textField76.drawTextBox((float) mouseX, (float) mouseY);
                break;
            default:
                break;
        }
        if (this.statusMessage != null) {
            long age = System.currentTimeMillis() - this.statusTimer.lastMs;
            if (this.statusTimer.lastMs + 3000L <= System.currentTimeMillis()) {
                float fade = (float) (int) (age - 3000L) / 1000.0F;
                if (alphaScale >= fade && (alphaScale - fade) > 0.05F) {
                    alphaScale -= fade;
                    GlStateManager.enableBlend();
                    int alpha = (int) (alphaScale * 255.0F);
                    drawCenteredString(this.fontRendererObj, this.statusMessage, this.width / 2, 30, new Color(190, 50, 50, alpha).getRGB());
                    GlStateManager.disableBlend();
                }
            } else {
                GlStateManager.enableBlend();
                drawCenteredString(this.fontRendererObj, this.statusMessage, this.width / 2, 30, new Color(190, 50, 50, 255).getRGB());
                GlStateManager.disableBlend();
            }
        }
        DragDropOverlay.INSTANCE.render();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLabel(String text, float y) {
        this.fontRendererObj.drawString(text, 0xA0A0A0 /* 10526880 */, true, (float) (this.width / 2 - 100), y, 1.0F);
    }

    /** Creates the four text fields, selects the remembered account type and builds the buttons. */
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.selectedFile = null;
        this.textField68 = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
        if (this.accountType != AddAccountType.COOKIE /* cookie file */) {
            this.textField68.setFocused(true);
        }
        this.textField68.setMaxStringLength(128);
        this.textField64 = new MaskedTextField(1, this.fontRendererObj, this.width / 2 - 100, 106, 200, 20);
        this.textField64.setMaxStringLength(256);
        this.textField72 = new MaskedTextField(2, this.fontRendererObj, this.width / 2 - 100, 146, 200, 20);
        this.textField72.setMaxStringLength(2048);
        this.textField76 = new MaskedTextField(3, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
        this.textField76.setMaxStringLength(2048);
        if (this.accountType != null) {
            this.statusMessage = null;
            this.statusTimer = null;
            buildButtons();
            return;
        }
        setAccountType(UserConfiguration.INSTANCE.addAccountType);     // the account type used last time
        this.statusMessage = null;
        this.statusTimer = null;
        buildButtons();
    }

    /** Enables / disables the buttons: Done needs valid input for the current account type, the others are off while a request is running. */
    public void updateButtons() {
        boolean doneEnabled;
        boolean othersEnabled;
        if (this.busy) {
            doneEnabled = false;
            othersEnabled = false;
        } else if (this.accountType == AddAccountType.COOKIE /* cookie file */) {
            othersEnabled = !this.fileDialogOpen;
            doneEnabled = this.selectedFile != null ? !this.fileDialogOpen : false;
        } else if (this.accountType == AddAccountType.SESSION /* session token */) {
            String username = this.textField68.getText();
            String uuid = this.textField64.getText();
            String token = this.textField72.getText();
            if (!username.isEmpty()) {
                doneEnabled = (token.isEmpty() == uuid.isEmpty());
            } else if (!uuid.isEmpty()) {
                doneEnabled = false;
            } else {
                doneEnabled = !token.isEmpty();
            }
            othersEnabled = true;
        } else if (this.accountType == AddAccountType.REFRESH /* refresh token */) {
            doneEnabled = !this.textField76.getText().isEmpty();
            othersEnabled = true;
        } else {
            // e-mail/password (and, with empty fields, browser login)
            if (this.textField64.getText().isEmpty()) {
                doneEnabled = false;
            } else {
                doneEnabled = !this.textField68.getText().isEmpty();
            }
            othersEnabled = true;
        }
        this.doneButton.enabled = doneEnabled;
        this.typeButton.enabled = othersEnabled;
        this.cancelButton.enabled = othersEnabled;
        if (this.primaryActionButton != null) {
            this.primaryActionButton.enabled = othersEnabled;
        }
        if (this.secondaryActionButton != null) {
            this.secondaryActionButton.enabled = othersEnabled;
        }
    }

    /** (Re)builds the button list for the current account type. */
    private void buildButtons() {
        this.primaryActionButton = null;
        this.secondaryActionButton = null;
        this.buttonList.clear();
        int top = this.height / 4;
        int center = this.width / 2;
        this.buttonList.add(this.doneButton = new GuiButton(0, center - 100, top + 134, 200, 20, "Done"));
        this.buttonList.add(this.cancelButton = new GuiButton(1, center - 100, top + 158, 200, 20, "Cancel"));
        this.buttonList.add(this.typeButton = new GuiButton(2, center - 100, top + 110, 200, 20, "Account type"));
        int ordinal = this.accountType.ordinal();
        if (ordinal < 3) {
            if (ordinal >= 2) {
                // browser login
                this.buttonList.add(this.primaryActionButton = new GuiButton(3, center - 100, 66, 200, 20, "Open Link in Browser"));
                this.buttonList.add(this.secondaryActionButton = new GuiButton(4, center - 100, 90, 200, 20, "Copy Link to Clipboard"));
            } else if (ordinal != 0) {
                if (ordinal == 1) {
                    // cookie file
                    this.buttonList.add(this.primaryActionButton = new GuiButton(3, center + 104, 66, 64, 20, "Select..."));
                    if (this.typeButton.yPosition >= 114) {
                        this.buttonList.add(new GuiButton(6, center - 100, 90, 200, 20, "Bulk Import..."));
                        this.bulkImportMode = com.adjustclient.ui.account.importer.ImportMode.COOKIE;
                    }
                }
            } else {
                // e-mail / password
                this.buttonList.add(new GuiButton(4, center - 100, 130, 200, 20, "Clipboard Import"));
                if (this.typeButton.yPosition >= 178) {
                    this.buttonList.add(new GuiButton(6, center - 100, 154, 200, 20, "Bulk Import..."));
                    this.bulkImportMode = com.adjustclient.ui.account.importer.ImportMode.MICROSOFT;
                }
            }
        } else if (ordinal == 3) {
            // session token
            this.buttonList.add(new GuiButton(4, center - 100, 170, 200, 20, "Clipboard Import"));
            this.buttonList.add(new GuiButton(5, center + 104, 66, 50, 20, "Random"));
        } else if (ordinal == 4) {
            this.buttonList.add(new GuiButton(6, center - 100, 90, 200, 20, "Bulk Import..."));
            this.bulkImportMode = com.adjustclient.ui.account.importer.ImportMode.REFRESH;
        }
        updateTypeButtonLabel();
        updateButtons();
    }

    /** Cycles to the next account type (5 types, wrapping). */
    private void cycleAccountType() {
        AddAccountType[] types = AddAccountType.values();
        setAccountType(types[(this.accountType.ordinal() + 1) % 5]);
    }

    /** Sets the label of the account type button: "Account type: " + colour + type name + colour. */
    private void updateTypeButtonLabel() {
        this.typeButton.displayString = "Account type: " + ChatColor.GRAY + this.accountType.displayName + ChatColor.RESET;
    }

    /**
     * "Done": checks the input for the current account type, creates the account object and starts the login on the worker thread.
     * Messages: "No repository selected (disconnected?).", "Invalid email.", "No refresh token?", "No cookie file selected.",
     * "No login method for account type. (?)".
     */
    private void submit() {
        if (Adjust.INSTANCE.accountManager.currentRepository == null) {      // no repository selected in the account manager
            this.statusMessage = "No repository selected (disconnected?).";
            this.statusTimer = new Timer();
            return;
        }
        AccountEntry account;
        if (this.accountType == AddAccountType.MICROSOFT /* e-mail + password */) {
            if (!Pattern.matches("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,}$", this.textField68.getText())) {
                this.statusMessage = "Invalid email.";
                this.statusTimer = new Timer();
                return;
            }
            account = new MicrosoftCredentialAccount(this.textField68.getText(), this.textField64.getText(), false);
        } else if (this.accountType == AddAccountType.BROWSER /* browser login */) {
            if (this.browserRefreshToken == null) {
                this.statusMessage = "No refresh token?";
                this.statusTimer = new Timer();
                return;
            }
            account = new MicrosoftRefreshTokenAccount(this.browserRefreshToken, false, false);
        } else if (this.accountType == AddAccountType.COOKIE /* cookie file */) {
            if (this.selectedFile == null) {
                this.statusMessage = "No cookie file selected.";
                this.statusTimer = new Timer();
                return;
            }
            account = new MicrosoftCookieAccount(new String(this.selectedFile.contents, StandardCharsets.UTF_8), false);
        } else if (this.accountType == AddAccountType.SESSION /* session token */) {
            account = new SessionTokenAccount(this.textField68.getText(), this.textField64.getText(), this.textField72.getText(), false);
        } else if (this.accountType == AddAccountType.REFRESH /* refresh token */) {
            account = new MicrosoftRefreshTokenAccount(this.textField76.getText(), true, false);
        } else {
            this.statusMessage = "No login method for account type. (?)";
            this.statusTimer = new Timer();
            return;
        }
        this.busy = true;
        this.browserRefreshToken = null;
        this.doneButton.enabled = false;
        final AccountEntry toLogin = account;
        AccountEntry.EXECUTOR.execute(() -> loginAndAdd(toLogin));
    }

    private void openFileDialog() {
        String result;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.txt")).flip();
            String start = (this.selectedFilePath != null) ? this.selectedFilePath : System.getProperty("user.home");
            result = TinyFileDialogs.tinyfd_openFileDialog("Open File", start, filters, "Cookie Files (*.txt)", true);
        }
        this.fileDialogOpen = false;
        if (result != null) {
            List<File> files = new ArrayList<>();
            for (String path : result.split("\\|")) {
                File file = new File(path);
                if (file.exists()) {
                    files.add(file);
                }
            }
            com.adjustclient.ui.account.AccountFileDropHandler.INSTANCE.importFiles(files);
        }
    }
}
