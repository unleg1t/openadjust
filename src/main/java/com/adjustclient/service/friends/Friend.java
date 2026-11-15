package com.adjustclient.service.friends;

import com.adjustclient.user.ClientUser;
import java.util.ArrayList;
import java.util.List;

public final class Friend {
    public final ClientUser user;
    public String server;
    public Object field12;
    public List<FriendDisplay> messages;
    public boolean online;
    public long statusChangedAt;
    public Object field32;

    public Friend(ClientUser user) { this.user = user; }

    public void addMessage(ClientUser sender, String text) {
        if (messages == null) messages = new ArrayList<>();
        messages.add(new FriendDisplay(sender, text));
    }

    @Override public int hashCode() { return user == null ? 0 : System.identityHashCode(user); }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null || other.getClass() != Friend.class) return false;
        return user == ((Friend) other).user;
    }
}
