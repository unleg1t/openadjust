package com.adjustclient.util.anticheat.player.kill;

import com.adjustclient.util.anticheat.player.KillMessageParser;
import com.adjustclient.util.anticheat.player.KillEvent;
import java.util.List;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public final class TwoPlayerKillParser extends KillMessageParser {
    public Object field4;
    @Override public KillEvent parse(IChatComponent message) {
        List<IChatComponent> s = message.getSiblings();
        int count = s.size();
        if (count != 2 && count != 4 && count != 5) return null;
        String head = s.get(0).getUnformattedTextForChat();
        if (!head.endsWith(" ")) return null;
        String victim = head.trim();
        if (!victim.matches("^\\w{2,16}$")) return null;
        if (count == 2) {
            if (s.get(1).getChatStyle().getColor() != ChatColor.GRAY) return null;
            return new KillEvent(victim, null, false);
        }
        if (count == 4 && s.get(1).getChatStyle().getColor() != ChatColor.GRAY) return null;
        String killer = s.get(2).getUnformattedTextForChat().trim();
        if (!killer.matches("^\\w{2,16}$") || s.get(3).getChatStyle().getColor() != ChatColor.GRAY) return null;
        if (count == 4) return new KillEvent(victim, killer, false);
        if (s.get(4).getChatStyle().getColor() != ChatColor.AQUA || !s.get(4).getChatStyle().getBold()) return null;
        return new KillEvent(victim, killer, true);
    }
}
