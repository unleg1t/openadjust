package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum RepositoryInviteError {
    ALREADY_USER, ALREADY_INVITED, INVITE_SETTINGS, NOT_ONLINE, LIMIT_REACHED, BLOCKED;
    public BiConsumer<NotificationBuilder, String> handler;
    public static final BiConsumer<NotificationBuilder, String> lambda_1ff26bc4998f372b6b0815b4e55b19910 = (notification, name) -> notification.format("{} is already in this repository.", name);
    public static final BiConsumer<NotificationBuilder, String> lambda_293f15e2fa5930f464eb3233622992490 = (notification, name) -> notification.format("You already have an invite pending to {}.", name);
    public static final BiConsumer<NotificationBuilder, String> lambda_5f82178ae4b308f5c43453bd31c0cc5d0 = (notification, name) -> notification.format("You cannot invite {} because of their invite settings.", name);
    public static final BiConsumer<NotificationBuilder, String> lambda_e22f85e8f6b094315725cc3d754bb31c0 = (notification, name) -> notification.format("This repository has reached maximum users.", name);
    public static final BiConsumer<NotificationBuilder, String> lambda_e57e7a778d25b86bdf4d8fd890634e000 = (notification, name) -> notification.format("You cannot invite {} because they have blocked you.", name);
    public static final BiConsumer<NotificationBuilder, String> lambda_f9876fd6514d514ae4d8766141bcc6f00 = (notification, name) -> notification.format("You cannot invite {} because they aren't online.", name);
    static {
        ALREADY_USER.handler = lambda_1ff26bc4998f372b6b0815b4e55b19910;
        ALREADY_INVITED.handler = lambda_293f15e2fa5930f464eb3233622992490;
        INVITE_SETTINGS.handler = lambda_5f82178ae4b308f5c43453bd31c0cc5d0;
        NOT_ONLINE.handler = lambda_f9876fd6514d514ae4d8766141bcc6f00;
        LIMIT_REACHED.handler = lambda_e22f85e8f6b094315725cc3d754bb31c0;
        BLOCKED.handler = lambda_e57e7a778d25b86bdf4d8fd890634e000;
    }
}
