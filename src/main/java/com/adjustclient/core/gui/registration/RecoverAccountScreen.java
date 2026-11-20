package com.adjustclient.core.gui.registration;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.transport.HydraPacketEncoder;
import com.adjustclient.managers.util.RecoveryKeyGenerator;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.utilities.Timer;
import java.awt.Color;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class RecoverAccountScreen extends GuiScreen {
    private static boolean recovering;

    private GuiTextField keyField;
    private final GuiButton recoverButton;
    private final GuiButton backButton;
    public String statusMessage;
    public final Timer statusTimer;
    public int statusAlpha;

    public RecoverAccountScreen() {
        this.recoverButton = new GuiButton(0, 0, 100, 200, 20, "Recover Account");
        this.backButton = new GuiButton(1, 0, 124, 200, 20, "Back");
        this.statusTimer = new Timer();
    }

    @Override
    public void updateScreen() {
        if (!this.statusTimer.hasElapsed(3000L)) {
            this.statusAlpha = 255;
        } else if (this.statusMessage != null) {
            this.statusAlpha -= 10;
            if (this.statusAlpha < 1) {
                this.statusMessage = null;
            }
        }

        boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (!authenticated) {
            recovering = false;
        }

        String text = this.keyField.getText();
        this.recoverButton.enabled = !recovering && authenticated && text.length() >= 11 && text.contains(" ");
        this.keyField.updateCursorCounter();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == 1) {
            this.openRegisterScreen();
            return;
        }
        if (button.id == 0) {
            recovering = true;
            String hardwareId = "";
            byte[] packet = HydraPacketEncoder.encodeRecoverAccount(
                    RecoveryKeyGenerator.hash(this.keyField.getText()),
                    1,
                    hardwareId);
            ServiceUtil.sendToBackend(packet);
            this.keyField.setText("");
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        this.keyField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.openRegisterScreen();
        }
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
        this.drawCenteredString(this.fontRendererObj, "Recover Account", centerX, 20, new Color(184, 189, 178, 255).getRGB());
        this.fontRendererObj.drawString("Recovery Key", fieldX + 1, 54, new Color(40, 40, 40).getRGB());
        this.fontRendererObj.drawString("Recovery Key", fieldX, 53, new Color(160, 160, 160).getRGB());

        boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (!authenticated) {
            this.drawCenteredString(this.fontRendererObj, "Hydra is unavailable.", centerX, 33, new Color(171, 36, 36).getRGB());
        } else if (this.statusMessage != null && this.statusAlpha > 0 && this.statusAlpha <= 255) {
            this.drawCenteredString(this.fontRendererObj, this.statusMessage, centerX, 33, new Color(171, 36, 36, this.statusAlpha).getRGB());
        }

        this.keyField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(this.recoverButton);
        this.buttonList.add(this.backButton);

        int fieldX = this.width / 2 - 100;
        this.recoverButton.xPosition = fieldX;
        this.backButton.xPosition = fieldX;

        String previousText = this.keyField != null ? this.keyField.getText() : "";
        if (previousText.length() > 600) {
            previousText = previousText.substring(0, 600);
        }
        this.keyField = new GuiTextField(2, this.fontRendererObj, fieldX, 66, 200, 20);
        this.keyField.setMaxStringLength(600);
        this.keyField.setText(previousText);
        this.keyField.setFocused(true);
    }

    public void openRegisterScreen() {
        this.mc.displayGuiScreen(new RegisterScreen());
    }
}
