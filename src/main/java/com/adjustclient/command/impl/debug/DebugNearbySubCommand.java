package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public final class DebugNearbySubCommand extends SubCommand {
    public Object field16;

    public DebugNearbySubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        StringBuilder text = new StringBuilder();
        int count = 0;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer)) continue;
            float x = (float) (entity.posX - mc.thePlayer.posX);
            float y = (float) (entity.posY - mc.thePlayer.posY);
            float z = (float) (entity.posZ - mc.thePlayer.posZ);
            float distance = MathHelper.sqrt_float(x * x + y * y + z * z);
            if (distance < 9.0F) {
                text.append(((EntityPlayer) entity).getGameProfile().getName()).append(" (dist ").append(distance).append(")\n");
                ++count;
            }
        }
        text.append("Printed ").append(count).append(" nearby players");
        sendMessage(createMessage().append(text.toString()));
    }
}
