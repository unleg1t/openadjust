package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import java.awt.Color;
import net.minecraft.util.MathHelper;
import org.joml.Vector4f;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.gui.clickgui.CategoryTab;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.clickgui.ClickGuiTab;
import com.adjustclient.core.gui.clickgui.ConfigurationTab;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.util.ChatColor;

public final class TabHeader extends ClickGuiComponent {

    protected ClickGuiTab tab;
    public HoverEffect titleHover;
    public HoverEffect gradientHover;
    protected float titleReserve;
    public float titleShift;
    protected Object unused76;

    public TabHeader(ClickGuiTab tab) {
        super();
        this.tab = tab;
    }

    @Override
    public void renderContent(Render2DEvent queue, Vec2f mouse) {
        ClickGuiTab tab = this.tab;
        if (tab == null) {
            return;
        }
        float a = 0.08F;
        float b = 0.2F;
        float dim = 0.0F;
        if (tab.hovered) {
            float f = ((mouse.x - this.x) / this.width) * 0.5F;
            if (f < 0.0F) {
                a = 0.08F;
                b = 0.2F;
                dim = 0.0F;
            } else if (f > 1.0F) {
                a = 0.16F;
                b = 0.5F;
                dim = 0.3F;
            } else {
                dim = 0.3F * f;
                a = f * 0.08F + 0.08F;
                b = 0.2F + 0.3F * f;
            }
        }
        float v = this.gradientHover.k().value;
        int levelAlpha = MathHelper.clamp_int((int) ((v - dim) * 30.0F + 210.0F), 0, 255);
        Color accent = HUDModule.getHudColor();
        int rgb = accent.getRGB();
        Color c1 = new Color((rgb & 0xFFFFFF) | (levelAlpha << 24), true);       // accent with the computed alpha
        int start = ColorUtil.brighter(c1, 0.5F - a * v).getRGB();
        Color themed = ColorUtil.secondary(c1);
        float d = 1.0F - b * this.gradientHover.k().value;
        int tr = themed.getRGB();
        int end = new Color(clamp255((int) (((tr >> 16) & 0xFF) * d)), clamp255((int) (((tr >> 8) & 0xFF) * d)),
                clamp255((int) ((tr & 0xFF) * d)), tr >>> 24).getRGB();
        NvgContext.pushEvenScale();
        Vector4f p = NvgContext.toPixels(this.x, this.y, this.x + this.width, this.y + this.height);
        NvgDraw.fillGradientRect(start, end, p.x, p.y, p.z, p.w, p.x, p.y, p.z, p.y);   // horizontal gradient, left = start
        Float scale = NvgContext.scaleFactor;
        float line = (float) (scale == null ? 1 : (NvgContext.roundScaledPixels ? Math.round(scale) : scale.intValue())) * 0.5F;
        NvgDraw.strokeRect(false, 0x64232323, p.x, p.y, p.z, p.w, line);          // rgba(35,35,35,100)
        NvgContext.popEvenScale();
        String name = tab instanceof ConfigurationTab ? tab.title : ((CategoryTab) tab).category.getName();
        String text = ChatColor.BOLD + name;
        float cx = ((this.width - this.titleReserve) / 2.0F + this.x + 1.0F) - this.titleShift;
        float y = this.y + 4.5F;
        int color = this.titleHover.color(220);
        Fonts.TAHOMA_12.drawString(text, cx - Fonts.TAHOMA_12.getStringWidth(text) * 0.5F, y, color);
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        if (pressed && button == 0) {
            ClickGuiTab tab = this.tab;
            ClickGuiScreen screen = tab.screen;
            screen.draggedTab = tab;
            screen.focusedTab = tab;
            float ax = tab.anchor != null ? tab.anchor.x : tab.x;
            float ay = tab.anchor != null ? tab.anchor.y : tab.y;
            screen.grabX = mouse.x - ax;
            screen.grabY = mouse.y - ay;
            return true;
        }
        if (button == 1 && pressed) {
            this.toggleExpanded();
        }
        return true;
    }

    @Override
    public boolean isHovered() {
        return this.tab.hovered;
    }

    @Override
    public float getHeight() {
        return 11.0F;
    }

    @Override
    public boolean isHeader() {
        return true;
    }
}
