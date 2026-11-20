package com.adjustclient.core.gui.clickgui.overlay;

import net.minecraft.client.gui.ScaledResolution;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.clickgui.Modifier;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.core.rendering.animation.TimedFade;
import com.adjustclient.core.gui.screen.AdjustComponent;
import com.adjustclient.utilities.Vec2f;

public class DimOverlay extends Modifier {
    private AdjustComponent bounds;
    private TimedFade fade;

    public DimOverlay(AdjustComponent bounds) {
        this.bounds = bounds;
        this.fade = new TimedFade(150L, 850L, 355L);
    }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object unused, Vec2f mouse, boolean firstPass) {
        float value = fade.b();
        if (value <= 0.0F) {
            return;
        }
        float w = (float) window.getScaledWidth();
        float h = (float) window.getScaledHeight();
        int argb = ((int) (value * 170.0F) << 24) | 0x191919; // Color(25, 25, 25, alpha)
        float top = bounds.y;
        NvgDraw.fillRect(argb, 0.0F, 0.0F, w, top);                                   // above the component
        NvgDraw.fillRect(argb, 0.0F, bounds.height + bounds.y, w, h);                 // below
        NvgDraw.fillRect(argb, 0.0F, top, bounds.x, bounds.height + top);             // left
        NvgDraw.fillRect(argb, bounds.width + bounds.x, top, w, bounds.height + top); // right
    }
}
