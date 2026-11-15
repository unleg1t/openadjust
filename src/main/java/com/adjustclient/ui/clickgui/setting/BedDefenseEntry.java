package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.service.party.ui.AdjustContextMenu;
import com.adjustclient.service.party.ui.ContextMenuAction;
import com.adjustclient.ui.clickgui.Modifier;
import com.adjustclient.ui.screen.AdjustComponent;

public final class BedDefenseEntry extends Modifier {

    final BedDefenseSettingComponent owner;
    final Animation fade;

    public BedDefenseEntry(BedDefenseSettingComponent owner, Animation fade) {
        this.owner = owner;
        this.fade = fade;
    }

    @Override public boolean mouseClicked(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (!pressed) {
            return false;
        }
        AdjustContextMenu menu = this.owner.menu;
        if (menu == null) {
            if (button == 0 || button == 1) {
                this.owner.closeMenu(null);
                return true;
            }
            return false;
        }
        boolean consumed = false;
        boolean alreadyMatched = false;
        for (com.adjustclient.service.party.ui.ContextMenuEntry entry : menu.entries) {
            ContextMenuAction b = (ContextMenuAction) entry;
            boolean inside = mouse.x >= b.x && mouse.x <= b.x + 50.0F && mouse.y >= b.y && mouse.y <= b.y + b.height;
            if (!alreadyMatched && inside) {
                b.hovered = true;
                alreadyMatched = true;
                if (button == 0) {
                    b.action.run();
                    this.owner.closeMenu(null);
                    consumed = true;
                } else if (button != -1) {
                    consumed = true;
                }
            } else {
                b.hovered = false;
            }
        }
        if (consumed) {
            return true;
        }
        if (button == 0 || button == 1) {
            this.owner.closeMenu(null);
            return true;
        }
        return false;
    }

    @Override public void render(net.minecraft.client.gui.ScaledResolution screen, com.adjustclient.event.Render2DEvent queue,
                                 Object drawList, com.adjustclient.util.Vec2f mouse, boolean firstPass) {
        if (this.owner.menu == null) {
            return;
        }
        this.owner.positionMenu();
        if (this.owner.menu == null) {
            return;
        }
        int alpha = (int) this.fade.update(115.0F);
        NvgDraw.fillRect(alpha << 24, 0.0F, 0.0F, (float) screen.getScaledWidth(), (float) screen.getScaledHeight()); // Color(0, 0, 0, alpha)
        this.owner.menu.updateHover(mouse.x, mouse.y);
        this.owner.menu.render();
    }
}
