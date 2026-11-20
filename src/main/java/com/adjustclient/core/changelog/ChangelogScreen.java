package com.adjustclient.core.changelog;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.gui.dialog.AdjustButton;
import com.adjustclient.core.gui.dialog.AlphaModifier;
import com.adjustclient.core.gui.mainmenu.AdjustMainMenu;
import com.adjustclient.core.gui.mainmenu.VoronoiBackground;
import com.adjustclient.utilities.ScreenUtil;
import com.adjustclient.utilities.Timer;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import net.minecraft.util.ChatColor;

public class ChangelogScreen extends GuiScreen implements AlphaModifier {
    public boolean closing;
    public Animation fadeAnimation;
    public ChangelogLink hoveredVersion;
    public Animation scrollAnimation;
    public ChangelogHighlight hoveredHighlight;
    public float scroll;
    public float minimumScroll;
    public Object field88;

    public ChangelogScreen() {
        this.mc = Minecraft.getMinecraft();
        fadeAnimation = new Animation(Easing.LINEAR, 65L);
        scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 230L);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) closing = true;
    }

    public float getLineWidth(List<ChangelogHighlight> highlights) {
        float width = 0.0F;
        for (ChangelogHighlight highlight : highlights) width += Fonts.TAHOMA_10.getStringWidth(highlight.text);
        return width;
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (key == org.lwjgl.input.Keyboard.KEY_ESCAPE) {
            closing = true;
            return;
        }
        if (key == 1) {
            mc.displayGuiScreen(null);
            if (mc.currentScreen == null) mc.setIngameFocus();
        }
    }

    public float renderVersions(int mouseX, int mouseY) {
        float animatedScroll = scrollAnimation.value;
        float startY = animatedScroll + 10.0F;
        boolean overlayOpen = ScreenUtil.isAdjustScreenOpen();
        ChangelogLink previousHover = hoveredVersion;
        hoveredVersion = null;
        hoveredHighlight = null;
        float y = startY;
        boolean stop = false;
        for (int i = Changelog.versions.size() - 1; i >= 0 && !stop; i--) {
            ChangelogLink version = Changelog.versions.get(i);
            if (version.announcement) y += 5.0F;
            float versionTop = y;
            int alpha = Changelog.unreadVersions.contains(version) ? 225 : 150;
            float selected = version.animate(previousHover == version);
            alpha = Math.min(255, alpha + (int) (selected * 65.0F) + (version.announcement ? 50 : 0));
            int grey = version.announcement ? 225 : 212;
            Fonts.TAHOMA_12.drawCenteredString(version.title, width / 2.0F, y,
                    new Color(grey, grey, grey, alpha).getRGB());
            int categoryAlpha = Math.max(0, Math.min(255, (int) (alpha + selected * 30.0F)));
            y += 9.0F;
            Class<?> previousType = null;
            for (ChangelogChange change : version.version.changes) {
                if (minimumScroll != 0.0F && y > height - 42.0F) {
                    stop = true;
                    break;
                }
                float x;
                if (!version.announcement && previousType != change.getClass()) {
                    if (previousType != null) y += 2.0F;
                    y += 0.5F;
                    String category;
                    if (change instanceof RemovedChange) category = "removed";
                    else if (change instanceof AddedChange) category = "added";
                    else category = "improved";
                    Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
                    Color color = ColorUtil.withAlpha(ColorUtil.blend(accent, new Color(199, 199, 199), 0.6F), categoryAlpha);
                    Fonts.TAHOMA_10.drawCenteredString(String.valueOf(ChatColor.ITALIC) + category,
                            width / 2.0F, y, color.getRGB());
                    y += 7.0F;
                }
                previousType = change.getClass();
                x = width / 2.0F - getLineWidth(change.highlights) / 2.0F;
                float proximity = Math.max(0.0F, Math.min(1.0F, 1.0F - (y - mouseY) * (y - mouseY) / 250.0F));
                float lineHover = 0.0F;
                if (change.animation != null) lineHover = change.animation.update(proximity);
                else if (proximity != 0.0F) {
                    change.animation = new Animation(Easing.LINEAR, 100L);
                    lineHover = change.animation.update(proximity);
                }
                int lineAlpha = alpha + (int) (lineHover * 70.0F);
                for (ChangelogHighlight highlight : change.highlights) {
                    float spanWidth = Fonts.TAHOMA_10.getStringWidth(highlight.text);
                    if (!overlayOpen && hoveredHighlight == null && mouseX >= x && y <= mouseY
                            && x + spanWidth >= mouseX && y + 5.0F + 0.5F >= mouseY) {
                        hoveredHighlight = highlight;
                    }
                    float hover = highlight.animation == null ? 0.0F
                            : highlight.animation.update(hoveredHighlight == highlight ? 1.0F : 0.0F);
                    float blend = 1.0F - hover * 0.4F;
                    Color original = ColorUtil.withAlpha(new Color(highlight.color.get(), true),
                            (int) (hover * 40.0F + lineAlpha));
                    Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
                    Color color = ColorUtil.blend(original, accent, blend);
                    Fonts.TAHOMA_10.drawString(highlight.text, x, y, color.getRGB());
                    x += spanWidth;
                }
                y += 7.0F;
            }
            if (!overlayOpen && mouseY >= versionTop && y - animatedScroll + 2.0F >= mouseY
                    && !version.announcement) hoveredVersion = version;
            y += 13.0F;
            if (version.announcement) y += 5.0F;
        }
        minimumScroll = startY - y + height / 1.5F;
        if (hoveredHighlight != null && hoveredHighlight.action != null) {
            for (GuiButton button : buttonList) {
                if (button.isMouseOver()) {
                    hoveredHighlight = null;
                    return y;
                }
            }
            RenderUtil.cursor = 221188;
        }
        return y;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) return;
        if (hoveredHighlight != null && hoveredHighlight.action != null
                && (hoveredHighlight.clickTimer == null || hoveredHighlight.clickTimer.hasElapsed(500L))) {
            ChangelogHighlight highlight = hoveredHighlight;
            highlight.action.run();
            if (highlight.clickTimer == null) highlight.clickTimer = new Timer();
            highlight.clickTimer.reset();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float previousAlpha = NvgDraw.getGlobalAlpha();
        NvgDraw.setGlobalAlpha(fadeAnimation.value);
        scrollAnimation.update(scroll);
        fadeAnimation.update(closing ? 0.0F : 1.0F);
        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        VoronoiBackground.draw(this, mouseX, mouseY, accent);
        scroll = Math.min(0.0F, scroll);
        if (minimumScroll != 0.0F) scroll = Math.max(minimumScroll, scroll);
        RenderUtil.pushScissor(new ScaledResolution(mc), 1, 0.0F, 0.0F, width, height - 42.0F);
        if (Adjust.INSTANCE.screenStack.getTopOverlay() == null) renderVersions(mouseX, mouseY);
        else renderVersions(-666666, -666666);
        RenderUtil.popScissor();
        drawCredits();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (closing && fadeAnimation.value == 0.0F) {
            Changelog.updateUnread(Changelog.latest);
            mc.displayGuiScreen(new AdjustMainMenu());
            VoronoiBackground.opening = true;
        }
        NvgDraw.setGlobalAlpha(previousAlpha);
    }

    public void drawRightAligned(String text, int alpha, float y, float rightMargin) {
        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        Color color = ColorUtil.blend(accent, new Color(177, 177, 177, alpha), 0.8F);
        Fonts.TAHOMA_10.drawString(text, width - Fonts.TAHOMA_10.getStringWidth(text) - rightMargin,
                y, color.getRGB());
    }

    @Override
    public void initGui() {
        buttonList.clear();
        minimumScroll = 0.0F;
        buttonList.add(new AdjustButton(0, (int) (width / 2.0F - 30.0F), height - 35.0F,
                60, 11, "Acknowledge", this));
        VoronoiBackground.resize(width, height);
    }

    public void drawCredits() {
        String current = String.valueOf(ChatColor.GRAY) + "You are running the latest version of "
                + ChatColor.RESET + "Adjust" + ChatColor.GRAY + " from " + Changelog.latest.title;
        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        Color color = ColorUtil.withAlpha(ColorUtil.blend(accent, new Color(177, 177, 177), 0.8F), 255);
        Fonts.TAHOMA_12.drawCenteredString(current, width / 2.0F, height - 13.5F, color.getRGB());
        drawRightAligned(String.valueOf(ChatColor.RESET) + "Adjust Client" + ChatColor.GRAY
                + " is developed by " + ChatColor.WHITE + "final" + ChatColor.GRAY + ", "
                + ChatColor.WHITE + "calculus" + ChatColor.GRAY + " and " + ChatColor.WHITE + "vincent",
                255, 4.0F, 2.0F);
        String suffix = String.valueOf(ChatColor.GRAY) + ChatColor.ITALIC;
        String auth = String.valueOf(ChatColor.WHITE) + ChatColor.ITALIC + "auth" + suffix;
        String purity = String.valueOf(ChatColor.WHITE) + ChatColor.ITALIC + "purity" + suffix;
        drawRightAligned(suffix + "(previously by " + auth + " and " + purity + " aswell)",
                55, 9.5F, 0.5F);
        if ("wykt".equals(Adjust.username)) {
            drawRightAligned(String.valueOf(ChatColor.GRAY) + "We hope you" + ChatColor.RED
                    + ChatColor.BOLD + " stop molesting children, " + Adjust.username, 255, 15.0F, 2.0F);
        } else {
            drawRightAligned(String.valueOf(ChatColor.GRAY) + "We hope you are having fun", 255, 15.0F, 2.0F);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) scroll += wheel > 0 ? 42.0F : -42.0F;
    }

    @Override
    public int multiplyAlpha(int alpha) {
        return alpha;
    }

    @Override
    public int getMaxFps() {
        return AdjustMainMenu.windowActive ? Math.min(mc.gameSettings.limitFramerate, 175) : 60;
    }
}
