package com.adjustclient.event;

public final class HotbarItemRenderEvent extends Event {
    public int slot;
    public int x;
    public int y;

    public HotbarItemRenderEvent(int slot, int x, int y) {
        this.slot = slot;
        this.x = x;
        this.y = y;
    }
}
