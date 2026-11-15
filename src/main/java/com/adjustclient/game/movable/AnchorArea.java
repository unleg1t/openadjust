package com.adjustclient.game.movable;

public class AnchorArea {
    public MovableAnchor anchor;
    public float x;
    public float y;
    public float width;
    public float height;
    public AnchorArea(MovableAnchor anchor, float x, float y, float width, float height) {
        this.anchor = anchor;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public AnchorArea[] update() {
        float halfWidth = width / 2.0F, halfHeight = height / 2.0F;
        return new AnchorArea[] {
            new AnchorArea(MovableAnchor.TOP_LEFT, x, y, halfWidth, halfHeight),
            new AnchorArea(MovableAnchor.TOP_RIGHT, x + halfWidth, y, halfWidth, halfHeight),
            new AnchorArea(MovableAnchor.BOTTOM_LEFT, x, y + halfHeight, halfWidth, halfHeight),
            new AnchorArea(MovableAnchor.BOTTOM_RIGHT, x + halfWidth, y + halfHeight, halfWidth, halfHeight),
            new AnchorArea(MovableAnchor.CENTER, x, y, width, height)
        };
    }
}
