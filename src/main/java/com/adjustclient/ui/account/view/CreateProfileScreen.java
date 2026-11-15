package com.adjustclient.ui.account.view;

import java.awt.Color;
import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import com.adjustclient.Adjust;
import com.adjustclient.auth.MicrosoftAuth;
import com.adjustclient.auth.RandomNames;
import com.adjustclient.ui.account.AccountEntry;
import com.adjustclient.ui.account.login.AddAccountScreen;
import com.adjustclient.util.WorldUtil;
import net.minecraft.client.Minecraft;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;

public final class CreateProfileScreen extends GuiScreen {

    /** Add Account screen that opened this screen. */
    private final AddAccountScreen addAccountScreen;
    private final AccountEntry account;
    private GuiTextField usernameField;
    private GuiButton createButton;
    private GuiButton randomButton;
    /** Error / status text drawn under the title (null = none). */
    private String errorMessage;
    private Object obf88;

    public CreateProfileScreen(AddAccountScreen addAccountScreen, AccountEntry account) {
        this.addAccountScreen = addAccountScreen;
        this.account = account;
    }

    public int getMaxFps() {
        if (!AdjustMainMenu.windowActive) {
            return 60;
        }
        int limit = Minecraft.getMinecraft().gameSettings.limitFramerate;
        return Math.min(175, limit);
    }

    @Override
    public void updateScreen() {
        if (this.usernameField != null) {
            this.usernameField.updateCursorCounter();
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.usernameField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, 66, 200, 20);
        this.usernameField.setMaxStringLength(16);
        this.createButton = new GuiButton(2, this.width / 2 - 100, 90, "Create profile");
        this.buttonList.add(this.createButton);
        this.createButton.enabled = false;
        this.randomButton = new GuiButton(3, this.width / 2 - 100, 114, "Random username");
        this.buttonList.add(this.randomButton);
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 110, I18n.format("gui.back")));
    }

    private void updateCreateButton() {
        boolean valid;
        if (this.usernameField.isEnabled()) {
            String text = this.usernameField.getText();
            valid = text.length() >= 3 && text.matches("^\\w{2,16}$");
        } else {
            valid = false;
        }
        this.createButton.enabled = valid;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {                                      // ESC
            this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
            return;
        }
        this.usernameField.textboxKeyTyped(typedChar, keyCode);
        updateCreateButton();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        switch (button.id) {
            case 1: // back
                this.mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
                break;
            case 2: { // create profile
                GuiScreen accountScreen = Adjust.INSTANCE.accountManager;
                this.usernameField.setEnabled(false);
                this.randomButton.enabled = false;
                this.createButton.enabled = false;
                AccountEntry.EXECUTOR.execute(new CreateProfileTask(this, accountScreen));
                break;
            }
            case 3: // random username
                this.usernameField.setText(RandomNames.randomUsername());
                updateCreateButton();
                break;
            default:
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.theWorld != null) {
            Gui.drawRect(0, 0, this.width, this.height, 0xC0101010);
        } else {
            this.drawBackground(0);
        }
        this.drawCenteredString(this.fontRendererObj, "Profile Creation", this.width / 2, 17, 0xFFFFFF);
        this.usernameField.drawTextBox();
        this.drawString(this.fontRendererObj, "Username", this.width / 2 - 100, 54, 10526880);
        if (this.errorMessage != null) {
            this.drawCenteredString(this.fontRendererObj, this.errorMessage, this.width / 2, 42,
                    new Color(190, 50, 50, 255).getRGB());
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    void createProfile(GuiScreen accountScreen) {
        String username = this.usernameField.getText();
        try {
            int status = MicrosoftAuth.createProfile(this.account.token, username);
            if (status >= 401) {
                if (status == 401) {
                    this.errorMessage = "Unauthorized, try again later.";
                } else if (status == 429) {
                    this.errorMessage = "Too many requests, change your IP address or try again later.";
                } else {
                    this.errorMessage = "Error creating profile: " + status;
                }
            } else if (status == 200) {
                Object uuid = WorldUtil.lookupUuid(username);
                this.account.username = username;                  // entry name
                this.account.setUuid(uuid.toString());
                if (accountScreen != null) {
                    this.addAccountScreen.addToRepository(this.account, ((com.adjustclient.ui.account.AccountManagerScreen) accountScreen).currentRepository);
                    this.mc.displayGuiScreen(accountScreen);
                    this.addAccountScreen.updateButtons();
                }
            } else if (status != 400) {
                this.errorMessage = "Error creating profile: " + status;
            } else {
                this.errorMessage = "This username is invalid, or something went wrong.";
            }
        } catch (IOException e) {
            this.errorMessage = "Successfully created profile, but failed to get UUID; try adding the account again.";
        }
    }

    private static final class CreateProfileTask implements Runnable {
        private final CreateProfileScreen screen;
        private final GuiScreen accountScreen;

        CreateProfileTask(CreateProfileScreen screen, GuiScreen accountScreen) {
            this.screen = screen;
            this.accountScreen = accountScreen;
        }

        @Override
        public void run() {
            this.screen.createProfile(this.accountScreen);
        }
    }
}
