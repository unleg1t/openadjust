package com.adjustclient.modules.impl.player.inventory.click;
public final class WindowClick {
    public final int windowId;
    public final int slot;
    public final int button;
    public final int mode;
    public WindowClick(int windowId, int slot, int button, int mode) {
        this.windowId = windowId; this.slot = slot; this.button = button; this.mode = mode;
    }
}
