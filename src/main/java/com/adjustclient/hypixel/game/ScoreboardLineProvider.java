package com.adjustclient.hypixel.game;

import com.adjustclient.game.server.GameCountdown;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public interface ScoreboardLineProvider {
    default Pair<GameCountdown, Boolean> parseCountdown(S3EPacketTeams packet) {
        if (packet.getPlayers() != null && !packet.getPlayers().isEmpty()
                || !packet.getName().startsWith("team_")) return Pair.of(null, false);
        String prefix = packet.getPrefix();
        if (prefix.isEmpty()) return Pair.of(null, true);
        if (!prefix.contains(" ")) return Pair.of(null, false);
        String[] words = prefix.split(" ");
        if (words.length == 0) return Pair.of(null, false);
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            label.append(words[i]);
            if (i < words.length - 2) label.append(" ");
        }
        String name = StringUtils.stripControlCodes(label.toString().replaceAll(":", ""));
        String time = StringUtils.stripControlCodes(words[words.length - 1] + packet.getSuffix());
        if (time == null || !time.contains(":")) return Pair.of(null, false);
        String[] parts = time.split(":");
        if (parts.length == 0) return Pair.of(null, false);
        try {
            int minutes = Integer.parseInt(parts[0]);
            if (parts.length < 2) return Pair.of(null, false);
            int seconds = Integer.parseInt(parts[1]);
            return Pair.of(new GameCountdown(name, 1000 * (seconds + 60 * minutes)), true);
        } catch (NumberFormatException ignored) {
            return Pair.of(null, false);
        }
    }
}
