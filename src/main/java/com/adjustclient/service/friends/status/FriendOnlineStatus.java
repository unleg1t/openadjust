package com.adjustclient.service.friends.status;

import com.adjustclient.user.ClientUser;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import java.util.ArrayList;
import com.adjustclient.service.config.user.UserConfiguration;

public class FriendOnlineStatus extends FriendStatusChange {
    public final ClientUser user;
    public Object field8;

    public FriendOnlineStatus(ClientUser user) { this.user = user; }

    @Override
    public void apply() {
        if (user.friend.online != true) {
            user.friend.online = true;
            user.friend.statusChangedAt = System.currentTimeMillis();
        }
        if (!UserConfiguration.onlineNotifications.isEnabled()) return;
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = "Friends";
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.format("Your friend {} just came online.", user.name);
        notification.lifetimeSteps = 70;
        notification.key = "friend." + user.name;
        notification.show();
    }
}
