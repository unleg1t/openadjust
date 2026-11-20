package com.adjustclient.core.gui.hud.toggled;

import com.adjustclient.modules.Module;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.managers.util.ServiceUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;

public final class ToggledEntry implements Comparable<ToggledEntry> {
    public Animation xAnim;
    public Animation yAnim;
    public ToggledList owner;
    public Module module;
    public Object field20;
    public String text;
    public Animation heightAnim;
    public float width;
    public float targetX;
    public float targetY;
    public boolean active;
    public boolean removing;
    public Object field48;

    public void render(ScaledResolution sr, ToggledEntry previous, boolean last, Color primary, Color secondary) {
        this.xAnim.update(this.targetX);
        this.yAnim.update(this.targetY);
        if (ServiceUtil.ahmedElSisiMode) {
            this.heightAnim.update((System.currentTimeMillis() % 500L) < 250L ? 6.0F : 16.0F);
        }

        float y = this.yAnim.getValue();
        float x = (float) sr.getScaledWidth_double() + this.xAnim.getValue();   // right aligned: the animation runs from -width to 0
        ToggledSettings settings = this.owner.settings;

        if (NvgContext.currentScale != null && (NvgContext.currentScale % 2.0F) != 0.0F) {
            NvgContext.savedScale = NvgContext.currentScale;
            NvgContext.setScale(2.0F);
            NvgContext.scaleFactor = NvgContext.savedScale / 2.0F;
        }

        // geometry: left/top/right/bottom in (virtual) units
        float rowHeight = ServiceUtil.ahmedElSisiMode ? this.heightAnim.getValue() : settings.padding.getValue().rowHeight;
        float right = x + this.width;
        float bottom = y + rowHeight;
        float left = x;
        float top = y;

        float barWidth = 0.5F;
        float inset = 3.0F;
        float insetFull = 3.5F;
        float ratio = 1.0F;
        if (NvgContext.scaleFactor != null) {
            ratio = NvgContext.scaleFactor;
            float snapped = NvgContext.roundScaledPixels ? (float) Math.round(ratio) : (float) (int) ratio;
            float sx = ratio * left;
            float sy = ratio * top;
            float w = right - left;
            float h = bottom - top;
            float sRight = w > 1.0F ? right * ratio : sx + w * snapped;
            float sBottom = h > 1.0F ? bottom * ratio : sy + h * snapped;
            left = sx;
            top = sy;
            right = sRight;
            bottom = sBottom;
            barWidth = snapped * 0.5F;
            insetFull = ratio * 3.5F;
            inset = ratio * 3.0F;
        }

        ToggledBorder border = settings.border.getValue();
        if (settings.background.isEnabled()) {
            float bgLeft = border == ToggledBorder.RIGHT ? left - insetFull : left - inset;
            NvgDraw.fillRect(0x46131313, bgLeft, top, right, bottom);
        }

        // ---- text colour ----
        int color;
        ToggledColor mode = settings.color.getValue();
        if (mode == ToggledColor.DEFAULT && com.adjustclient.modules.impl.render.hud.HUDModule.colorSetting.getMode() == com.adjustclient.core.settings.ColorSetting.Mode.RAINBOW) {
            mode = ToggledColor.RAINBOW;
        }
        switch (ToggledSwitchMap.$SwitchMap$Color[mode.ordinal()]) {
            case 1: {
                // Default: pulsing blend of the two HUD colours: t = (cos(now * -0.003 + y * 0.075) + 1) / 2, clamped to [0, 1]
                double angle = (double) System.currentTimeMillis() * -0.003 + (double) (y * 0.075F);
                float t = ((float) Math.cos(angle) + 1.0F) * 0.5F;
                if (t > 1.0F) {
                    t = 1.0F;
                } else if (t < 0.0F) {
                    t = 0.0F;
                }
                double td = (double) t;
                double ud = 1.0 - td;
                int pa = primary.getRGB();
                int sa = secondary.getRGB();
                color = lerpArgb(pa, sa, td, ud);
                break;
            }
            case 2:
                // Rainbow
                color = Color.HSBtoRGB((float) ((System.currentTimeMillis() % 5000) + (5000.0F - y * 30.0F)) / 5000.0F, 0.45F, 0.8F) | 0xFF000000;
                break;
            case 3: {
                // Astolfo
                long t = System.currentTimeMillis();
                int offset = (int) (y * 50.0F);
                color = Color.HSBtoRGB((float) (Math.abs(((float) ((t - offset) % 8000L) / 4000.0F) - 1.0F) * 0.5F + 0.5F), 0.45F, 0.8F) | 0xFF000000;
                break;
            }
            case 4:
                // Random: stable per module
                color = this.module.hashCode() | 0xFF000000;
                break;
            default:
                throw new IllegalStateException();
        }

        // ---- border ----
        float textX = x - 1.5F;
        switch (ToggledSwitchMap.$SwitchMap$Border[border.ordinal()]) {
            case 1: {
                // Full: bar on the left of the row + connection to the previous row + closing line under the last row
                NvgDraw.fillRect(color, (left - inset) - barWidth, top, left - inset, bottom);
                if (!this.removing || last) {
                    if (previous != null) {
                        // connection piece between this row and the previous one (only if their x positions differ)
                        float screenWidth = (float) sr.getScaledWidth_double();
                        float prevX = screenWidth + previous.xAnim.getValue();
                        float connectY = rowHeight + previous.yAnim.getValue();     // bottom of the previous row
                        float base;
                        if (this.xAnim.isFinished()) {
                            base = (prevX - (screenWidth + this.targetX)) + x;
                            if (!previous.xAnim.isFinished() && !previous.removing) {
                                connectY = y;                                        // previous row still sliding: connect at this row's top
                            }
                        } else {
                            base = prevX;
                        }
                        if (prevX != x) {
                            float cLeft = (base - 3.0F) * ratio - barWidth;
                            float cTop = connectY * ratio;
                            float cRight = cLeft + (x - prevX) * ratio;
                            NvgDraw.fillRect(color, cLeft, cTop, cRight, cTop + barWidth);
                        }
                    }
                }
                if (last) {
                    NvgDraw.fillRect(color, (left - inset) - barWidth, bottom, ratio * this.width + left, bottom + barWidth);
                }
                break;
            }
            case 2:
                // Right: bar at the right edge of the row; the text moves 0.5 left
                NvgDraw.fillRect(color, right - barWidth, top, right, bottom);
                textX = textX - 0.5F;
                break;
            default:
                break;
        }

        // restore the NVG scale if it was replaced above
        if (NvgContext.savedScale != null) {
            NvgContext.setScale(NvgContext.savedScale);
            NvgContext.scaleFactor = null;
            NvgContext.savedScale = null;
        }

        Fonts.TAHOMA_12.drawString(this.text, textX, (rowHeight * 0.5F + y) - 1.5F, color);
    }

    private static int lerpArgb(int a, int b, double ta, double tb) {
        int red = (int) ((a >> 16 & 0xFF) * ta + (b >> 16 & 0xFF) * tb);
        int green = (int) ((a >> 8 & 0xFF) * ta + (b >> 8 & 0xFF) * tb);
        int blue = (int) ((a & 0xFF) * ta + (b & 0xFF) * tb);
        int alpha = (int) ((a >>> 24) * ta + (b >>> 24) * tb);
        return new Color(red, green, blue, alpha).getRGB();   // java.awt.Color.testColorValueRange
    }

    /** Widest first. */
    @Override
    public int compareTo(ToggledEntry other) {
        float a = this.width;
        float b = other.width;
        if (ServiceUtil.ahmedElSisiMode) {
            b += (float) (Math.abs(other.text.hashCode()) % 15);
            a += (float) (Math.abs(this.text.hashCode()) % 15);
        }
        return Float.compare(b, a);
    }
}
