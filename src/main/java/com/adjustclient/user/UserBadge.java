package com.adjustclient.user;

import com.adjustclient.util.ChatMessage;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

public enum UserBadge {
    REGULAR(null, ChatColor.WHITE, null),
    MEDIA("YT", ChatColor.RED, "This user creates content with Adjust."),
    SPED("Special Ed", ChatColor.AQUA, null),
    VIP("VIP", ChatColor.GOLD, null),
    GAY("Gay", ChatColor.GRAY, null),
    TROON("Troon", ChatColor.DARK_PURPLE, "LGBTQIA++"),
    NIGGER("Nigger", ChatColor.BLACK, null),
    HACKER("!", ChatColor.OBFUSCATED, null),
    INDIAN("Indian", ChatColor.YELLOW, "Shits on the street, showers haven't been invented for him yet."),
    EGIRL("E-Girl", null, null);

    public final String prefix;
    public final ChatColor color;
    public IChatComponent description;
    private Object field28;

    UserBadge(String prefix, ChatColor color, String description) {
        this.prefix = prefix;
        this.color = color;
        if (description != null) {
            if ("LGBTQIA++".equals(description)) {
                ChatMessage message = new ChatMessage();
                message.color = color;
                message.append(description);
                this.description = message.component;
            } else {
                ChatComponentText label = new ChatComponentText(description);
                if (color != null) label.setChatStyle(new ChatStyle().setColor(color));
                ChatComponentText root = new ChatComponentText("");
                label.getChatStyle().setParentStyle(root.getChatStyle());
                root.getSiblings().add(label);
                this.description = root;
            }
        }
    }

}
