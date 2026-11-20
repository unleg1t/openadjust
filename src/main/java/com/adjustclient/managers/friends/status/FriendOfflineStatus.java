package com.adjustclient.managers.friends.status;

import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import java.util.ArrayList;
import com.adjustclient.managers.config.user.UserConfiguration;

public class FriendOfflineStatus extends FriendStatusChange {
    public final ClientUser user;
    public Object field8;

    public FriendOfflineStatus(ClientUser user) { this.user = user; }

    @Override
    public void apply() {
        if (user.friend.online != false) {
            user.friend.online = false;
            user.friend.statusChangedAt = System.currentTimeMillis();
        }
        user.friend.server = null;
        if (!UserConfiguration.onlineNotifications.isEnabled()) return;
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = "Friends";
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.format("Your friend {} is now offline.", user.name);
        notification.lifetimeSteps = 60;
        notification.key = "friend." + user.name;
        notification.show();
    }
}
