package com.adjustclient.ui.registration;

import com.adjustclient.Adjust;
import com.adjustclient.backend.transport.HydraPacketEncoder;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;
import com.adjustclient.util.Timer;
import java.awt.Color;
import java.io.IOException;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public final class RegisterScreen extends GuiScreen {
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");

    private boolean wasAuthenticated;
    private boolean registering;
    private boolean pendingClose;
    private GuiTextField usernameField;
    private GuiTextField inviteCodeField;
    private final GuiButton registerButton;
    private final GuiButton recoverButton;
    private final GuiButton migrateButton;
    public String statusMessage;
    public final Timer statusTimer;
    public int statusAlpha;

    public RegisterScreen() {
        this.registerButton = new GuiButton(0, 0, 140, 200, 20, "Register");
        this.recoverButton = new GuiButton(1, 0, 164, 200, 20, "Account Recovery");
        this.migrateButton = new GuiButton(2, 0, 188, 200, 20, "Migrate Hardware ID");
        this.statusTimer = new Timer();
    }

    @Override
    public void updateScreen() {
        boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (authenticated != this.wasAuthenticated) {
            this.updateRegisterButtonState();
            this.wasAuthenticated = authenticated;
        }

        if (!this.statusTimer.hasElapsed(3000L)) {
            this.statusAlpha = 255;
        } else if (this.statusMessage != null) {
            this.statusAlpha -= 10;
            if (this.statusAlpha < 1) {
                this.statusMessage = null;
                this.updateRegisterButtonState();
            }
        }

        if (this.pendingClose) {
            this.mc.displayGuiScreen(new AdjustMainMenu());
        }
        ServiceUtil.openMainMenu();

        if (this.usernameField != null) {
            this.usernameField.updateCursorCounter();
        }
        if (this.inviteCodeField != null) {
            this.inviteCodeField.updateCursorCounter();
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private static boolean isValidInviteCode(String inviteCode) {
        return inviteCode != null
                && inviteCode.length() >= 6
                && inviteCode.length() < 8
                && ALPHANUMERIC_PATTERN.matcher(inviteCode).matches();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == 0) {
            if (!this.registering) {
                long hwidHash = 0L;
                byte[] packet = HydraPacketEncoder.encodeRegister(
                        this.usernameField.getText(),
                        this.inviteCodeField.getText(),
                        hwidHash);
                ServiceUtil.sendToBackend(packet);
                this.registering = true;
                this.buttonList.get(0).enabled = false;
            }
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(new RecoverAccountScreen());
        } else if (button.id == 2) {
            this.mc.displayGuiScreen(new MigrateHwidScreen());
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        this.usernameField.textboxKeyTyped(typedChar, keyCode);
        this.inviteCodeField.textboxKeyTyped(typedChar, keyCode);
        this.updateRegisterButtonState();

        if (keyCode == Keyboard.KEY_TAB) {
            boolean usernameFocused = this.usernameField.isFocused();
            this.usernameField.setFocused(!usernameFocused);
            this.inviteCodeField.setFocused(usernameFocused);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.inviteCodeField.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.theWorld != null) {
            drawRect(0, 0, this.width, this.height, -1072689136);
        } else {
            this.drawDefaultBackground();
        }

        int centerX = this.width / 2;
        int fieldX = centerX - 100;
        this.drawCenteredString(this.fontRendererObj, "Join Adjust", centerX, 20, new Color(184, 189, 178, 255).getRGB());
        this.drawFieldLabel("Username", fieldX, 53);
        this.drawFieldLabel("Invite code (XXXXXX)", fieldX, 94);

        boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (!authenticated) {
            this.drawCenteredString(this.fontRendererObj, "Hydra is unavailable.", centerX, 33, new Color(171, 36, 36).getRGB());
        } else if (this.statusMessage != null && this.statusAlpha > 0 && this.statusAlpha <= 255) {
            this.drawCenteredString(this.fontRendererObj, this.statusMessage, centerX, 33, new Color(171, 36, 36, this.statusAlpha).getRGB());
        }

        this.inviteCodeField.drawTextBox();
        this.usernameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawFieldLabel(String label, int x, int y) {
        this.fontRendererObj.drawString(label, x + 1, y + 1, new Color(40, 40, 40).getRGB());
        this.fontRendererObj.drawString(label, x, y, 10526880);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.buttonList.clear();
        this.buttonList.add(this.registerButton);
        this.buttonList.add(this.recoverButton);
        this.buttonList.add(this.migrateButton);

        int fieldX = this.width / 2 - 100;
        this.registerButton.xPosition = fieldX;
        this.recoverButton.xPosition = fieldX;
        this.migrateButton.xPosition = fieldX;

        String usernameText = this.usernameField == null
                ? System.getProperty("user.name", "")
                : this.usernameField.getText();
        this.usernameField = createField(3, fieldX, 66, 16, usernameText);
        this.usernameField.setFocused(true);

        String inviteText = this.inviteCodeField == null ? "" : this.inviteCodeField.getText();
        this.inviteCodeField = createField(4, fieldX, 106, 7, inviteText);

        this.updateRegisterButtonState();
    }

    private GuiTextField createField(int id, int x, int y, int maxLength, String initialText) {
        String clamped = initialText.length() > maxLength ? initialText.substring(0, maxLength) : initialText;
        GuiTextField field = new GuiTextField(id, this.fontRendererObj, x, y, 200, 20);
        field.setMaxStringLength(maxLength);
        field.setText(clamped);
        return field;
    }

    public void updateRegisterButtonState() {
        String username = this.usernameField.getText();
        String inviteCode = this.inviteCodeField.getText();
        boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        this.registerButton.enabled = !this.registering
                && authenticated
                && username.length() >= 3
                && username.length() <= 16
                && ALPHANUMERIC_PATTERN.matcher(username).matches()
                && isValidInviteCode(inviteCode);
    }

    @Override
    public int getMaxFps() {
        return AdjustMainMenu.windowActive ? Math.min(175, this.mc.gameSettings.limitFramerate) : 60;
    }
}
