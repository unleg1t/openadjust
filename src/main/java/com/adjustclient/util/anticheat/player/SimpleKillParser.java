package com.adjustclient.util.anticheat.player;

import com.adjustclient.util.anticheat.player.KillMessageParser;
import com.adjustclient.util.anticheat.player.KillEvent;
import java.util.List;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public final class SimpleKillParser extends KillMessageParser {
    public Object field4;
    @Override public KillEvent parse(IChatComponent message) {
        List<IChatComponent> s = message.getSiblings();
        if (s.size() != 3) return null;
        String victim = s.get(0).getUnformattedTextForChat();
        if (!victim.matches("^\\w{2,16}$") || s.get(0).getChatStyle().getColor() != ChatColor.RED) return null;
        if (s.get(1).getChatStyle().getColor() != ChatColor.GRAY) return null;
        String killer = s.get(2).getUnformattedTextForChat();
        if (!killer.matches("^\\w{2,16}$") || s.get(2).getChatStyle().getColor() != ChatColor.RED) return null;
        return new KillEvent(victim, killer, false);
    }
}
