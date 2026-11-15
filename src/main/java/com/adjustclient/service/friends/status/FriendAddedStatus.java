package com.adjustclient.service.friends.status;

import com.adjustclient.user.ClientUser;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import java.util.ArrayList;

public class FriendAddedStatus extends FriendStatusChange {
    public final ClientUser user;
    public Object field8;

    public FriendAddedStatus(ClientUser user) { this.user = user; }

    @Override
    public void apply() {
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = "Friends";
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.format("You are now friends with {}!", user.name);
        notification.lifetimeSteps = 60;
        notification.show();
    }
}
