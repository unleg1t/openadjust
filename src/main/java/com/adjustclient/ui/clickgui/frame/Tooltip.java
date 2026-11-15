package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.image.NvgDraw;
import java.awt.Color;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.ui.clickgui.ModifierPhase;
import com.adjustclient.ui.screen.AdjustScreen;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;

public class Tooltip extends ElementDecorator {

    protected ClickGuiComponent owner;
    protected String text;
    protected String extended;
    protected BooleanSupplier hovered;
    protected Animation fade;
    public Timer hoverTimer;
    protected Tooltip previous;
    public long delay = 250L;
    protected float bottom;
    protected Object configurationData;

    public Tooltip(ClickGuiComponent owner, String text, BooleanSupplier hovered) {
        this.owner = owner;
        this.text = text;
        this.hovered = hovered;
        this.delay = 300L;
    }

    public Tooltip(ClickGuiComponent owner, String text, BooleanSupplier hovered, long delay) {
        this(owner, text, hovered);
        this.delay = delay;
    }

    public Tooltip(ClickGuiComponent owner, long delay, String text, String extended) {
        this.owner = owner;
        this.delay = delay;
        this.text = text;
        this.extended = extended;
        this.hovered = () -> owner.isHovered();
    }

    @Override
    public ModifierPhase getPhase() {
        return ModifierPhase.POST;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void reset() {
        this.hoverTimer = null;
        this.fade = null;
        this.previous = null;
    }

    @Override
    public void render(ScaledResolution window, Render2DEvent queue, Object drawList, Vec2f mouse, boolean beforeChildren) {
        Vec2f anchor = mouse;
        Runnable draw = () -> drawDeferred(anchor);
        ((Render2DEvent) drawList).postTasks.add(draw);
    }

    public void drawDeferred(Vec2f anchor) {
        String title = this.getText();
        String ext = this instanceof LastUsedTooltip ? null : this.extended;
        boolean isHovered = this.hovered.getAsBoolean();
        this.draw(title, ext, isHovered, anchor, 170);
    }

    private Tooltip findPrevious() {
        var decorators = this.owner.modifiers;
        Tooltip last = null;
        if (decorators == null) {
            return null;
        }
        for (com.adjustclient.ui.clickgui.Modifier d : decorators) {
            if (d instanceof Tooltip) {
                Tooltip t = (Tooltip) d;
                if (t.fade != null && t.fade.value != 0.0F && t.text.length() != 0) {
                    if (t == this) {
                        return last;
                    }
                    last = t;
                }
            }
        }
        return null;
    }

    private void draw(String titleText, String extendedText, boolean isHovered, Vec2f anchor, int maxAlpha) {
        float ax = anchor.x;
        float ay = anchor.y;
        Timer timer = this.hoverTimer;
        if (!isHovered || this.text.length() == 0) {
            if (timer != null) {
                timer.lastMs = System.currentTimeMillis();       // keep the timer reset while not hovered
            }
            if (this.fade != null) {
                this.fade.update(0.0F);                           // fade out
                if (this.fade.finished) {                          // finished
                    this.hoverTimer = null;
                    this.fade = null;
                    this.previous = null;
                }
            }
        } else {
            if (timer == null) {
                timer = new Timer();
                this.hoverTimer = timer;
            }
            if (timer.lastMs + this.delay <= System.currentTimeMillis()) {
                if (this.fade == null) {
                    this.fade = new Animation(Easing.LINEAR, 90L);
                }
                this.fade.update(1.0F);                           // fade in
            }
        }
        if (this.fade == null || this.fade.value == 0.0F) {
            return;
        }
        if (this.text.length() == 0) {
            return;
        }
        if (this.previous == null) {
            this.previous = this.findPrevious();
        }
        int alpha = MathHelper.clamp_int((int) (this.fade.value * 170.0F), 0, 255);
        String hint = "Hold " + (ChatColor.WHITE != null ? ChatColor.WHITE.toString() : "null") + "LCONTROL"
                + (ChatColor.RESET != null ? ChatColor.RESET.toString() : "null") + " to view extended description";
        float stackOffset = this.previous != null ? this.previous.bottom + 2.0F : 0.0F;
        float top = (ay - 6.0F) + stackOffset;
        float x0 = ax + 5.0F;
        float width = Fonts.TAHOMA_10.getStringWidth(titleText);
        float lineHeight;
        boolean ctrl = false;
        if (extendedText == null) {
            lineHeight = 6.5F;
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            ctrl = true;
            width = Math.max(width, Fonts.TAHOMA_10.getStringWidth(extendedText));
            lineHeight = 14.0F;
        } else {
            width = Math.max(width, Fonts.TAHOMA_8.getStringWidth(hint));
            lineHeight = 13.0F;
        }
        this.bottom = lineHeight + stackOffset;
        float boxWidth = width + 2.0F;
        float left = (float) AdjustScreen.screenResolution.getScaledWidth() - boxWidth - 2.0F;
        if (left >= x0) {
            left = x0;
        }
        float right = left + boxWidth;
        float bottomEdge = top + lineHeight;
        NvgDraw.fillRect(new Color(25, 25, 28, alpha).getRGB(), left, top, right, bottomEdge);
        NvgDraw.strokeRect(true, new Color(35, 35, 38, alpha).getRGB(), left, top, right, bottomEdge, 0.5F);
        int textColor = new Color(233, 235, 234, alpha).getRGB();
        Fonts.TAHOMA_10.drawString(titleText, left + 1.0F, top + 2.5F, textColor);
        if (extendedText != null) {
            if (ctrl) {
                Fonts.TAHOMA_10.drawString(extendedText, left + 1.0F, top + 9.5F, textColor);
            } else {
                Fonts.TAHOMA_8.drawString(hint, left + 1.0F, top + 9.5F, textColor);
            }
        }
    }
}
