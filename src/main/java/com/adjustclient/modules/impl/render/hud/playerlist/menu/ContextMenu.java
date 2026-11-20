package com.adjustclient.modules.impl.render.hud.playerlist.menu;

import com.adjustclient.core.rendering.font.Fonts;
import net.minecraft.client.Minecraft;

import java.util.List;

public abstract class ContextMenu extends HoverMenu {

    public List<MenuGroup> groups;

    public float layout(float x, float y) {
        float cy = y;
        float height = 0.0F;
        boolean first = true;
        for (MenuGroup group : this.groups) {
            boolean groupHidden = group.field12 != null && group.field12.test(this.owner.entry, this.owner.trackedPlayer);
            group.visible = !groupHidden;
            if (group.visible) {
                if (first) {
                    height += 2.5F;
                    first = false;
                }
                group.x = x;
                group.y = cy;
                height += 6.5F;
                cy += 6.5F;
                for (MenuItem menuItem : group.items) {
                    boolean hidden = menuItem.visible != null && menuItem.visible.test(this.owner.entry, this.owner.trackedPlayer);
                    menuItem.enabled = hidden;
                    if (!hidden) {
                        menuItem.x = x;
                        menuItem.y = cy;
                        cy += 5.5F;
                        height += 5.5F;
                    }
                }
            }
        }
        if (this.item != null) this.item.y = cy - 5.5F;
        return height;
    }

    /** Finds the item under the mouse and runs its callback. */
    public void click(float mouseX, float mouseY) {
        for (MenuGroup group : this.groups) {
            if (!group.visible) {
                continue;
            }
            for (MenuItem menuItem : group.items) {
                if (!menuItem.enabled && menuItem.hovered && menuItem.callback != null) {
                    menuItem.callback.accept(this.owner.entry, this.owner.trackedPlayer);
                    return;
                }
            }
        }
        super.onClick();
    }

    public void render(float x, float y, float width, float alpha) {
        render(x, y, width, -111.0F, -111.0F, alpha);
    }

    /** Draws group titles in (244,244,244), alpha 255 * a, and updates item hover from the actual mouse. */
    @Override
    public void render(float x, float y, float width, float mouseX, float mouseY, float alpha) {
        for (MenuGroup group : this.groups) {
            if (group.visible) {
                Fonts.TAHOMA_10.drawString(group.title, group.x, group.y, (int) (alpha * 255.0F) << 24 | 0xF4F4F4);
                for (MenuItem menuItem : group.items) {
                    if (!menuItem.enabled) {
                        menuItem.render(mouseX, mouseY, alpha);
                    }
                }
            }
        }
        if (this.item != null) {
            this.item.x = (x + width) - this.item.width - 5.0F;
            this.item.render(mouseX, mouseY, alpha);
        }
    }

    /** Closes the menu: clears the open screen/menu reference and closes the player's container screen. */
    public static void close() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null || !(mc.currentScreen instanceof net.minecraft.client.gui.inventory.GuiContainer)) {
            mc.displayGuiScreen(null);
            return;
        }
        mc.thePlayer.closeScreen();
        mc.displayGuiScreen(null);
    }
}
