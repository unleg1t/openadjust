package com.adjustclient.module.impl.render.hud.widget;

import com.adjustclient.event.InputEvent;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.hud.layout.Point2f;
import com.adjustclient.util.Timer;
import net.minecraft.util.ChatColor;

import java.awt.Color;
import com.adjustclient.game.movable.Movable;

public final class ClickSpeedWidget extends HudWidget {

    ClickTracker[] trackers;

    public ClickSpeedWidget(Movable movable) {
        super("Click Speed", null, movable);
        this.trackers = new ClickTracker[] {
                new ClickTracker(this, new com.adjustclient.render.animation.Animation(
                        com.adjustclient.render.animation.Easing.LINEAR, 220L)),
                new ClickTracker(this, new com.adjustclient.render.animation.Animation(
                        com.adjustclient.render.animation.Easing.LINEAR, 220L))
        };
    }

    /** Mouse click handler: flashes the button's tracker and records the click time. */
    @com.adjustclient.event.EventHandler
    public void onClick(InputEvent event) {
        if (!this.isActive()) {
            return;
        }
        ClickTracker tracker = this.trackers[event.action.ordinal()];
        tracker.animation.value = 1.0F;
        tracker.animation.startedAt = System.currentTimeMillis();
        tracker.animation.from = tracker.animation.value;
        tracker.animation.finished = false;
        tracker.clicks.add(new Timer());
    }

    /** Render2D handler. */
    @com.adjustclient.event.EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!this.isActive()) {
            return;
        }
        this.drawFrame(event.scaledResolution, 50.0F, 17.0F);
        com.adjustclient.util.Vec2f position = this.movable.updatePosition();
        float x = position.x;
        float y = position.y;
        int labelColor = new Color(215, 219, 213, 215).getRGB();
        ClickTracker left = this.trackers[0];
        ClickTracker right = this.trackers[1];
        Fonts.TAHOMA_12.drawString(ChatColor.BOLD + "Left", x + 4.5F, y + 13.0F, labelColor);
        String leftCps = left.getCps() + " CPS";
        Fonts.TAHOMA_10.drawString(leftCps, x + 11.0F - Fonts.TAHOMA_10.getStringWidth(leftCps) * 0.5F, y + 21.0F, left.getColor());
        Fonts.TAHOMA_12.drawString(ChatColor.BOLD + "Right", x + 28.5F, y + 13.0F, labelColor);
        String rightCps = right.getCps() + " CPS";
        Fonts.TAHOMA_10.drawString(rightCps, x + 37.0F - Fonts.TAHOMA_10.getStringWidth(rightCps) * 0.5F, y + 21.0F, right.getColor());
    }
}
