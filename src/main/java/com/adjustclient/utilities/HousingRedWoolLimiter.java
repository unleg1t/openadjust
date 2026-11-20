package com.adjustclient.utilities;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.Map;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.network.play.server.S23PacketBlockChange;

public final class HousingRedWoolLimiter {
    public static Timer redWoolTimer;
    public static int redWoolChanges;

    public static boolean allowBlockChange(S23PacketBlockChange packet) {
        IBlockState state = packet.getBlockState();
        if (state == null || state.getBlock() != Blocks.wool
                || state.getProperties() == null || state.getProperties().isEmpty()) return true;
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != ServerType.HYPIXEL) return true;
        if (Adjust.gameStateTracker.state.game != HypixelGame.HOUSING) return true;
        for (Map.Entry<IProperty, Comparable> property : state.getProperties().entrySet()) {
            if (!property.getKey().getName().equals("color")) continue;
            if (property.getValue() != EnumDyeColor.RED) return true;
            if (redWoolTimer == null) {
                redWoolTimer = new Timer();
            } else if (redWoolTimer.hasElapsed(400L)) {
                redWoolChanges = 0;
                redWoolTimer.reset();
            }
            return ++redWoolChanges < 25;
        }
        return true;
    }
}
