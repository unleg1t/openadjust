package com.adjustclient.service.party.ui;

import com.adjustclient.user.ClientUser;

public class ContextMenuKey {
    public PartyPanel party;
    public ClientUser user;
    public boolean isTyping;
    public ContextMenuKey(PartyPanel party, ClientUser user) { this.party = party; this.user = user; }
    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null || other.getClass() != ContextMenuKey.class) return false;
        return ((ContextMenuKey) other).user == user;
    }
    @Override public int hashCode() { return user.hashCode(); }
}
