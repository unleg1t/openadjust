package com.adjustclient.backend.transport;

public enum HydraMessageType {
    REPOSITORY("Repository Invite"),
    CONFIGURATION_SEND("Configuration"),
    FRIEND_REQUEST("Friend Request");

    public final String title;
    private Object field20;

    HydraMessageType(String title) {
        this.title = title;
    }
}
