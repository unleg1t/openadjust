package com.adjustclient.util;

import com.adjustclient.Adjust;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.ServerTracker;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.DataWatcher;
import net.minecraft.potion.PotionEffect;

public final class PotionColorRegistry {
    public static final Int2ObjectOpenHashMap<PotionColorSignature> colors;

    static {
        colors = new Int2ObjectOpenHashMap<>();
        register(0);
        register(7952296, 0x000a0000, 0x00160000);
        register(13458603, 0x000a0000);
        register(8351897, 0x00010001, 0x00050000, 0x000a0000, 0x00160000);
        register(8356754, 0x000e0000);
        register(7489885, 0x000b0001, 0x00160000);
        register(9061729, 0x00050003, 0x000a0001, 0x000d0002, 0x00150000);
        register(8472187, 0x00050000, 0x000a0000, 0x00160000);
        register(2445989, 0x00160000);
        register(8171462, 0x00010000);
        register(14981690, 0x000c0000);
        register(10044730, 0x000b0000);
        register(5198233, 0x000e0000, 0x00100000);
        register(10048346, 0x00050001, 0x00060000, 0x000e0002);
        register(8998513, 0x000a0001, 0x000b0003, 0x00160001);
        register(10115947, 0x00020000, 0x00060002, 0x000d0000, 0x000e0003);
        register(10251651, 0x000a0000, 0x000c0000, 0x00160000);
        register(8681071, 0x000c0000, 0x00160000);
        register(6262971, 0x00010001, 0x00160000);
        register(5149489, 0x00130000);
        register(8029111, 0x00010001, 0x000a0000, 0x00160000);
        register(9722025, 0x000a0003, 0x00160001);
        register(9933756, 0x00010001, 0x000a0000);
        register(8746919, 0x00010004, 0x00070000, 0x000a0002, 0x00100000);
        register(10645122, 0x000a0001, 0x00130000);
        register(10397847, 0x00010001, 0x000c0000);
        register(8688538, 0x00010001, 0x00130000, 0x000a0000);
        register(7828872, 0x00010004, 0x00020003, 0x000c0004, 0x00100004);
        register(9668765, 0x00010001, 0x000a0001, 0x00130000);
        register(10782136, 0x00010000, 0x000a0000);
        register(11550823, 0x000a0000, 0x00050000);
        register(14187378, 0x000a0000, 0x000c0000);
        register(8169147, 0x00010003, 0x000e0000);
        register(11037069, 0x000a0001, 0x000c0000, 0x00160000);
        register(13922437, 0x000c0000, 0x000a0001);
        register(9718663, 0x00160000, 0x000a0001, 0x00050000);
        register(9851004, 0x00160000, 0x000a0001, 0x000b0001);
        register(7762567, 0x00160000, 0x00010001, 0x000b0001);
        register(9202844, 0x000a0001, 0x00160000, 0x00010001, 0x00050000);
        register(9075636, 0x000a0001, 0x00010001, 0x00160000);
        register(8618127, 0x00010001, 0x00050000);
        register(10580634, 0x000a0001, 0x00010001, 0x00050000);
    }

    public static void updatePotionEffects(Entity entity, List<DataWatcher.WatchableObject> metadata) {
        if (!(entity instanceof EntityPlayer)) return;
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != ServerType.HYPIXEL) return;
        if (Adjust.gameStateTracker.state.startTimer == null) return;
        EntityPlayer player = (EntityPlayer) entity;
        EntityPlayer localPlayer = Minecraft.getMinecraft().thePlayer;
        if (localPlayer != null && localPlayer.getEntityId() == player.getEntityId()) return;
        for (DataWatcher.WatchableObject watched : metadata) {
            if (watched.getDataValueId() != 7 || !(watched.getObject() instanceof Integer)) continue;
            PotionColorSignature signature = colors.get(((Integer) watched.getObject()).intValue());
            player.getActivePotionEffects().clear();
            if (signature != null) {
                for (PotionColorEffect effect : signature.effects) {
                    player.addPotionEffect(new PotionEffect(effect.potion.getId(), Integer.MAX_VALUE,
                            effect.amplifier, false, true));
                }
            }
            return;
        }
    }

    public static void register(int color, int... packedEffects) {
        if (colors.get(color) != null) {
            throw new RuntimeException("Duplicate color: " + color);
        }
        colors.put(color, new PotionColorSignature(packedEffects));
    }
}
