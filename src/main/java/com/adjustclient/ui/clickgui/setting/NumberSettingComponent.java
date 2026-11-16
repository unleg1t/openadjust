package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.NvgFontRenderer;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.NumberSetting;
import java.awt.Color;
import java.text.NumberFormat;
import net.minecraft.client.gui.GuiScreen;
import org.joml.Vector4f;
import org.lwjgl.input.Mouse;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.ui.clickgui.frame.HoverEffect;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.ui.screen.AdjustComponent;
import net.minecraft.util.ChatColor;

public final class NumberSettingComponent extends SettingComponent<NumberSetting> {
    public static double dragScale;
    public static double dragSensitivity;

    boolean dragging;
    HoverEffect labelColor;
    Animation fillAnimation;
    Object obf72;

    static final java.util.function.BooleanSupplier ALWAYS_ACTIVE = () -> true;

    public NumberSettingComponent(NumberSetting setting) {
        super(setting);
        this.labelColor = new HoverEffect(this, ALWAYS_ACTIVE, HoverEffect.hoveredOf(this), 0.27F, 0.75F);
        this.modifiers.add(this.labelColor);
    }

    void setFromText(String text) {
        double parsed;
        try {
            parsed = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            NotificationBuilder notification = new NotificationBuilder("Invalid Value");
            notification = notification.addText("Value ", 0xFFBEBCBD);
            notification = notification.append(text);
            notification = notification.addText(" is not a valid number.", 0xFFBEBCBD);
            notification.show(); // show
            return;
        }
        this.setting.setValue(Double.valueOf(parsed));
    }

    public boolean onScroll(com.adjustclient.util.Vec2f mouse, float delta) {
        AdjustComponent r = this.bounds;
        if (mouse.x < r.x || mouse.y < r.y || r.x + r.width <= mouse.x || r.y + r.height <= mouse.y) {
            return false;
        }
        com.adjustclient.ui.clickgui.ClickGuiScreen frameState = this.owner.screen;
        if (frameState.timer48.lastMs + 700L > System.currentTimeMillis()) {
            return false;
        }
        if (delta > 0.0F) {
            this.setting.decrement();
        } else if (delta < 0.0F) {
            this.setting.increment();
        }
        return true;
    }

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        AdjustComponent bounds = this.bounds;
        float trackLeft = bounds.x + 0.0F;
        float trackWidth = bounds.width - 2.0F; // f2
        NumberSetting setting = this.setting;
        if (this.dragging) {
            float mouseX = mouse.x;
            if (Mouse.isButtonDown(0) && dragScale == (double) HUDModule.getClickGuiScale()) {
                float t = (mouseX - trackLeft) / trackWidth;
                t = t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
                float min = setting.getMin().floatValue();
                float max = setting.getMax().floatValue();
                double v = (double) Math.round((double) ((max - min) * t + min) * 20.0) / 20.0; // quantised to 0.05
                setting.setValue(Double.valueOf(v));
            } else {
                this.dragging = false;
            }
        }
        float value = ((Number) setting.getValue()).floatValue();
        float min = setting.getMin().floatValue();
        float max = setting.getMax().floatValue();
        float fraction = (value - min) / (max - min);
        float fillTarget = fraction * trackWidth;
        if (this.fillAnimation == null) {
            this.fillAnimation = new Animation(Easing.EASE_OUT_SINE, 80L);
            this.fillAnimation.value = fillTarget;
            this.fillAnimation.restart(); // start = now, previous = value, finished = false
        }
        float fill = this.fillAnimation.update(fillTarget);
        float barY = this.y + 8.5F;   // bar top (y + 8.5)
        float barLeft = trackLeft + 2.0F;  // bar x1
        float barRight = trackWidth + trackLeft; // bar x2 (= x + width - 2.0)
        NvgContext.pushEvenScale();
        Vector4f trackPx = NvgContext.toPixels(barLeft, barY, barRight, barY + 1.0F); // pixel snapped track rectangle
        if (fill > 2.0F) {
            Color accent = new Color(this.labelColor.accent(255) | 0xFF000000, true);
            Color second;
            if (!HUDModule.isSecondaryActive()) {
                second = com.adjustclient.render.ColorUtil.scale(accent, 0.58F);
            } else {
                second = com.adjustclient.render.ColorUtil.secondary(accent);
                second = com.adjustclient.render.ColorUtil.blend(second, accent, fraction);
            }
            float end = Math.min(trackWidth, Math.max(fill, 2.0F));
            // upper half of the bar: y .. y + 0.5 ; lower half: one pixel unit below the upper one (the lower rectangle is placed from the upper one)
            Vector4f upper = NvgContext.toPixels(barLeft, barY, end + trackLeft, barY + 0.5F);
            int a1 = new Color(scaled(accent.getRed(), 0.7F), scaled(accent.getGreen(), 0.7F), scaled(accent.getBlue(), 0.7F), accent.getAlpha()).getRGB();
            int a2 = new Color(scaled(second.getRed(), 0.87F), scaled(second.getGreen(), 0.87F), scaled(second.getBlue(), 0.87F), second.getAlpha()).getRGB();
            NvgDraw.fillGradientRect(a1, a2, upper.x, upper.y, upper.z, upper.w, upper.x, upper.y, upper.z, upper.y);   // horizontal
            float p = lineWidth();
            float ly1 = upper.y + p;
            float ly2 = upper.y + p + p;
            int b1 = new Color(scaled(accent.getRed(), 0.585F), scaled(accent.getGreen(), 0.585F), scaled(accent.getBlue(), 0.585F), accent.getAlpha()).getRGB();
            int b2 = new Color(scaled(second.getRed(), 0.598F), scaled(second.getGreen(), 0.598F), scaled(second.getBlue(), 0.598F), second.getAlpha()).getRGB();
            NvgDraw.fillGradientRect(b1, b2, upper.x, ly1, upper.z, ly2, upper.x, ly1, upper.z, ly1);   // horizontal
        }
        // track tint and outline (rectangle = trackPx)
        NvgDraw.fillRect(this.labelColor.accent(90), trackPx.x, trackPx.y, trackPx.z, trackPx.w); // accent with alpha 90 * animation value
        NvgDraw.strokeRect(true, this.labelColor.color(5), trackPx.x, trackPx.y, trackPx.z, trackPx.w, lineWidth()); // grey 5 + 110 v, alpha 15 + 48 v
        NvgContext.popEvenScale();
        // value text
        String valueText;
        NumberSetting.Formatter formatter = setting.getFormatter();
        if (formatter != null) {
            valueText = formatter.transform(setting);
        } else {
            Number n = (Number) setting.getValue();
            valueText = NumberSetting.FORMAT /* shared DecimalFormat */.format(n.doubleValue());
        }
        valueText = ChatColor.BOLD /* ChatColor control string */ + valueText;
        NvgFontRenderer font = Fonts.TAHOMA_10;
        float valueWidth = font.getStringWidth(valueText);
        String label = this.getLabel();
        float labelWidth = font.getStringWidth(label);
        float valueX = (trackLeft + bounds.width) - 2.5F - valueWidth;
        float labelX = Math.min(2.0F + bounds.x, (valueX - labelWidth) - 1.0F);
        float textY = this.y + 4.0F;
        font.drawString(label, labelX, textY, this.labelColor.color(190));
        font.drawString(valueText, valueX, textY, this.labelColor.color(190));
    }

    private static int scaled(int component, float factor) {
        return Math.max(0, (int) ((float) component * factor));
    }

    /** The {@code pixelUnit * 0.5} line width that every outline of the Click GUI passes (pixelUnit: whole device pixels of the pushed even scale). */
    private static float lineWidth() {
        Float factor = NvgContext.scaleFactor;
        int unit = factor == null ? 1 : (NvgContext.roundScaledPixels ? Math.round(factor) : factor.intValue());
        return (float) unit * 0.5F;
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (button != 0) {
            if (button != 2 || this.dragging) {
                return false;
            }
            this.openValuePrompt(this::setFromText);
            return true;
        }
        if (pressed && GuiScreen.isCtrlKeyDown()) {
            this.dragging = false;
            this.openResetPrompt();
            return true;
        }
        this.dragging = pressed;
        int scale = UserConfiguration.secondaryScale.getInt();
        dragScale = scale == 16 ? 1000.0 : (double) (scale - 1) * 0.25 + 2.0;
        return true;
    }

    @Override
    public float getHeight() {
        return 11.0F;
    }
}
