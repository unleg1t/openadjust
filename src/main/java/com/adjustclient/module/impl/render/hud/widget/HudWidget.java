package com.adjustclient.module.impl.render.hud.widget;

import com.adjustclient.event.Listener;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.ui.hud.layout.Point2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector4f;

import java.awt.Color;
import com.adjustclient.game.movable.Movable;
import com.adjustclient.game.movable.MovableManager;

public abstract class HudWidget implements Listener {

    protected String name;
    protected String description;
    protected Movable movable;
    protected BooleanSetting enabled;

    protected HudWidget(String name, String description, Movable movable) {
        this.name = name;
        this.description = description;
        this.movable = movable;
    }

    /**
     * Creates the widget's "Enabled" setting in {@code parent} (the HUD > Widgets group; called once for every widget by the
     * lambda of {@link HudWidgets#createSettings}). The boolean setting is labelled with the widget name (so the labels
     * are "On"/"Off", only a setting named "Enabled" would get "Yes"/"No"), defaults to false and takes the description.
     * Also installs the visibility supplier of the movable.
     */
    public void createSettings(ExpandSetting parent) {
        this.enabled = new BooleanSetting(parent, this.name, false, "On", "Off");
        if (this.description != null) {
            this.enabled.description = this.description;
        }
        this.movable.canDrag = (java.util.function.BooleanSupplier) () -> this.enabled.isEnabled() && Minecraft.getMinecraft().thePlayer != null;
    }

    /** The widget is switched on (and not hidden by its dependency) and the HUD module itself is enabled. */
    public boolean isActive() {
        if (!this.enabled.isEnabled()) {
            return false;
        }
        return HUDModule.INSTANCE_ALT.isEnabled();
    }

    /**
     * Lays the widget out and draws its frame: stores the size in the movable ({@code width x (height + 9)}, header height 9),
     * gets the on-screen position, lets the movable paint its HUD-editor highlight, draws the panel and the centred title.
     * Called at the start of every widget's render handler.
     *
     * @param sr     the ScaledResolution of the Render2D event (a2)
     * @param width  f1: content width
     * @param height f2: content height (the header strip adds 9)
     */
    protected void drawFrame(ScaledResolution sr, float width, float height) {
        this.movable.width = width;
        this.movable.height = height + 9.0F;
        this.movable.headerHeight = 9.0F;
        com.adjustclient.util.Vec2f position = this.movable.updatePosition();
        float x = position.x;
        float y = position.y;
        this.movable.drawOverlay(sr);
        this.drawPanel(140, x, y, width, height);
        float centerX = x + width * 0.5F;
        float titleY = y + 4.0F;
        int titleColor = new Color(240, 240, 240, 222).getRGB();
        Fonts.TAHOMA_10.drawString(this.name, centerX - Fonts.TAHOMA_10.getStringWidth(this.name) * 0.5F, titleY, titleColor);
    }

    public static void drawPanel(int alpha, float x, float y, float width, float height) {
        NvgContext.pushEvenScale();
        float headerBottom = y + 9.0F;
        float bottom = headerBottom + height + 1.0F;
        int bodyColor = (alpha << 24) | 0x131313;

        Vector4f body = NvgContext.toPixels(x, y, x + width, bottom);
        NvgDraw.fillRect(bodyColor, body.x, body.y, body.z, body.w);
        // two more fills with the colour that is still set on the NanoVG state (the body colour): a 0.5 px separator line at the
        // bottom of the header (the rectangle (x, y + 9) - (x + w, y + 8.5) has a negative height) and the header strip, which darkens
        // the header because the colour is translucent
        Vector4f separator = NvgContext.toPixels(x, headerBottom, x + width, headerBottom - 0.5F);
        NvgDraw.fillRect(separator.x, separator.y, separator.z, separator.w);
        Vector4f header = NvgContext.toPixels(x, y, x + width, headerBottom);
        NvgDraw.fillRect(header.x, header.y, header.z, header.w);
        // outline
        Vector4f outline = NvgContext.toPixels(x, y, x + width, bottom);
        // outline line width: 0.5 without a scale factor, else (int or rounded scale factor) * 0.5
        float lineWidth = NvgContext.scaleFactor == null ? 0.5F
                : (float) (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor) : (int) (float) NvgContext.scaleFactor) * 0.5F;
        NvgDraw.strokeRect(false, (alpha << 24) | 0x1B1B1B, outline.x, outline.y, outline.z, outline.w, lineWidth);
        NvgContext.popEvenScale();
    }
}
