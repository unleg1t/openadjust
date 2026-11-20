package com.adjustclient.managers.servertracker;

import com.adjustclient.managers.user.ClientUser;

public class TrackedFriend {
    public String name;
    public String server;
    public boolean hidden;
    public ClientUser user;

    public TrackedFriend(String name, String server, boolean hidden) {
        this.name = name;
        this.server = server;
        this.hidden = hidden;
    }
}
