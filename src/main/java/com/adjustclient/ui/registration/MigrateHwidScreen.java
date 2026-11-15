package com.adjustclient.ui.registration;

import com.adjustclient.Adjust;
import com.adjustclient.backend.transport.HydraPacketEncoder;
import com.adjustclient.changelog.Changelog;
import com.adjustclient.util.Timer;
import java.awt.Color;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class MigrateHwidScreen extends GuiScreen {
    private static boolean migrating;

    private final GuiButton migrateButton;
    private final GuiButton backButton;
    public String statusMessage;
    public final Timer statusTimer;
    public int statusAlpha;

    public MigrateHwidScreen() {
        this.migrateButton = new GuiButton(0, 0, 100, 200, 20, "Migrate Hardware ID");
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

        boolean canMigrate = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (canMigrate) {
            if (migrating) {
                canMigrate = false;
            }
        } else {
            migrating = false;
        }
        this.migrateButton.enabled = canMigrate;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == 0) {
            migrating = true;
            long oldHwidHash = 0L;
            long newHwidHash = 0L;
            String hardwareId = "";
            int changelogId = Changelog.latest != null ? Changelog.latest.id : 0;
            byte[] packet = HydraPacketEncoder.encodeMigrateHwid(changelogId, oldHwidHash, newHwidHash, 1, hardwareId);
            if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated) {
                Adjust.hydraConnection.send(packet);
            }
        } else if (button.id == 1) {
            this.openRegisterScreen();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
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
        this.drawCenteredString(this.fontRendererObj, "Migrate Hardware ID", centerX, 20, new Color(184, 189, 178, 255).getRGB());
        this.drawCenteredString(this.fontRendererObj, "If you registered before Oct 5th 2026, your hardware ID may be outdated.", centerX, 53, 10526880);
        this.drawCenteredString(this.fontRendererObj, "This will attempt to update your hardware ID to the new version.", centerX, 63, 10526880);

        boolean authenticated = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (!authenticated) {
            this.drawCenteredString(this.fontRendererObj, "Hydra is unavailable.", centerX, 33, new Color(171, 36, 36).getRGB());
        } else if (this.statusMessage != null && this.statusAlpha > 0 && this.statusAlpha <= 255) {
            this.drawCenteredString(this.fontRendererObj, this.statusMessage, centerX, 33, new Color(171, 36, 36, this.statusAlpha).getRGB());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(this.migrateButton);
        this.buttonList.add(this.backButton);

        int buttonX = this.width / 2 - 100;
        this.migrateButton.xPosition = buttonX;
        this.backButton.xPosition = buttonX;
    }

    public void openRegisterScreen() {
        this.mc.displayGuiScreen(new RegisterScreen());
    }
}
