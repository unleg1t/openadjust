package com.adjustclient.ui.account.view;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.io.Files;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import com.adjustclient.logging.Logger;
import com.adjustclient.util.Timer;
import com.adjustclient.Adjust;
import com.adjustclient.auth.MicrosoftAuth;
import com.adjustclient.auth.RandomNames;
import com.adjustclient.backend.api.BackendRequest;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.DataResponseBody;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.AccountHead;
import com.adjustclient.ui.account.AccountManagerScreen;
import com.adjustclient.ui.account.AccountType;
import com.adjustclient.ui.account.login.LoginAccountButton;
import com.adjustclient.ui.account.login.auth.MicrosoftCookieAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftCredentialAccount;
import com.adjustclient.ui.account.login.auth.MicrosoftRefreshTokenAccount;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.ui.stats.HypixelStatsScreen;
import com.adjustclient.util.ClipboardUtil;
import com.adjustclient.util.StringUtil;
import net.minecraft.client.Minecraft;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;

public final class ViewAccountScreen extends GuiScreen {
    public static String lastCookieSavePath;

    private static boolean credentialRequestIdle;

    static {
        credentialRequestIdle = true;
    }

    /** true while the "Edit profile" mode is active. */
    private boolean editing;
    /** Fake player used for the skin preview (created lazily in drawScreen). */
    private EntityOtherPlayerMP previewPlayer;
    private GuiTextField nameField;
    private GuiButton editButton;
    private GuiButton setNameButton;
    private GuiButton randomNameButton;
    private GuiButton setRandomSkinButton;
    private GuiButton refreshButton;
    private GuiButton doneButton;
    /** "Download" (cookie account) or "Copy Credentials" (credential account), id 9; null for other account kinds. */
    private GuiButton exportButton;
    private LoginAccountButton hypixelButton;
    /** Status / error line shown while editing (red, fades after 3 s, removed after 4 s). */
    private String message;
    private Timer messageTimer;
    private Object obf112;

    public ViewAccountScreen() {
    }

    public int getMaxFps() {
        if (!AdjustMainMenu.windowActive) {
            return 60;
        }
        return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate);
    }

    @Override
    public void updateScreen() {
        if (this.messageTimer != null) {
            if (this.messageTimer.hasElapsed(4000L)) {                // lastMs + 4000 <= currentTimeMillis()
                this.messageTimer = null;
                this.message = null;
                this.nameField.setEnabled(true);
                this.randomNameButton.enabled = true;
                updateSetNameButton();
            }
        }
        GuiScreen accountScreen = Adjust.INSTANCE.accountManager;
        AccountEntry account = ((AccountManagerScreen) accountScreen).selectedAccount;
        boolean refreshEnabled;
        if (account != null) {
            if (account.fetchingAccountData) {
                this.refreshButton.enabled = false;
                refreshEnabled = false;
            } else {
                AccountType type = account.getType();
                refreshEnabled = AccountType.SESSION != type;
                this.refreshButton.enabled = refreshEnabled;
            }
        } else {
            this.refreshButton.enabled = true;
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);      // no account to show: back to the account manager
        }
        if (this.editing) {
            this.nameField.updateCursorCounter();
        }
        if (this.exportButton != null) {
            this.exportButton.enabled = credentialRequestIdle;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void setEditing(boolean editing) {
        if (!editing) {
            this.editButton.displayString = "Edit profile";
            this.buttonList.remove(this.setNameButton);
            this.buttonList.remove(this.randomNameButton);
            this.buttonList.remove(this.setRandomSkinButton);
            this.editing = false;
        } else {
            this.editButton.displayString = "Done editing";
            this.buttonList.add(this.setNameButton);
            this.buttonList.add(this.randomNameButton);
            addSetRandomSkinButton();
            this.editing = true;
        }
    }

    private void setRandomSkin(AccountEntry account) {
        Object skin = RandomNames.randomSkin();
        if (skin == null) {
            this.message = "Failed to get random skin.";
            this.messageTimer = new Timer();
            this.setRandomSkinButton.enabled = true;
            return;
        }
        int status = MicrosoftAuth.setSkin(account.token, ((com.adjustclient.auth.RandomSkin) skin).skinUrl(), ((com.adjustclient.auth.RandomSkin) skin).variant());
        switch (status) {
            case 200:
                this.message = ChatColor.GREEN + "Successfully set skin.";
                if (account.head != null) {
                    account.head.skin = null;
                    account.head.profile = null;
                    account.head.requested = false;
                    account.head.loaded = false;
                }
                break;
            case 400:
                this.message = "Bad request, try again?";
                break;
            case 401:
                this.message = "This account is invalid, refresh it and try again.";
                break;
            case 403:
                this.message = "This skin is invalid.";
                break;
            case 429:
                this.message = "Too many requests, change your IP address or try again later.";
                break;
            case -1:
                this.message = "An unknown error occurred.";
                break;
            default:
                this.message = "Error setting skin: " + status;
                break;
        }
        this.messageTimer = new Timer();
        this.setRandomSkinButton.enabled = true;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        AccountManagerScreen accountScreen = (AccountManagerScreen) Adjust.INSTANCE.accountManager;
        switch (button.id) {
            case 0: { // Refresh
                AccountEntry account = accountScreen.selectedAccount;
                if (account != null) {
                    if (accountScreen.currentRepository != null) {
                        account.requestAccountData(null);
                    }
                }
                break;
            }
            case 1: // Done
                if (!this.doneButton.enabled) {
                    break;
                }
                this.mc.displayGuiScreen(accountScreen);
                setEditing(false);
                break;
            case 2: // Edit profile / Done editing
                setEditing(!this.editing);
                break;
            case 3: { // Set name
                AccountEntry account = accountScreen.selectedAccount;
                if (account == null) {
                    break;
                }
                this.nameField.setEnabled(false);
                this.randomNameButton.enabled = false;
                this.setNameButton.enabled = false;
                this.doneButton.enabled = false;
                String newName = this.nameField.getText();
                AccountEntry.EXECUTOR.execute(new AsyncTask(this, new SetNameTask(this, account, newName, account.token)));
                break;
            }
            case 4: { // Set random skin
                AccountEntry account = accountScreen.selectedAccount;
                if (account == null) {
                    break;
                }
                this.setRandomSkinButton.enabled = false;
                runAsync(new RandomSkinTask(this, account));
                break;
            }
            case 5: // Random username
                this.nameField.setText(RandomNames.randomUsername());
                updateSetNameButton();
                break;
            case 9: { // Download cookie / Copy credentials
                if (!credentialRequestIdle) {
                    break;
                }
                AccountEntry account = accountScreen.selectedAccount;
                if (account != null) {
                    if (account instanceof MicrosoftCookieAccount || account instanceof MicrosoftCredentialAccount) {
                        credentialRequestIdle = false;
                        AccountEntry.EXECUTOR.execute(() -> {
                            try { onCredentialsResponse(account, account.serialize()); }
                            catch (Exception error) { credentialRequestIdle = true; error.printStackTrace(); }
                        });
                    }
                }
                break;
            }
            case 13: { // Move Account
                AccountEntry account = accountScreen.selectedAccount;
                if (account != null) {
                    accountScreen.removeAccounts(new AccountEntry[] { account });
                }
                break;
            }
            default:
                break;
        }
    }

    private void runAsync(Runnable action) {
        this.doneButton.enabled = false;
        AccountEntry.EXECUTOR.execute(new AsyncTask(this, action));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {                                           // ESC
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
        } else if (this.editing) {
            this.nameField.textboxKeyTyped(typedChar, keyCode);
            updateSetNameButton();
        }
    }

    private static void onCredentialsResponse(AccountEntry account, byte[] response) throws IOException {
        if (response != null) {
            if (account instanceof MicrosoftCookieAccount) {
                AccountEntry.EXECUTOR.execute(() -> {
                    try { saveCookieFile(account, response); }
                    catch (Exception error) { credentialRequestIdle = true; error.printStackTrace(); }
                });
                return;
            }
            credentialRequestIdle = true;
            String[] credentials = MicrosoftCredentialAccount.decode(response);
            NotificationBuilder toast = new NotificationBuilder("Account Manager");
            toast.format("Your account {} was saved to the clipboard.", new Object[] { account.username });
            toast.show();
            ClipboardUtil.copyToClipboard(credentials[0] + ":" + credentials[1]);       // set clipboard string
            return;
        }
        Logger.log(LogLevel.ERROR, true, "Failed to download account data.", new Object[0]);
        NotificationBuilder toast = new NotificationBuilder("Account Manager");
        toast.format("An error occurred while fetching your account data for {}.", new Object[] { account.username });
        toast.show();
        credentialRequestIdle = true;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.editing) {
            this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            LoginAccountButton icon = this.hypixelButton;
            if (icon.enabled && icon.visible) {
                float left = icon.x;
                float top = icon.y;
                if ((float) mouseX >= left && (float) mouseY >= top
                        && (float) icon.width + left > (float) mouseX && (float) icon.height + top > (float) mouseY) {
                    AccountEntry account = ((AccountManagerScreen) Adjust.INSTANCE.accountManager).selectedAccount;
                    if (account != null) {
                        this.mc.displayGuiScreen(new HypixelStatsScreen(this, account.uuid));
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setName(AccountEntry account, String newName, String token) {
        try {
            UUID uuid = null;
            boolean checkWithApi;
            AccountType type = account.getType();
            if (type == null || account instanceof MicrosoftCookieAccount || account instanceof MicrosoftRefreshTokenAccount) {
                checkWithApi = true;
            } else if (AccountType.SESSION == type) {
                if (account.uuid == null || account.uuid.isEmpty()) {
                    showMessage("This account is invalid.");
                    return;
                }
                try {
                    uuid = UUIDTypeAdapter.fromString(account.uuid);              // local (offline) account: no name change API call
                } catch (IllegalArgumentException ignored) {
                    uuid = net.minecraft.entity.player.EntityPlayer.getOfflineUUID(account.username != null ? account.username : account.uuid);
                }
                checkWithApi = false;
            } else {
                checkWithApi = true;
            }
            if (checkWithApi) {
                com.adjustclient.auth.NameChangeStatus status = MicrosoftAuth.getNameChangeStatus(token);
                switch (status) {
                    case INVALID:
                        showMessage("This account is invalid, refresh it and try again.");
                        return;
                    case TOO_MANY_REQUESTS:
                        showMessage("Too many requests, change your IP address or try again later.");
                        return;
                    case COOLDOWN:
                        showMessage("This account is on name change cool-down.");
                        return;
                    case UNKNOWN:
                        showMessage("An unknown error occurred while checking name change availability.");
                        return;
                    default:
                        break;
                }
                int code = MicrosoftAuth.changeName(token, newName);
                switch (code) {
                    case 200:
                        break;
                    case 401:
                        showMessage("This account is invalid, refresh it and try again.");
                        return;
                    case 403:
                        showMessage("This username is invalid.");
                        return;
                    case 429:
                        showMessage("Too many requests, change your IP address or try again later.");
                        return;
                    case -1:
                        showMessage("An unknown error occurred setting name.");
                        return;
                    default:
                        showMessage("Error setting name: " + code);
                        return;
                }
            }
            this.mc.addScheduledTask(() -> {
                account.username = newName;
                account.setUuid(account.uuid);
                com.adjustclient.ui.account.LocalAccountStore.INSTANCE.save();
            });
            this.message = ChatColor.GREEN + "Successfully set name to " + newName + ".";
            this.messageTimer = new Timer();
        } catch (RuntimeException e) {
            showMessage("This account is invalid.");
        }
    }

    private void showMessage(String text) {
        this.message = text;
        this.messageTimer = new Timer();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        AccountEntry account = ((AccountManagerScreen) Adjust.INSTANCE.accountManager).selectedAccount;
        if (this.mc.theWorld == null) {
            this.drawBackground(0);
        } else {
            Gui.drawRect(0, 0, this.width, this.height, 0xC0101010);
        }
        this.drawCenteredString(this.fontRendererObj, this.editing ? "Editing" : "Viewing", this.width / 2, 17, 0xFFFFFF);
        // sub title: "<name> <colour>(<type>)" at half scale, centred at y = 28.5
        String subtitle;
        ChatColor colour = ChatColor.GRAY;
        if (account != null) {
            String shownName = com.adjustclient.module.impl.render.streamer.StreamerModule.INSTANCE.maskNameIfEnabled(account.username);   // name, hidden in streamer mode
            AccountType type = account.getType();
            if (account instanceof MicrosoftRefreshTokenAccount) {
                subtitle = shownName + " " + colour + "(" + "Browser" + ")";
            } else if (account instanceof MicrosoftCookieAccount) {
                subtitle = shownName + " " + colour + "(" + "Cookie" + ")";
            } else {
                subtitle = shownName + " " + colour + "(" + (type != null ? type.displayName : "Session") + ")";
            }
        } else {
            subtitle = colour + "None";
        }
        float subtitleWidth = this.fontRendererObj.getStringWidth(subtitle, 0.5F);   // client FontRenderer: width * scale
        float subtitleX = (float) this.width * 0.5F - subtitleWidth * 0.5F;
        this.fontRendererObj.drawString(subtitle, 0xFFFFFF, true, subtitleX, 28.5F, 0.5F);
        if (account != null) {
            account.head.c();
            if (this.editing) {
                this.nameField.drawTextBox();
                this.fontRendererObj.drawString("Username", 10526880, true, (float) (this.width / 2 - 100), 54.0F, 1.0F);
                if (this.message != null && this.messageTimer != null) {
                    long elapsed = System.currentTimeMillis() - this.messageTimer.lastMs;
                    float alpha;
                    if (this.messageTimer.lastMs + 3000L > System.currentTimeMillis()) {
                        alpha = 1.0F;
                    } else {
                        float fade = (float) (int) (elapsed - 3000L) / 1000.0F;
                        if (1.0F < fade) {
                            fade = 1.0F;
                        }
                        alpha = 1.0F - fade;
                        if (alpha <= 0.05F) {
                            super.drawScreen(mouseX, mouseY, partialTicks);
                            return;
                        }
                    }
                    GlStateManager.enableBlend();
                    int a = (int) (alpha * 255.0F);
                    this.drawCenteredString(this.fontRendererObj, this.message, this.width / 2, 42,
                            new Color(190, 50, 50, a).getRGB());
                    GlStateManager.disableBlend();
                }
            } else {
                float centre = (float) this.width * 0.5F;
                float bottom = (float) this.height * 0.25F + 82.0F;
                float left = centre - 100.0F;
                float right = centre + 100.0F;
                float top = 38.5F;
                NvgDraw.fillRect(new Color(50, 50, 50, 50).getRGB(), left, top, right, bottom);
                NvgDraw.strokeRect(false, new Color(110, 110, 115, 50).getRGB(), left, top, right, bottom, 0.5F);
                this.hypixelButton.drawButton(mouseX, mouseY, 50, left + 2.0F, bottom - 10.0F);
                AccountHead profile = account.head;
                if (profile != null && profile.profile != null) {
                    if (this.previewPlayer == null) {
                        this.previewPlayer = new EntityOtherPlayerMP(null, profile.profile);   // (world, GameProfile)
                        this.previewPlayer.playerInfo = new NetworkPlayerInfo(profile.profile);
                        // show all skin layers: dataWatcher byte #10 = (byte) 127
                        this.previewPlayer.getDataWatcher().updateObject(10, (byte) 127);
                    }
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.enableDepth();
                    boolean small = this.height < 425;
                    int scale = small ? 40 : 60;
                    int posY = small ? 130 : 180;
                    int pivot = small ? 65 : 95;
                    GuiInventory.drawEntityOnScreen(this.width / 2, posY, scale,
                            (float) (this.width / 2 - mouseX), (float) (posY - mouseY - pivot), this.previewPlayer);
                    GlStateManager.disableDepth();
                }
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (account == null) {
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.refreshButton = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 86, 98, 20, "Refresh");
        this.buttonList.add(this.refreshButton);
        this.editButton = new GuiButton(2, this.width / 2 + 2, this.height / 4 + 86, 98, 20,
                this.editing ? "Done editing" : "Edit profile");
        this.buttonList.add(this.editButton);
        this.nameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
        this.nameField.setMaxStringLength(16);
        this.setNameButton = new GuiButton(3, this.width / 2 - 100, 90, 200, 20, "Set name");
        this.setNameButton.enabled = false;
        this.randomNameButton = new GuiButton(5, this.width / 2 + 104, 66, 50, 20, "Random");
        this.setRandomSkinButton = new GuiButton(4, this.width / 2 - 100, 114, 200, 20, "Set random skin");
        if (this.editing) {
            this.buttonList.add(this.setNameButton);
            this.buttonList.add(this.randomNameButton);
            addSetRandomSkinButton();
        }
        this.doneButton = new GuiButton(1, this.width / 2 - 100, this.height / 4 + 110, I18n.format("gui.done"));
        this.buttonList.add(this.doneButton);
        AccountEntry account = ((AccountManagerScreen) Adjust.INSTANCE.accountManager).selectedAccount;
        if (account instanceof MicrosoftCookieAccount) {
            this.exportButton = new GuiButton(9, this.width / 2 - 100, this.height / 4 + 134, 98, 20, "Download");
            this.buttonList.add(this.exportButton);
            this.buttonList.add(new GuiButton(13, this.width / 2 + 2, this.height / 4 + 134, 98, 20, "Move Account"));
        } else if (account instanceof MicrosoftCredentialAccount) {
            this.exportButton = new GuiButton(9, this.width / 2 - 100, this.height / 4 + 134, 98, 20, "Copy Credentials");
            this.buttonList.add(this.exportButton);
            this.buttonList.add(new GuiButton(13, this.width / 2 + 2, this.height / 4 + 134, 98, 20, "Move Account"));
        }
        this.message = null;
        this.hypixelButton = new LoginAccountButton(account, new ResourceLocation("adjust", "textures/account/hypixel.png"));   // 8 x 8 icon
        this.messageTimer = null;
        this.previewPlayer = null;
    }

    private static void saveCookieFile(AccountEntry account, byte[] response) {
        String lastPath = lastCookieSavePath;
        String base = lastPath != null ? lastPath : System.getProperty("user.home");
        File start = new File(base);
        String directory = start.isDirectory() ? base : start.getParent();
        String defaultFile = new File(directory, account.username + ".txt").getPath();
        String chosen;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            org.lwjgl.PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.txt"));
            filters.flip();
            chosen = TinyFileDialogs.tinyfd_saveFileDialog("Save Cookie", defaultFile, filters, "Cookie Files (*.txt)");
        }
        if (chosen == null) {
            credentialRequestIdle = true;
            return;
        }
        lastCookieSavePath = chosen;
        String content;
        try {
            content = MicrosoftCookieAccount.decode(response);
        } catch (IOException e) {
            NotificationBuilder toast = new NotificationBuilder("Account Manager");
            toast.format("An error occurred when decoding your cookie account {}.",
                    new Object[] { account.username });
            toast.show();
            credentialRequestIdle = true;
            return;
        }
        try {
            Files.write(content, new File(chosen), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.log(LogLevel.ERROR, true, "Unable to save cookie file at '{}'", new Object[] { chosen });
            NotificationBuilder toast = new NotificationBuilder("Account Manager");
            toast.format("An error occurred while saving your cookie account {}. Is the file writable?",
                    new Object[] { account.username });
            toast.show();
            credentialRequestIdle = true;
            return;
        }
        String size = StringUtil.formatBytes(content.length());            // human readable size
        NotificationBuilder toast = new NotificationBuilder("Account Manager");
        toast.format("Your account {} was saved successfully. (" + size + ")", new Object[] { account.username });
        toast.show();
        credentialRequestIdle = true;
    }

    private void addSetRandomSkinButton() {
        AccountEntry account = ((AccountManagerScreen) Adjust.INSTANCE.accountManager).selectedAccount;
        if (account == null) {
            return;
        }
        AccountType type = account.getType();
        boolean add;
        if (type == null || account instanceof MicrosoftCookieAccount || account instanceof MicrosoftRefreshTokenAccount) {
            add = true;
        } else if (AccountType.SESSION == type) {
            add = account.uuid.length() != 0;                                 // local account needs a uuid
        } else {
            add = true;
        }
        if (add) {
            this.buttonList.add(this.setRandomSkinButton);
        }
    }

    private void updateSetNameButton() {
        boolean valid;
        if (this.nameField.isEnabled()) {
            String text = this.nameField.getText();
            valid = text.length() >= 3 && text.matches("^\\w{2,16}$");
        } else {
            valid = false;
        }
        this.setNameButton.enabled = valid;
    }

    // ---------------------------------------------------------------------------------------------------------------------
    // lambdas

    private static final class AsyncTask implements Runnable {
        private final ViewAccountScreen screen;
        private final Runnable action;

        AsyncTask(ViewAccountScreen screen, Runnable action) {
            this.screen = screen;
            this.action = action;
        }

        @Override
        public void run() {
            this.action.run();
            this.screen.doneButton.enabled = true;
        }
    }

    private static final class RandomSkinTask implements Runnable {
        private final ViewAccountScreen screen;
        private final AccountEntry account;

        RandomSkinTask(ViewAccountScreen screen, AccountEntry account) {
            this.screen = screen;
            this.account = account;
        }

        @Override
        public void run() {
            this.screen.setRandomSkin(this.account);
        }
    }

    private static final class SetNameTask implements Runnable {
        private final ViewAccountScreen screen;
        private final AccountEntry account;
        private final String newName;
        private final String token;

        SetNameTask(ViewAccountScreen screen, AccountEntry account, String newName, String token) {
            this.screen = screen;
            this.account = account;
            this.newName = newName;
            this.token = token;
        }

        @Override
        public void run() {
            this.screen.setName(this.account, this.newName, this.token);
        }
    }

    private static final class CredentialsCallback implements Consumer<BackendResponse> {
        private final AccountEntry account;

        CredentialsCallback(AccountEntry account) {
            this.account = account;
        }

        @Override
        public void accept(BackendResponse response) {
            try { ViewAccountScreen.onCredentialsResponse(this.account, this.account.serialize()); }
            catch (IOException error) { credentialRequestIdle = true; error.printStackTrace(); }
        }
    }

    private static final class SaveCookieTask implements Runnable {
        private final AccountEntry account;
        private final BackendResponse response;

        SaveCookieTask(AccountEntry account, BackendResponse response) {
            this.account = account;
            this.response = response;
        }

        @Override
        public void run() {
            try { ViewAccountScreen.saveCookieFile(this.account, this.account.serialize()); }
            catch (IOException error) { credentialRequestIdle = true; error.printStackTrace(); }
        }
    }

    private static final class SessionUpdateErrorLogger implements Consumer<BackendResponse> {
        private final AccountEntry account;

        SessionUpdateErrorLogger(AccountEntry account) {
            this.account = account;
        }

        @Override
        public void accept(BackendResponse response) {
            if (response.status == ResponseStatus.OK) {
                return;
            }
            Logger.log(LogLevel.WARN, true, "Failed to update {} session: {}",
                    new Object[] { this.account.username, response.status });
        }
    }

    private static final class AccountUpdateErrorLogger implements Consumer<BackendResponse> {
        private final AccountEntry account;

        AccountUpdateErrorLogger(AccountEntry account) {
            this.account = account;
        }

        @Override
        public void accept(BackendResponse response) {
            if (response.status == ResponseStatus.OK) {
                return;
            }
            Logger.log(LogLevel.WARN, true, "Failed to update {} account: {}",
                    new Object[] { this.account.username, response.status });
        }
    }
}
