package com.adjustclient.utilities;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;
import net.minecraft.util.ChatColor;

public final class ChatMessage {
    public IChatComponent component;
    public ChatColor color;
    public ClickEvent clickEvent;
    public HoverEvent hoverEvent;
    public boolean strikethrough;
    public boolean field21;
    public boolean bold;
    public boolean italic;
    public boolean obfuscated;
    public Object field28;

    public ChatMessage() { }
    public ChatMessage(IChatComponent component) { this.component = component; color = ChatColor.WHITE; }
    public IChatComponent getComponent() {
        if (component == null) component = new ChatComponentText("");
        return component;
    }
    public ChatMessage withClickToOpenHover() {
        ChatMessage tooltip = new ChatMessage();
        tooltip.color = ChatColor.WHITE;
        tooltip.append("Click to open!");
        hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip.getComponent());
        return this;
    }
    public ChatMessage format(String format, Object... args) {
        StringBuilder literal = new StringBuilder();
        int arg = 0;
        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (ch == '{' && i + 1 < format.length() && format.charAt(i + 1) == '}') {
                String value = arg < args.length ? String.valueOf(args[arg++]) : "{}";
                if (literal.length() != 0) {
                    color = ChatColor.GRAY;
                    append(literal.toString());
                    literal.setLength(0);
                }
                color = ChatColor.WHITE;
                append(value);
                i++;
            } else literal.append(ch);
        }
        if (literal.length() != 0) {
            color = ChatColor.GRAY;
            append(literal.toString());
        }
        return this;
    }
    public ChatMessage append(Object value) {
        IChatComponent next = value instanceof IChatComponent ? (IChatComponent) value
                : new ChatComponentText(String.valueOf(value));
        if (color != null || strikethrough || bold || italic || obfuscated || clickEvent != null || hoverEvent != null) {
            ChatStyle style = new ChatStyle();
            if (color != null) style.setColor(color);
            color = null;
            if (strikethrough) { style.setStrikethrough(true); strikethrough = false; }
            if (obfuscated) { style.setObfuscated(true); obfuscated = false; }
            if (bold) { style.setBold(true); bold = false; }
            if (italic) { style.setItalic(true); italic = false; }
            style.setChatClickEvent(clickEvent);
            clickEvent = null;
            style.setChatHoverEvent(hoverEvent);
            hoverEvent = null;
            next.setChatStyle(style);
        }
        if (component == null) {
            ChatStyle parent = new ChatStyle();
            next.getChatStyle().setParentStyle(parent);
            component = new ChatComponentText("").setChatStyle(parent);
        }
        component.appendSibling(next);
        return this;
    }
}
