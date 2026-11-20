package com.adjustclient.utilities.server;

public enum ServerRegion {
    OWNER(1), ADMIN(1), MEMBER(0), GUEST(0);

    ServerRegion(int field16) { this.field16 = field16; }
    public int field16;
}
