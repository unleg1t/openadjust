package com.adjustclient.ui.dialog;

import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.ui.hud.layout.Point2f;
import com.adjustclient.ui.hud.layout.Rect2f;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector4f;

public final class DropdownElement {
    public String label;
    public List<DropdownOption> options;
    public Animation hover;
    public Animation expansion;
    public Animation arrow;
    public Rect2f bounds;
    public DropdownOption selected;
    public BiConsumer<Integer, Integer> selectionListener;
    public boolean expanded;
    public Object field40;

    public DropdownElement(String label, String[] options) {
        hover = new Animation(Easing.LINEAR, 100L);
        expansion = new Animation(Easing.EASE_OUT_SINE, 125L);
        arrow = new Animation(Easing.EASE_OUT_EXPO, 125L);
        this.label = label;
        this.options = Arrays.stream(options).map(text -> new DropdownOption(text))
                .collect(Collectors.toList());
    }
    public boolean mouseClicked(Vec2f mouse) {
        if (bounds != null && mouse.x >= bounds.x && mouse.y >= bounds.y
                && mouse.x < bounds.x + bounds.width && mouse.y < bounds.y + bounds.height) {
            expanded = !expanded;
            return true;
        }
        if (expanded) {
            for (DropdownOption option : options) {
                if (mouse.x >= option.x && mouse.y >= option.y && mouse.x < option.x + option.width
                        && mouse.y < option.y + option.height) {
                    DropdownOption previous = selected;
                    selected = previous == option ? null : option;
                    if (selectionListener != null) {
                        selectionListener.accept(previous == null ? -1 : options.indexOf(previous),
                                selected == null ? -1 : options.indexOf(selected));
                    }
                    expanded = false;
                    return true;
                }
            }
        }
        expanded = false;
        return false;
    }
    public boolean isHovered(Vec2f mouse, boolean includeOptions) {
        if (bounds == null) return false;
        float extra = includeOptions ? options.size() * 10.0F : 0.0F;
        return mouse.x >= bounds.x && mouse.y >= bounds.y && mouse.x < bounds.x + bounds.width
                && mouse.y < bounds.y + bounds.height + extra;
    }
    public void draw(ScaledResolution window, Vec2f mouse, float x, float y, float width) {
        bounds = new Rect2f();
        bounds.x = x; bounds.y = y; bounds.width = width; bounds.height = 9.0F;
        hover.update(isHovered(mouse, expanded) ? 1.0F : 0.0F);
        expansion.update(expanded ? 1.0F : 0.0F);
        arrow.update(expanded ? 1.0F : 0.0F);
        int highlight = (int) (hover.value * 25.0F);
        int fillGrey = 52 - (int) (expansion.value * 20.0F);
        int outlineGrey = 42 - (int) (expansion.value * 10.0F);
        int expandedAlpha = (int) (expansion.value * 105.0F);
        float height = 9.0F + options.size() * 10.0F * expansion.value;
        NvgContext.pushEvenScale();
        Vector4f pixels = NvgContext.toPixels(x, y, x + width, y + height);
        NvgDraw.fillRect(new Color(fillGrey, fillGrey, fillGrey, Math.min(255, expandedAlpha + highlight + 82)).getRGB(),
                pixels.x, pixels.y, pixels.z, pixels.w);
        float scale = NvgContext.scaleFactor == null ? 1.0F
                : NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor) : NvgContext.scaleFactor.intValue();
        NvgDraw.strokeRect(true, new Color(outlineGrey, outlineGrey, outlineGrey, Math.min(255, expandedAlpha + highlight + 145)).getRGB(),
                pixels.x, pixels.y, pixels.z, pixels.w, scale * 0.5F);
        NvgContext.popEvenScale();
        int textAlpha = highlight * 2 + 190;
        if (expansion.value != 0.0F) {
            RenderUtil.pushScissor(window, 1, x, y, width, height);
            float rowY = y + 9.0F;
            for (DropdownOption option : options) {
                option.x = x; option.y = rowY; option.width = width;
                boolean inside = mouse.x >= x && mouse.x < x + width && mouse.y >= rowY && mouse.y < rowY + option.height;
                option.hover.update(inside ? 1.0F : 0.0F);
                int grey = (int) (option.hover.value * 36.0F) + 150;
                int alpha = (int) (option.hover.value * 55.0F) + 170;
                String text = (option == selected || inside ? net.minecraft.util.ChatColor.BOLD.toString() : "") + option.text;
                int color = option == selected ? HUDModule.getColor(alpha).getRGB()
                        : new Color(grey, grey, grey, alpha).getRGB();
                Fonts.TAHOMA_12.drawString(text, x + 2.0F, rowY + 4.0F, color);
                rowY += option.height;
            }
            RenderUtil.popScissor();
        }
        String selectedText = selected == null ? label : net.minecraft.util.ChatColor.BOLD.toString() + selected.text;
        int color = selected == null ? new Color(185, 185, 185, textAlpha).getRGB()
                : HUDModule.getColor(textAlpha).getRGB();
        Fonts.TAHOMA_12.drawString(selectedText, x + 2.0F, y + 3.0F, color);
        int arrowAlpha = highlight - (int) (expansion.value * 50.0F) + 190;
        float baseY = y + 3.0F + arrow.value * 3.0F;
        float tipY = y + 3.0F + (1.0F - arrow.value) * 3.0F;
        NvgDraw.fillTriangle(new Color(210, 210, 210, arrowAlpha).getRGB(),
                x + width - 5.0F, baseY, x + width - 2.0F, baseY, x + width - 3.5F, tipY);
    }
}
