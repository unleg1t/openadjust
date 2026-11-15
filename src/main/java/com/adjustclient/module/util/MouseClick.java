package com.adjustclient.module.util;

import net.minecraft.client.settings.KeyBinding;

public class MouseClick {
    public final KeyBinding key;
    public Integer clicks;
    public Boolean down;
    public int holdTicks;
    public boolean disabled;
    protected boolean sampled;
    public boolean previousDown;
    private Object field24;

    public MouseClick(KeyBinding key) { this.key = key; }

    public boolean isClicked(boolean consume) {
        boolean result = !disabled && (down != null ? down : key.isKeyDown() || holdTicks >= 1);
        if (consume) { sampled = true; previousDown = result; }
        return result;
    }

    public boolean isPressed(boolean consume) {
        boolean pressed = consume && key.isPressed();
        if (disabled) return false;
        boolean result = clicks != null ? clicks >= 1 : consume ? pressed : key.pressTime >= 1;
        if (consume && clicks != null) {
            clicks = clicks - 1;
            if (clicks < 1) clicks = null;
        }
        return result;
    }

    public void endTick() {
        if (!sampled) isClicked(true);
        sampled = false;
        clicks = null;
        down = null;
        disabled = false;
        holdTicks = Math.max(holdTicks - 1, 0);
    }
}
