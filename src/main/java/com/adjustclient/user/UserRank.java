package com.adjustclient.user;

import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

public enum UserRank {
    NORMAL(null, ChatColor.WHITE, null),
    STAFF("\u2726", ChatColor.DARK_AQUA, "Support"),
    ADMIN("\u269d", ChatColor.RED, "Administrator");

    public final String prefix;
    public final ChatColor color;
    public final IChatComponent description;
    private Object field28;

    UserRank(String prefix, ChatColor color, String description) {
        this.prefix = prefix;
        this.color = color;
        if (description == null) {
            this.description = null;
        } else {
            IChatComponent label = new ChatComponentText(description);
            if (color != null) label.setChatStyle(new ChatStyle().setColor(color));
            ChatComponentText root = new ChatComponentText("");
            label.getChatStyle().setParentStyle(root.getChatStyle());
            root.getSiblings().add(label);
            this.description = root;
        }
    }

}
