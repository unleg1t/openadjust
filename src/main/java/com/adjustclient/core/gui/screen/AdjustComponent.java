package com.adjustclient.core.gui.screen;

public class AdjustComponent {
    public float x;
    public float y;
    public float width;
    public float height;
    public Object owner;

    public AdjustComponent() { }
    public AdjustComponent(float x, float y, float width, float height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }
    public boolean intersects(AdjustComponent other, float mouseX, float mouseY) {
        return mouseX >= Math.max(x, other.getX()) && mouseY >= Math.max(y, other.getY())
                && mouseX < Math.min(x + width, other.getX() + other.width)
                && mouseY < Math.min(y + height, other.getY() + other.height);
    }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
    }
}
