package com.adjustclient.utilities.hypixel.game;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.util.StringUtils;
public interface AliveCountProvider {
    default boolean a_(S3EPacketTeams packet) {
        if (!packet.getName().startsWith("team_")) return false;
        String text = packet.getPrefix() + packet.getSuffix();
        if (!text.contains(": §a") || !text.contains("/")) return false;
        String[] parts = text.split("/", 2);
        return parts.length >= 2 && Integer.parseInt(StringUtils.stripControlCodes(parts[1])) >= 13;
    }
}
