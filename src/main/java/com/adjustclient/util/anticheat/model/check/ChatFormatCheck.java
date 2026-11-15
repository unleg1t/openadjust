package com.adjustclient.util.anticheat.model.check;
import com.adjustclient.util.anticheat.model.AntiCheatCheck;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;
import net.minecraft.event.HoverEvent;
public final class ChatFormatCheck extends AntiCheatCheck {
    public Object field8;
    public boolean onPacket(Packet<?> packet) {
        if (!(packet instanceof S47PacketPlayerListHeaderFooter)) return false;
        S47PacketPlayerListHeaderFooter tab = (S47PacketPlayerListHeaderFooter) packet;
        if (!(tab.getHeader() instanceof ChatComponentText)
                || !(tab.getFooter() instanceof ChatComponentText)) return false;
        if (tab.getHeader().getSiblings().isEmpty()) return false;
        IChatComponent first = tab.getHeader().getSiblings().get(0);
        if (first.getSiblings().size() >= 3) return false;
        if (first.getSiblings().isEmpty()) return true;
        IChatComponent child = first.getSiblings().get(0);
        HoverEvent hover = child.getChatStyle().getChatHoverEvent();
        return child.getChatStyle().getColor() == ChatColor.YELLOW
                && child.getChatStyle().getBold()
                && hover != null && hover.getAction() == HoverEvent.Action.SHOW_TEXT;
    }
}
