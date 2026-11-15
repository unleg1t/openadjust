package com.adjustclient.ui.registration;

import com.adjustclient.Adjust;
import com.adjustclient.backend.api.request.client.RegisterRequest;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.util.RecoveryKeyGenerator;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.mainmenu.AdjustMainMenu;
import com.adjustclient.util.ClipboardUtil;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.util.Timer;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public final class RecoveryKeyScreen extends GuiScreen {
    private boolean submitting;
    private GuiTextField keyField;
    private final GuiButton copyButton;
    private final GuiButton confirmButton;
    private Timer timer;
    private String recoveryKey;
    private List<IChatComponent> wrappedKeyLines;

    private final Runnable returnToMenu = () -> finishRecovery();

    private final Consumer<BackendResponse> recoveryResponse = response -> {
        this.submitting = false;
        if (response == null) {
            return;
        }
        if (response.status == ResponseStatus.OK) {
            if (this.mc != null) {
                this.mc.addScheduledTask(this.returnToMenu);
            }
            return;
        }
        Logger.log(LogLevel.ERROR, true, "Failed to update recovery key: {}", response);
    };

    public RecoveryKeyScreen() {
        this.copyButton = new GuiButton(0, 0, 0, 200, 20, "Copy to Clipboard");
        this.confirmButton = new GuiButton(1, 0, 0, 200, 20, "");
    }

    @Override
    public void updateScreen() {
        this.confirmButton.displayString = "Confirm";
        long now = System.currentTimeMillis();
        this.confirmButton.enabled = this.timer != null && this.timer.lastMs + 15000L <= now;
        if (!this.confirmButton.enabled) {
            long remaining = 15000L - (now - (this.timer != null ? this.timer.lastMs : now));
            this.confirmButton.displayString = this.confirmButton.displayString
                    + ChatColor.GRAY + " (" + TimeUtil.formatDuration(remaining, true) + ")";
        } else {
            boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
            if (!authenticated || this.submitting) {
                this.confirmButton.enabled = false;
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) {
            return;
        }
        if (button.id == 0) {
            ClipboardUtil.copyToClipboard(this.recoveryKey);
        } else if (button.id == 1) {
            this.submitting = true;
            new RegisterRequest(RecoveryKeyGenerator.hash(this.recoveryKey)).toPacket().sendAsync(this.recoveryResponse);
        }
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int centerX = this.width / 2;
        this.drawCenteredString(this.fontRendererObj, "Recovery Key", centerX, 20, new Color(184, 189, 178, 255).getRGB());
        this.drawCenteredString(this.fontRendererObj, "Write this text on a piece of paper and keep it somewhere safe.",
                centerX, 34, new Color(200, 200, 200, 255).getRGB());
        this.drawCenteredString(this.fontRendererObj, "If you lose this key, you might not be able to access your Adjust account later on.",
                centerX, 45, new Color(155, 47, 47, 255).getRGB());
        if (this.keyField != null && this.wrappedKeyLines != null) {
            this.keyField.drawTextBox(mouseX, mouseY);
            int lineY = this.keyField.yPosition + 4;
            for (IChatComponent line : this.wrappedKeyLines) {
                if (this.fontRendererObj != null) {
                    this.fontRendererObj.drawString(line.getFormattedText(),
                            (float) this.keyField.xPosition + 3.0F, (float) lineY, 0xFFFFFFFF, true);
                }
                lineY += 12;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        if (this.recoveryKey == null) {
            this.recoveryKey = RecoveryKeyGenerator.generate();
            this.timer = new Timer();
        }
        this.buttonList.clear();
        int buttonX = this.width / 2 - 100;
        this.copyButton.xPosition = buttonX;
        this.confirmButton.xPosition = buttonX;
        this.buttonList.add(this.copyButton);
        this.buttonList.add(this.confirmButton);

        this.keyField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 130, 66, 260, 60);
        this.keyField.setFocused(true);
        this.keyField.setMaxStringLength(64);
        if (this.fontRendererObj != null) {
            this.wrappedKeyLines = GuiUtilRenderComponents.splitText(
                    new ChatComponentText(this.recoveryKey), 260, this.fontRendererObj, false, false);
            int textHeight = 12 * this.wrappedKeyLines.size();
            this.keyField.height = textHeight + 4;
            this.copyButton.yPosition = textHeight + 80;
            this.confirmButton.yPosition = textHeight + 104;
        }
    }

    private void finishRecovery() {
        ServiceUtil.recoveryRequested = false;
        if (this.mc != null) {
            this.mc.displayGuiScreen(new AdjustMainMenu());
        }
    }

    @Override
    public int getMaxFps() {
        if (!AdjustMainMenu.windowActive) {
            return 60;
        }
        return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate);
    }
}
