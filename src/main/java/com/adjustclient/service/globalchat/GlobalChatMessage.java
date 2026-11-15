package com.adjustclient.service.globalchat;

import com.adjustclient.user.ClientUser;
import com.adjustclient.user.UserRank;
import com.adjustclient.user.UserBadge;
import net.minecraft.util.ChatColor;

public class GlobalChatMessage {
    public ClientUser sender;
    public String text;
    public UserRank rank;
    public UserBadge badge;
    public int id;
    public long timestamp;
    public int color;
    public Object field36;

    public GlobalChatMessage(ClientUser sender, String text, UserRank rank, UserBadge badge,
                             int id, long timestamp, int color) {
        this.sender = sender;
        this.text = text;
        this.rank = rank;
        this.badge = badge;
        this.id = id;
        this.timestamp = timestamp;
        this.color = color;
    }

    public GlobalChatMessage(ClientUser sender, String text, int color, byte flags, int id, long timestamp) {
        this.sender = sender;
        this.text = ChatFontStyle.convert(text, ChatColor.WHITE);
        try { rank = UserRank.values()[flags & 15]; }
        catch (Exception exception) { rank = UserRank.NORMAL; }
        try { badge = UserBadge.values()[(flags >> 4) & 15]; }
        catch (Exception exception) { badge = UserBadge.REGULAR; }
        this.id = id;
        this.timestamp = timestamp;
        this.color = color;
    }
}
