package com.adjustclient.utilities.sunshine.check;

import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.PacketUtil;
import com.adjustclient.utilities.StringUtil;
import com.adjustclient.utilities.sunshine.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.Packet;
import net.minecraft.util.*;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatColor;

public abstract class SunshineCheck {
    public String name;
    public String description;
    public SunshineAnticheat anticheat;
    public int violations;

    protected SunshineCheck(String name, String description) { this.name = name; this.description = description; }

    public void fail(ViolationDetails details) {
        java.util.Objects.requireNonNull(anticheat);
        ChatMessage message = SunshineAnticheat.createMessage();
        message.color = ChatColor.GRAY;
        message.append("Failed check ");
        ChatMessage tooltipDesc = new ChatMessage();
        tooltipDesc.color = ChatColor.GRAY;
        tooltipDesc.italic = true;
        tooltipDesc.append(String.valueOf(description));
        IChatComponent label = new ChatComponentText(String.valueOf(name));
        label.setChatStyle(new ChatStyle().setColor(ChatColor.RESET).setChatHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltipDesc.getComponent())));
        message.append(label);
        message.color = ChatColor.GRAY;
        message.append(" x" + ++violations);
        if (!details.messages.isEmpty() || !details.values.isEmpty()) {
            ChatMessage tooltip = new ChatMessage();
            AtomicBoolean started = new AtomicBoolean();
            for (String text : details.messages) {
                if (started.get()) tooltip.append("\n");
                tooltip.color = ChatColor.WHITE;
                tooltip.append(text);
                started.set(true);
            }
            details.values.forEach((key, value) -> appendDetail(started, tooltip, key, value));
            message.append(" ");
            message.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip.getComponent());
            message.italic = true;
            message.color = ChatColor.GRAY;
            int count = details.messages.size() + details.values.size();
            message.append("(" + count + " detail" + (count == 1 ? "" : "s") + ")");
        }
    }

    public String formatValue(Object value) {
        if (value instanceof Double || value instanceof Float)
            return StringUtil.THREE_DECIMALS.format(((Number) value).doubleValue());
        return String.valueOf(value);
    }

    public void appendDetail(AtomicBoolean started, ChatMessage message, String key, Object value) {
        if (started.get()) message.append("\n");
        String text = value instanceof Packet ? PacketUtil.describe((Packet<?>) value) : formatValue(value);
        message.color = ChatColor.GRAY;
        message.append(key);
        message.append(" = ");
        message.color = ChatColor.WHITE;
        message.append(text);
        started.set(true);
    }
}
