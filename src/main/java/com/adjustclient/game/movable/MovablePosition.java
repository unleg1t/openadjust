package com.adjustclient.game.movable;

import com.adjustclient.util.Vec2f;
import net.minecraft.client.gui.ScaledResolution;

public class MovablePosition {
    public static ScaledResolution hudResolution;
    public AnchorArea area;
    public float x;
    public float y;
    public MovablePosition(AnchorArea area, float x, float y) {
        this.area = area;
        this.x = x;
        this.y = y;
    }
    public Vec2f resolve(float width, float height) {
        AnchorArea area = this.area;
        MovableAnchor anchor = area.anchor;
        float x = this.x, y = this.y;
        if (anchor != MovableAnchor.TOP_LEFT) {
            x = x * area.width + area.x;
            y = y * area.height + area.y;
            if (anchor.bottom) y -= height;
            if (anchor.right) x -= width;
            if (anchor.absoluteY) y = this.y;
            if (anchor == MovableAnchor.CENTER) { y -= height / 2.0F; x -= width / 2.0F; }
        }
        ScaledResolution resolution = (ScaledResolution) hudResolution;
        if (resolution != null) {
            x = Math.max(0.0F, Math.min(x, (float) resolution.getScaledWidth() - width));
            y = Math.max(0.0F, Math.min(y, (float) resolution.getScaledHeight() - height));
        }
        return new Vec2f(x, y);
    }
}
