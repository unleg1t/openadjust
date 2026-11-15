package com.adjustclient.game.movable;

import com.adjustclient.Adjust;
import com.adjustclient.bind.BindMode;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.HudMouseRenderEvent;
import com.adjustclient.event.ScreenMouseClickEvent;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.PositionSetting;
import com.adjustclient.setting.SettingContainer;
import com.adjustclient.util.ScreenUtil;
import com.adjustclient.util.Vec2f;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector4f;
import org.lwjgl.input.Keyboard;

public final class MovableManager implements Listener {
    public List<SnapGuide> guides = new ArrayList<>(6);
    public List<Movable> movables = new ArrayList<>();
    public AnchorArea[] anchorAreas = new AnchorArea(MovableAnchor.ROOT, 0, 0, 1920, 1080).update();
    public Animation[] guideAnimations = {new Animation(Easing.LINEAR, 85L), new Animation(Easing.LINEAR, 85L)};
    public SnapLine[] lastSnap = new SnapLine[2];
    public Movable lastDragged;
    public Movable dragging;
    public boolean resizePending = true;
    public Object field36;

    public static final class SnapLine {
        public float guide;
        public float position;
        public Object field12;
        public SnapLine(float guide, float position) { this.guide = guide; this.position = position; }
    }

    @EventHandler public void onRender(HudMouseRenderEvent event) {
        ScaledResolution resolution = event.scaledResolution;
        float mouseX = event.getMouseX();
        float mouseY = event.mouseY;
        if (resizePending) {
            AnchorArea[] areas = new AnchorArea(MovableAnchor.ROOT, 0, 0,
                    resolution.getScaledWidth(), resolution.getScaledHeight()).update();
            for (int i = 0; i < anchorAreas.length; i++) {
                AnchorArea old = anchorAreas[i], next = areas[i];
                old.x = next.x; old.y = next.y; old.width = next.width; old.height = next.height;
            }
            resizePending = false;
        }
        boolean snappedX = false, snappedY = false;
        if (dragging != null) {
            if (Minecraft.getMinecraft().currentScreen == null && !ScreenUtil.isAdjustScreenOpen()
                    && !Adjust.inventoryStateTracker.open) { dragging = null; return; }
            if (dragging.canDrag != null && !dragging.canDrag.getAsBoolean()) { dragging = null; return; }
            lastDragged = dragging;
            float x = mouseX + dragging.grabX, y = mouseY + dragging.grabY;
            if (!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                SnapLine horizontal = findSnap(true, resolution, x);
                if (horizontal != null) { lastSnap[0] = horizontal; x = horizontal.position; snappedX = true; }
                SnapLine vertical = findSnap(false, resolution, y);
                if (vertical != null) { lastSnap[1] = vertical; y = vertical.position; snappedY = true; }
            }
            dragging.resolvePosition(x, y);
            if (dragging.drawBounds) {
                NvgContext.pushEvenScale();
                Vec2f position = dragging.updatePosition();
                Vector4f r = NvgContext.toPixels(position.x, position.y, position.x + dragging.width,
                        position.y + dragging.height + 1.0F);
                NvgDraw.strokeRect(true, 1687590550, r.x, r.y, r.z, r.w,
                        (NvgContext.scaleFactor == null ? 1.0F : (NvgContext.roundScaledPixels
                                ? Math.round(NvgContext.scaleFactor)
                                : NvgContext.scaleFactor.intValue())) * 0.5F);
                NvgContext.popEvenScale();
            }
        }
        drawGuides(snappedX, snappedY, resolution);
    }

    public void onMouseClick(ScreenMouseClickEvent event, Movable hovered) {
        if (event.mouseButton != 0) return;
        if (event.inputAction != BindMode.PRESSED) { dragging = null; return; }
        if (hovered == null || hovered.updatePosition().y + hovered.headerHeight < event.mouseY) return;
        dragging = hovered;
        hovered.grabX = hovered.updatePosition().x - event.mouseX;
        hovered.grabY = hovered.updatePosition().y - event.mouseY;
    }

    public void drawGuides(boolean snappedX, boolean snappedY, ScaledResolution resolution) {
        guideAnimations[0].update(snappedX ? 1.0F : 0.0F);
        float alphaX = guideAnimations[0].value;
        guideAnimations[1].update(snappedY ? 1.0F : 0.0F);
        float alphaY = guideAnimations[1].value;
        if (alphaX == 0.0F && alphaY == 0.0F) return;
        NvgContext.pushEvenScale();
        if (lastSnap[0] != null) {
            float x = lastSnap[0].guide, start = 0.0F;
            int color = com.adjustclient.render.ColorUtil.withAlpha(new java.awt.Color(150, 150, 150), (int) (alphaX * 100.0F)).getRGB();
            if (lastDragged.drawBounds && (snappedX || dragging == null)) {
                float top = lastDragged.updatePosition().y;
                if (top != 0.0F) {
                    Vector4f r = NvgContext.toPixels(x, 0, x, top);
                    NvgDraw.fillRect(color, r.x - 0.5F, r.y, r.z, r.w - 0.5F);
                    start = top + lastDragged.height + 1.0F;
                }
            }
            Vector4f r = NvgContext.toPixels(x, start, x, resolution.getScaledHeight());
            NvgDraw.fillRect(color, r.x - 0.5F, r.y + 0.5F, r.z, r.w);
        }
        if (lastSnap[1] != null) {
            float y = lastSnap[1].guide, start = 0.0F;
            int color = com.adjustclient.render.ColorUtil.withAlpha(new java.awt.Color(150, 150, 150), (int) (alphaY * 100.0F)).getRGB();
            if (lastDragged.drawBounds && (snappedY || dragging == null)) {
                float left = lastDragged.updatePosition().x;
                if (left != 0.0F) {
                    Vector4f r = NvgContext.toPixels(0, y, left, y + 0.5F);
                    NvgDraw.fillRect(color, r.x, r.y - 0.5F, r.z - 0.5F, r.y);
                    start = left + lastDragged.width;
                }
            }
            Vector4f r = NvgContext.toPixels(start, y, resolution.getScaledWidth(), y + 0.5F);
            NvgDraw.fillRect(color, r.x, r.y - 0.5F, r.z, r.y);
        }
        NvgContext.popEvenScale();
    }

    public SnapLine findSnap(boolean horizontal, ScaledResolution resolution, float position) {
        List<SnapGuide> points = new ArrayList<>(guides);
        for (Movable movable : movables) {
            if (movable == lastDragged || movable.canDrag != null && !movable.canDrag.getAsBoolean()) continue;
            Vec2f p = movable.updatePosition();
            SnapGuide topLeft = new SnapGuide();
            topLeft.x = p.x; topLeft.y = p.y; topLeft.active = true; topLeft.field17 = true;
            points.add(topLeft);
            p = movable.updatePosition();
            SnapGuide bottomRight = new SnapGuide();
            bottomRight.x = p.x + movable.width + 0.5F;
            bottomRight.y = p.y + movable.height + 1.5F;
            bottomRight.active = true; bottomRight.field17 = true;
            points.add(bottomRight);
        }
        List<float[]> coordinates = new ArrayList<>();
        for (SnapGuide point : points) {
            if (!horizontal && !point.active) continue;
            float size = horizontal ? dragging.width + 0.5F
                    : 1.0F + dragging.height + 0.5F / (NvgContext.currentScale / 2.0F);
            float guide = horizontal ? point.x : point.y;
            coordinates.add(new float[] {guide, point.field17 ? guide - size * 0.5F : -1.0F,
                    point.field17 ? guide - size : -1.0F});
        }
        List<SnapLine> candidates = new ArrayList<>();
        for (float[] values : coordinates) {
            for (float value : values) {
                if (value <= 1.0F || value >= (horizontal ? resolution.getScaledWidth() : resolution.getScaledHeight()) - 1.0F) continue;
                candidates.add(new SnapLine(values[0], value));
            }
        }
        candidates.removeIf(line -> Math.abs(line.position - position) >= 1.0F);
        candidates.sort(Comparator.comparingDouble(line -> line.position - position));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    public Movable findHovered(ScreenMouseClickEvent event) {
        for (Movable movable : movables) {
            Vec2f p = movable.updatePosition();
            if (event.mouseX < p.x || event.mouseY < movable.updatePosition().y
                    || event.mouseX > movable.updatePosition().x + movable.width
                    || event.mouseY > movable.updatePosition().y + movable.headerHeight) continue;
            if (movable.canDrag != null && !movable.canDrag.getAsBoolean()) continue;
            if (movable.dragFilter == null || movable.dragFilter.canDrag(event.mouseX, event.mouseY)) return movable;
        }
        return null;
    }

    public Movable registerMovable(String name, Vec2f defaultPosition) {
        float x = defaultPosition.x, y = defaultPosition.y;
        AnchorArea nearest = null;
        if (x == -1.0F && y == -1.0F) {
            nearest = Adjust.movableManager.anchorAreas[4]; x = -200.0F; y = 0.0F;
        } else {
            float distance = Float.MAX_VALUE;
            for (AnchorArea area : Adjust.movableManager.anchorAreas) {
                float dx = x - (area.width / 2.0F + area.x), dy = y - (area.height / 2.0F + area.y);
                float squared = dx * dx + dy * dy;
                if (distance > squared) { distance = squared; nearest = area; }
            }
            MovableAnchor anchor = java.util.Objects.requireNonNull(nearest).anchor;
            if (anchor != MovableAnchor.TOP_LEFT) {
                x = (x - nearest.x) / nearest.width;
                if (!anchor.absoluteY) y = (y - nearest.y) / nearest.height;
            }
        }
        PositionSetting setting = new PositionSetting((SettingContainer) Adjust.INSTANCE.field28,
                "global.movable." + name, new MovablePosition(nearest, x, y));
        Movable movable = new Movable(setting, null);
        movable.drawBounds = true;
        movables.add(movable);
        return movable;
    }

    public SnapGuide createGuide() {
        SnapGuide guide = new SnapGuide();
        guide.field17 = true;
        guides.add(guide);
        return guide;
    }
}
