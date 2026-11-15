package com.adjustclient.game.render.progress;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.font.image.NvgDraw;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ProgressBarRenderer implements Listener {
    public List<ProgressBar> bars = new ArrayList<>();
    public Object field8;

    @EventHandler public void onRender2D(Render2DEvent event) {
        if (bars.isEmpty()) return;
        float left = event.scaledResolution.getScaledWidth() / 2.0F - 50.0F;
        float y = event.scaledResolution.getScaledHeight() / 1.9F;
        for (ProgressBar bar : bars) {
            NvgDraw.fillGradientRect(new Color(35, 35, 35).getRGB(), new Color(25, 25, 25).getRGB(), left, y, left + 100.0F, y + 3.0F);
            Color color = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            Color dark = com.adjustclient.render.ColorUtil.scale(color, 0.5F);
            NvgDraw.fillGradientRect(color.getRGB(), dark.getRGB(), left, y, left + Math.min(bar.progress, 1.0F) * 100.0F, y + 3.0F);
            NvgDraw.strokeRect(true, new Color(15, 15, 15).getRGB(), left, y, left + 100.0F, y + 3.0F, 0.5F);
            y += 5.0F;
        }
        bars.clear();
    }
}
