package com.adjustclient.core.gui.clickgui.setting.color;

import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import java.awt.Color;
import net.minecraft.util.MathHelper;
import org.joml.Vector4f;

public abstract class ColorPickerSlider extends ColorPickerElement {

    protected void drawKnob(float progress) {
        float x = this.x;      // rectangle x
        float y = this.y;      // rectangle y
        float width = this.width; // rectangle width
        float height = this.height; // rectangle height
        // knob centre = x + progress * width, 2.5 wide -> left edge 1.25 before the centre, clamped into [x, x + width - 2.5]
        float left = MathHelper.clamp_float(progress * width + x - 1.25F, x, x + width - 2.5F);
        float knobWidth = 2.5F;
        float top = y;
        float right = left + knobWidth;
        float bottom = y + height;
        NvgContext.pushEvenScale();
        Vector4f px = NvgContext.toPixels(left, top, right, bottom);
        float x1 = px.x + 0.5F;
        float y1 = px.y + 0.5F;
        float x2 = px.z - 0.5F;
        float y2 = px.w - 0.5F;
        NvgDraw.strokeRect(true, new Color(9, 9, 9, 200).getRGB(), x1, y1, x2, y2, 0.5F);
        NvgDraw.strokeRect(false, new Color(228, 228, 228, 200).getRGB(), x1, y1, x2, y2, 0.5F);
        NvgContext.popEvenScale();
    }
}
