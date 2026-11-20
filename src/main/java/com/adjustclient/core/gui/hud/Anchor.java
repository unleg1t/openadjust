package com.adjustclient.core.gui.hud;

import com.adjustclient.core.gui.hud.layout.Rect2f;
import net.minecraft.client.gui.ScaledResolution;

public enum Anchor {
    TOP_LEFT((sr, out) -> {
        out.x = 0;
        out.y = 0;
        return 0.0F;
    }),
    TOP_RIGHT((sr, out) -> {
        float w = (float) sr.getScaledWidth_double();
        out.x = w;
        out.y = 0;
        return w;
    }),
    BOTTOM_LEFT((sr, out) -> {
        out.x = 0;
        float y = (float) (sr.getScaledHeight_double() - out.height);
        out.y = y;
        return y;
    }),
    BOTTOM_RIGHT((sr, out) -> {
        out.x = (float) sr.getScaledWidth_double();
        float y = (float) (sr.getScaledHeight_double() - out.height);
        out.y = y;
        return y;
    }),
    MIDDLE((sr, out) -> {
        out.x = (float) (sr.getScaledWidth_double() / 2.0);
        float y = (float) (sr.getScaledHeight_double() / 2.0);
        out.y = y;
        return y;
    });

    public final AnchorFunction function;

    Anchor(AnchorFunction function) {
        this.function = function;
    }

}
