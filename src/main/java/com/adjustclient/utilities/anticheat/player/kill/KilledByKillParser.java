package com.adjustclient.utilities.anticheat.player.kill;

import com.adjustclient.utilities.anticheat.player.KillMessageParser;
import com.adjustclient.utilities.anticheat.player.KillEvent;
import java.util.List;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public final class KilledByKillParser extends KillMessageParser {
    public Object field4;
    @Override public KillEvent parse(IChatComponent message) {
        List<IChatComponent> s = message.getSiblings();
        if (s.size() != 2 || s.get(0).getChatStyle().getColor() != ChatColor.GRAY) return null;
        String head = s.get(0).getUnformattedTextForChat();
        if (!head.endsWith(" ")) return null;
        String victim = head.trim();
        if (!victim.matches("^\\w{2,16}$") || s.get(1).getChatStyle().getColor() != ChatColor.GRAY) return null;
        String rest = s.get(1).getUnformattedTextForChat();
        if (!rest.startsWith("was killed by ")) return null;
        String killer = rest.split("was killed by ")[1];
        if (!killer.matches("^\\w{2,16}$")) return null;
        return new KillEvent(victim, killer, false);
    }
}
