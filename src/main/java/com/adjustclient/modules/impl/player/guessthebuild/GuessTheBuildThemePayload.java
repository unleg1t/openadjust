package com.adjustclient.modules.impl.player.guessthebuild;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.game.GuessTheBuildGameState;
import com.adjustclient.managers.payload.PayloadHandler;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.managers.user.ClientUser;
import net.minecraft.network.PacketBuffer;

public final class GuessTheBuildThemePayload implements PayloadHandler {
    public String builder;
    public String theme;

    public GuessTheBuildThemePayload() {
    }

    public GuessTheBuildThemePayload(String builder, String theme) {
        this.builder = builder;
        this.theme = theme;
    }

    @Override
    public boolean handle(ClientUser sender) {
        GuessTheBuildModule module = GuessTheBuildModule.INSTANCE;
        if (!module.isEnabled()) {
            return false;
        }
        if (!(Adjust.gameStateTracker.state instanceof GuessTheBuildGameState state)) {
            return false;
        }
        if (state.field56 == null || !state.field56.equals(builder)) {
            return false;
        }
        new NotificationBuilder(module.getName())
                .addText("Received guess the build theme from ", 0xFFBEBCBD)
                .append(sender.name)
                .addText(".", 0xFFBEBCBD)
                .show();
        module.sharedTheme = theme;
        return true;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeString(builder);
        buffer.writeString(theme);
    }

    @Override
    public void read(PacketBuffer buffer) {
        builder = buffer.readStringFromBuffer(16);
        theme = buffer.readStringFromBuffer(100);
    }
}
