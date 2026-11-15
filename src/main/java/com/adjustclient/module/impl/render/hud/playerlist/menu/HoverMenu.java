package com.adjustclient.module.impl.render.hud.playerlist.menu;

import com.adjustclient.module.impl.render.hud.playerlist.HoverCardState;

public abstract class HoverMenu extends MenuComponent {

    public HoverCardState owner;
    public MenuItem item;

    public void build() { }
    public void click(float mouseX, float mouseY) { onClick(); }
    public abstract float layout(float x, float y);
    public abstract void render(float x, float y, float width, float mouseX, float mouseY, float alpha);

    @Override
    public void onClick() {
        if (this.item == null || !this.item.hovered) {
            return;
        }
        this.item.callback.accept(this.owner.entry, this.owner.trackedPlayer);
    }

    public void renderItem(float x, float y, float width, float alpha) {
        renderItem(x, y, width, -111.0F, -111.0F, alpha);
    }

    public void renderItem(float x, float y, float width, float mouseX, float mouseY, float alpha) {
        if (this.item != null) {
            this.item.x = (x + width) - this.item.width - 5.0F;
            this.item.render(mouseX, mouseY, alpha);
        }
    }
}
