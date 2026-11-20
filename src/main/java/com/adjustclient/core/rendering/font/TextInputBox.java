package com.adjustclient.core.rendering.font;

import com.adjustclient.core.rendering.NvgRenderer;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.hud.layout.Point2f;
import com.adjustclient.core.gui.hud.layout.Rect2f;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector4f;
import org.lwjgl.input.Keyboard;

public final class TextInputBox extends Rect2f {
    public TextInput input;
    public TextInputBox(TextInput input) { this.input = input; }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE && input.focused) {
            input.focused = false;
            return true;
        }
        if (!input.focused) return false;
        input.field.setFocused(true);
        return input.field.textboxKeyTyped(typedChar, keyCode);
    }

    public void draw(ScaledResolution window, Point2f mouse, float x, float y) {
        float height = 9.0F + Math.max(0, input.lines.length - 1) * 7.5F;
        NvgContext.pushEvenScale();
        Vector4f rect = NvgContext.toPixels(x, y, x + width, y + height);
        NvgDraw.fillRect(0x5A2D2D2D, rect.x, rect.y, rect.z, rect.w);
        NvgDraw.strokeRect(true, 0x822D2D2D, rect.x, rect.y, rect.z, rect.w, (NvgContext.scaleFactor == null ? 1.0F : (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor) : NvgContext.scaleFactor.intValue())) * 0.5F);
        NvgContext.popEvenScale();
        input.hovered = mouse.x >= x && mouse.y >= y && x + width > mouse.x && y + height > mouse.y;
        NvgRenderer renderer = new NvgRenderer();
        input.draw(renderer, x + 2.0F, y + 3.5F);
        if (input.hovered) RenderUtil.cursor = 221186;
        this.x = x;
        this.y = y;
        this.height = height;
        renderer.flush(window);
    }
}
