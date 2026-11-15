package com.adjustclient.tag;

public final class PlayerTagData {
    public final String name;
    public final PlayerTag tag;
    public boolean field12;
    public final long time;
    public Object field24;

    public PlayerTagData(String name, PlayerTag tag, long time) {
        this.name = name;
        this.tag = tag;
        this.time = time;
        this.field12 = false;
        this.field24 = null;
    }
}
