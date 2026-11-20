package com.adjustclient.managers.party.chat;

import com.adjustclient.core.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class PositionQuickMessage extends QuickMessage {
    public PositionQuickMessage(String name, String description) { super(name, description); }

    @Override public String buildText() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return "(none)";
        double x = player.posX == -0.0D ? 0.0D : player.posX;
        double y = player.posY == -0.0D ? 0.0D : player.posY;
        double z = player.posZ == -0.0D ? 0.0D : player.posZ;
        return NumberSetting.FORMAT.format(x) + ", " + NumberSetting.FORMAT.format(y)
                + ", " + NumberSetting.FORMAT.format(z);
    }
}
