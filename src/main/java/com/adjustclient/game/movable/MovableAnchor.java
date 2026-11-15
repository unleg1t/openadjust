package com.adjustclient.game.movable;

public enum MovableAnchor {
    TOP_LEFT(false, false, false), TOP_RIGHT(false, true, false),
    BOTTOM_LEFT(true, false, false), BOTTOM_RIGHT(true, true, false),
    CENTER(false, false, false), ROOT(false, false, true);

    public final boolean bottom;
    public final boolean right;
    public final boolean absoluteY;
    MovableAnchor(boolean bottom, boolean right, boolean absoluteY) {
        this.bottom = bottom;
        this.right = right;
        this.absoluteY = absoluteY;
    }
}
