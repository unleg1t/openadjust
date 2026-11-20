package com.adjustclient.modules.impl.render.hud.widget;

import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.hud.layout.Point2f;
import net.minecraft.util.ChatColor;

import java.awt.Color;
import com.adjustclient.utilities.game.movable.Movable;

public final class MemoryUsageWidget extends HudWidget {

    public MemoryUsageWidget(Movable movable) {
        super("Memory Usage", null, movable);
    }

    /** Render2D handler. */
    @com.adjustclient.core.events.EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!this.isActive()) {
            return;
        }
        this.drawFrame(event.scaledResolution, 65.0F, 15.0F);
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long usedMb = used / 1024L / 1024L;
        long maxMb = max / 1024L / 1024L;
        String usage = String.format("%d%s%d%s", usedMb, "/", maxMb, "MB");

        com.adjustclient.utilities.Vec2f position = this.movable.updatePosition();
        float x = position.x;
        float width = this.movable.width;
        float y = position.y;
        int textColor = new Color(210, 210, 210, 200).getRGB();
        Fonts.TAHOMA_11.drawString(usage, (x + width) - 3.0F - Fonts.TAHOMA_11.getStringWidth(usage), y + 14.0F, textColor);

        long percent = 100L * used / max;                               // ArithmeticException when max == 0
        String percentText = ChatColor.BOLD + String.format("% 2d%%", percent);
        Fonts.TAHOMA_12.drawString(percentText, x + 2.0F, y + 14.0F, textColor);

        // bar: grey track, HUD coloured fill
        float barLeft = x + 3.5F;
        float barTop = y + 21.0F;
        float barWidth = width - 7.0F;
        float barBottom = barTop + 1.0F;
        int trackColor = new Color(210, 210, 210, 150).getRGB();
        NvgDraw.fillRoundedRect(trackColor, barLeft, barTop, barLeft + barWidth, barBottom, 1.0F);
        float fillRight = ((float) (int) percent / 100.0F) * barWidth + barLeft;
        int fillColor = (HUDModule.getHudColor().getRGB() & 0xFFFFFF) | 0x96000000;
        NvgDraw.fillRoundedRect(fillColor, barLeft, barTop, fillRight, barBottom, 1.0F);
    }
}
