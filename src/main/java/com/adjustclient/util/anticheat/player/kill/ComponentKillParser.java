package com.adjustclient.util.anticheat.player.kill;

import com.adjustclient.util.anticheat.player.KillMessageParser;
import com.adjustclient.util.anticheat.player.KillEvent;
import java.util.List;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public final class ComponentKillParser extends KillMessageParser {
    public Object field4;
    @Override public KillEvent parse(IChatComponent message) {
        List<IChatComponent> s = message.getSiblings();
        int count = s.size();
        if (count != 3 && count != 5 && count != 6) return null;
        if (s.get(0).getChatStyle().getColor() != ChatColor.YELLOW) return null;
        if (count == 3) {
            String victim = s.get(1).getUnformattedTextForChat();
            if (s.get(2).getChatStyle().getColor() != ChatColor.YELLOW) return null;
            return new KillEvent(victim, null, true);
        }
        if (!s.get(0).getUnformattedTextForChat().isEmpty()) return null;
        int victimIndex = count == 5 ? 1 : 2;
        if (count == 6 && s.get(1).getChatStyle().getColor() != ChatColor.YELLOW) return null;
        String victim = s.get(victimIndex).getUnformattedTextForChat();
        if (s.get(victimIndex + 1).getChatStyle().getColor() != ChatColor.YELLOW) return null;
        String killer = s.get(victimIndex + 2).getUnformattedTextForChat();
        if (s.get(victimIndex + 3).getChatStyle().getColor() != ChatColor.YELLOW) return null;
        return new KillEvent(victim, killer, true);
    }
}
