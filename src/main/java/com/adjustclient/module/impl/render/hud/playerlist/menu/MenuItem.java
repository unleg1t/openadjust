package com.adjustclient.module.impl.render.hud.playerlist.menu;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.font.Fonts;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import com.adjustclient.render.ColorUtil;

public class MenuItem {

    public String text;
    public BiConsumer<PlayerEntry, com.adjustclient.util.anticheat.entity.TrackedEntity> callback;
    public BiPredicate<PlayerEntry, com.adjustclient.util.anticheat.entity.TrackedEntity> visible;
    public Animation hoverAnimation;
    public float x;
    public float y;
    public float width;
    public boolean enabled;
    public boolean hovered;

    public MenuItem(String text, BiConsumer<PlayerEntry, com.adjustclient.util.anticheat.entity.TrackedEntity> callback) {
        this(text, callback, null);
    }

    public MenuItem(String text, BiConsumer<PlayerEntry, com.adjustclient.util.anticheat.entity.TrackedEntity> callback, BiPredicate<PlayerEntry, com.adjustclient.util.anticheat.entity.TrackedEntity> hideIf) {
        this.text = text;
        this.callback = callback;
        this.visible = hideIf;
        this.hoverAnimation = new Animation(com.adjustclient.render.animation.Easing.LINEAR, 111L);
        this.width = Fonts.TAHOMA_8.getStringWidth(text);
    }

    public void render(float mouseX, float mouseY, float alpha) {
        int baseAlpha = (int) (alpha * 200.0F);
        Color base = new Color(233, 233, 233, baseAlpha);
        Color hoverColor = new Color(255, 255, 255, (int) (alpha * 255.0F));
        float width = this.width;
        if (mouseX > this.x && mouseY > this.y - 1.5F) {
            this.hovered = (this.x + width > mouseX) && (this.y + 4.0F > mouseY);
        } else {
            this.hovered = false;
        }
        if (this.hovered) {
            com.adjustclient.render.RenderUtil.cursor = Integer.valueOf(221188);       // hand cursor
        }
        // Animation.get(): target 1 when hovered else 0 (ease over its duration), blended base -> hoverColor
        float t = this.hoverAnimation.update(this.hovered ? 1.0F : 0.0F);
        Color color = ColorUtil.blend(hoverColor, base, t);
        Fonts.TAHOMA_8.drawString(this.text, this.x, this.y, color.getRGB());
    }
}
