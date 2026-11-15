package com.adjustclient.game.movable;

import com.adjustclient.Adjust;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.PositionSetting;
import com.adjustclient.util.Vec2f;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.ScaledResolution;

public class Movable {
    public PositionSetting position;
    public Listener dragFilter;
    public BooleanSupplier canDrag;
    public Animation animation;
    public float width;
    public float height;
    public float grabX;
    public float grabY;
    public float headerHeight;
    public boolean drawBounds;

    public interface Listener { boolean canDrag(float mouseX, float mouseY); }

    public Movable(PositionSetting position, Listener dragFilter) {
        this.position = position; this.dragFilter = dragFilter; this.drawBounds = true;
    }

    public void drawOverlay(ScaledResolution resolution) {
        boolean dragging = Adjust.movableManager.dragging == this;
        if (!dragging && animation == null) return;
        if (animation == null) animation = new Animation(Easing.LINEAR, 160L);
        animation.update(dragging ? 1.0F : 0.0F);
        float alpha = Math.min(animation.value, 1.0F);
        if (alpha <= 0.0F) {
            if (!dragging) animation = null;
            return;
        }
        NvgDraw.fillRect(((int) (alpha * 100.0F) + 1) << 24 | 0x1e1e1e,
                0.0F, 0.0F, (float) resolution.getScaledWidth_double(), (float) resolution.getScaledHeight_double());
    }

    public void resolvePosition(float x, float y) {
        AnchorArea nearest = null;
        float distance = Float.MAX_VALUE;
        for (AnchorArea area : Adjust.movableManager.anchorAreas) {
            float dx = x + width / 2.0F - (area.width / 2.0F + area.x);
            float dy = y + height / 2.0F - (area.height / 2.0F + area.y);
            float squared = dx * dx + dy * dy;
            if (distance > squared) { distance = squared; nearest = area; }
        }
        Objects.requireNonNull(nearest);
        MovableAnchor anchor = nearest.anchor;
        float relativeY = y;
        if (anchor != MovableAnchor.TOP_LEFT) {
            if (anchor.bottom) relativeY += height;
            if (anchor.right) x += width;
            if (anchor == MovableAnchor.CENTER) { x += width / 2.0F; relativeY += height / 2.0F; }
            x = (x - nearest.x) / nearest.width;
            if (!anchor.absoluteY) relativeY = (relativeY - nearest.y) / nearest.height;
            else relativeY = y;
        }
        MovablePosition next = new MovablePosition(nearest, x, relativeY);
        position.setValue(next);
    }

    public Vec2f updatePosition() { return position.getValue().resolve(width, height); }
}
