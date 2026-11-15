package com.adjustclient.util.anticheat.model.check;
import com.adjustclient.util.anticheat.model.AntiCheatCheck;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.util.ChatColor;
import net.minecraft.util.StringUtils;
import java.util.Locale;
public final class ScoreboardCheck extends AntiCheatCheck {
    public Object field8;
    public boolean lacksExpectedColour(String text) {
        return (!text.contains(ChatColor.YELLOW.toString())
                && !text.contains(ChatColor.WHITE.toString()))
                || !text.contains(ChatColor.BOLD.toString());
    }
    public boolean onPacket(Packet<?> packet) {
        if (!(packet instanceof S3BPacketScoreboardObjective)) return false;
        String title = ((S3BPacketScoreboardObjective) packet).func_149337_d();
        if (title == null || lacksExpectedColour(title)) return false;
        String stripped = StringUtils.stripControlCodes(title);
        return stripped.equals(stripped.toUpperCase(Locale.ENGLISH));
    }
}
