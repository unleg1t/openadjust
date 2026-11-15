package com.adjustclient.service.party.ui;

import com.adjustclient.bind.BindMode;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.RenderUtil;
import org.joml.Vector4f;
import com.adjustclient.render.font.*;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.setting.BedDefenseSettingComponent;
import com.adjustclient.ui.friends.FriendGroup;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class AdjustContextMenu {
    public Object owner;
    public Object source;
    public List<ContextMenuEntry> entries = new ArrayList<>();
    public float x;
    public float y;
    public float width;
    public float height;

    public AdjustContextMenu(Object owner, float x, float y) { this(owner, null, x, y); }
    public AdjustContextMenu(Object owner, Object source, float x, float y) {
        this.owner = owner; this.source = source; this.x = x; this.y = y;
    }

    public boolean handleMouse(int button, BindMode mode, float mouseX, float mouseY) {
        boolean found = false, handled = false;
        for (ContextMenuEntry entry : entries) {
            boolean hit = !found && mouseX >= entry.x && mouseX <= entry.x + 50.0F
                    && mouseY >= entry.y && mouseY <= entry.y + entry.height;
            entry.hovered = hit;
            if (!hit) continue;
            found = true;
            if (button != -1) handled = true;
            if (mode == BindMode.PRESSED && button == 0) {
                ((ContextMenuAction) entry).action.run();
                if (owner instanceof PartyPanel) ((PartyPanel) owner).contextMenu = null;
                else if (owner instanceof FriendGroup) ((FriendGroup) owner).contextMenu = null;
                else if (owner instanceof BedDefenseSettingComponent) {
                    BedDefenseSettingComponent component = (BedDefenseSettingComponent) owner;
                    component.closeMenu(null);
                }
            }
        }
        return handled;
    }

    public void render() {
        layout();
        NvgContext.pushEvenScale();
        Vector4f bounds = NvgContext.toPixels(x, y, x + width, y + height);
        NvgDraw.fillRect(1814571048, bounds.x, bounds.y, bounds.z, bounds.w);
        float lineWidth = NvgContext.scaleFactor == null ? 0.5F
                : (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor)
                        : NvgContext.scaleFactor.intValue()) * 0.5F;
        NvgDraw.strokeRect(false, 1763252505, bounds.x, bounds.y, bounds.z, bounds.w, lineWidth);
        NvgContext.popEvenScale();
        for (int i = 0; i < entries.size(); i++) {
            ContextMenuEntry entry = entries.get(i);
            float inset = 0.5F;
            float left = entry.x + inset, right = entry.x + 50.0F - inset;
            float bottom = entry.y + entry.height - inset;
            if (i != entries.size() - 1)
                NvgDraw.fillRect(1763252505, left, entry.y + entry.height, right, bottom);
            Animation hover = entry.hover;
            if (entry.hovered) {
                RenderUtil.cursor = 221188;
                hover.duration = 50L;
                hover.update(1.0F);
            } else {
                hover.duration = 80L;
                hover.update(0.0F);
            }
            if (hover.value != 0.0F) NvgDraw.fillRect(new Color(95, 95, 99, (int)(hover.value * 70.0F)).getRGB(),
                    left, entry.y + (i == 0 ? inset : 0.0F), right, bottom);
            TextComponent.draw(((ContextMenuAction) entry).label, Fonts.TAHOMA_10, entry.x + 3.0F, entry.y + 4.0F);
        }
    }

    public void updateHover(float mouseX, float mouseY) {
        boolean found = false;
        for (ContextMenuEntry entry : entries) {
            entry.hovered = !found && mouseX >= entry.x && mouseX <= entry.x + 50.0F
                    && mouseY >= entry.y && mouseY <= entry.y + entry.height;
            if (entry.hovered) found = true;
        }
    }

    public void layout() {
        float offset = 0.0F;
        for (ContextMenuEntry entry : entries) {
            entry.x = x;
            entry.y = y + offset;
            offset += entry.height;
        }
        height = offset;
        width = 50.0F;
    }
}
