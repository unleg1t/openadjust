package com.adjustclient.hypixel.game;

import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.util.IChatComponent;

public final class HousingGameState extends HypixelGameState {
    public String houseName;
    public String owner;
    public int field52 = 30;
    public int field56 = 40;
    public Object field60;
    public HousingGameState() { super(HypixelGame.HOUSING); }

    public void parseFooter(IChatComponent footer) {
        if (owner != null) return;
        List<IChatComponent> lines = footer.getSiblings();
        if (lines.size() < 7) return;
        List<IChatComponent> parts = lines.get(1).getSiblings();
        String first = parts.get(0).getUnformattedTextForChat();
        String last = parts.get(parts.size() - 1).getUnformattedTextForChat();
        if (!"You are in ".equals(first)) return;
        String[] words = last.split("\\s+");
        owner = words[words.length - 1];
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < parts.size(); i++) {
            String text = parts.get(i).getUnformattedTextForChat();
            if (text.equals(", by ")) break;
            name.append(text);
        }
        houseName = name.toString();
    }

    @Override public void onPacket(Packet<?> packet, boolean before) {
        super.onPacket(packet, before);
        if (before && packet instanceof S47PacketPlayerListHeaderFooter && Boolean.FALSE.equals(lobby)) {
            parseFooter(((S47PacketPlayerListHeaderFooter) packet).getFooter());
        }
    }

    @Override public void onUpdate() {
        if (Boolean.FALSE.equals(lobby) && owner == null) {
            IChatComponent footer = ServerTracker.INSTANCE.footer;
            if (footer != null) parseFooter(footer);
        }
    }
}
