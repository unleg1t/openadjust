package com.adjustclient.utilities.server;

public class ServerInfo {
    public int status;
    public static final ServerInfo INVITED = new ServerInfo(916292);
    public static final ServerInfo UNKNOWN_USER = new ServerInfo(-48102387);
    public static final ServerInfo ALREADY_PENDING = new ServerInfo(174961553);
    public static final ServerInfo SOCIAL_SETTINGS = new ServerInfo(-3181892);
    public static final ServerInfo INVITE_SETTINGS = new ServerInfo(37829901);
    public static final ServerInfo BLOCKED = new ServerInfo(37829611);
    public static final ServerInfo OFFLINE = new ServerInfo(37192676);
    public static final ServerInfo ALREADY_IN_PARTY = new ServerInfo(67781889);
    public static final ServerInfo[] VALUES = { INVITED, UNKNOWN_USER, ALREADY_PENDING, SOCIAL_SETTINGS,
            INVITE_SETTINGS, BLOCKED, OFFLINE, ALREADY_IN_PARTY };
    public ServerInfo(int status) { this.status = status; }
}
