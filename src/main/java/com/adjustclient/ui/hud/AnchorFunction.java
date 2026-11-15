package com.adjustclient.ui.hud;

import com.adjustclient.ui.hud.layout.Rect2f;
import net.minecraft.client.gui.ScaledResolution;

public interface AnchorFunction {
    float get(ScaledResolution sr, Rect2f out);
}
