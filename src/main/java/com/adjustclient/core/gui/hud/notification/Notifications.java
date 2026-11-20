package com.adjustclient.core.gui.hud.notification;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.utilities.Timer;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.adjustclient.core.logging.AnsiColor;
import com.adjustclient.core.logging.Logger;
import net.minecraft.util.ChatColor;
import com.adjustclient.core.gui.mainmenu.AdjustMainMenu;

public final class Notifications implements com.adjustclient.core.events.Listener {

    public List<Notification> list = new ArrayList<>();
    public NotificationAction hovered;
    public Object field12;

    /** Removes every clearable notification ("clear all"). */
    public void clearClearable() {
        synchronized (this.list) {
            if (this.list.isEmpty()) {
                return;
            }
            Set<Notification> clearable = this.list.stream().filter(n -> n.clearable).collect(Collectors.toUnmodifiableSet());
            this.list.removeAll(clearable);
        }
    }

    /** Adds a notification (only while the HUD module is enabled); a notification with an equal key replaces the existing one in place. */
    public void add(Notification notification) {
        HUDModule hud = HUDModule.INSTANCE_ALT;
        if ((hud.flags & 2) == 0) {                              // module flags: enabled
            return;
        }
        synchronized (this.list) {
            if (notification.key != null) {
                Notification existing = this.find(notification);
                if (existing != null) {
                    notification.position = existing.position;
                    notification.slide = existing.slide;
                    notification.fade = existing.fade;
                    this.list.set(this.list.indexOf(existing), notification);
                    return;
                }
            }
            this.list.add(notification);
        }
    }

    /** The notification in the list with the same key (key objects are equal when they are the same or Strings with equal content). */
    public Notification find(Notification notification) {
        for (Notification candidate : this.list) {
            Object key = candidate.key;
            if (key == null) {
                continue;
            }
            if ((key == notification.key || (notification.key != null && key.equals(notification.key))) && candidate.state != 3) {
                return candidate;
            }
        }
        return null;
    }

    public boolean advance(Notification n) {
        Timer timer = n.stepTimer;
        if (timer.lastMs + 45L > System.currentTimeMillis()) {
            return false;
        }
        if (n.duration == null && !AdjustMainMenu.windowActive) {
            return false;
        }
        if (n.state != 0) {
            if (n.state == 3) {
                return true;
            }
        } else {
            int next = n.step + 1;
            boolean expired;
            if (n.duration != null && n.duration.durationMs >= 0L) {
                expired = n.duration.timer.lastMs + n.duration.durationMs <= System.currentTimeMillis();
            } else {
                expired = next >= n.lifetimeSteps;
            }
            if (expired) {
                n.state = 2;
            } else {
                n.step = next;
            }
        }
        timer.lastMs = System.currentTimeMillis();
        return false;
    }

    /**
     * Executes the hovered action (called on a mouse click by the HUDModule input handler): marks the owner as dismissing when the action
     * closes it, runs the ClickEvent and sets {@code actionRun}; an exception is logged as "Failed to run notification '{}' ClickEvent: {}".
     *
     * @return true when an action was run
     */
    public boolean runClick() {
        NotificationAction action = this.hovered;
        if (action == null || action.owner.actionRun) {
            return false;
        }
        boolean any;
        synchronized (this.list) {
            any = !this.list.isEmpty();
        }
        if (!any) {
            return false;
        }
        if (action.dismissOnClick) {
            action.owner.state = 2;
        }
        try {
            action.event.run();
        } catch (Exception e) {
            Logger.log(com.adjustclient.core.logging.LogLevel.WARN, true, "Failed to run notification '{}' ClickEvent: {}", new Object[] { action.owner.title, e });
        }
        action.owner.actionRun = true;
        return true;
    }

    public boolean animate(Notification n, boolean instant, float x, float y, float z) {
        byte state = n.state;
        if (instant) {
            n.slide.duration = 0L;
            n.position.duration = 0L;
            n.fade.duration = 0L;
        } else {
            n.slide.duration = 175L;
            n.position.duration = 190L;
            n.fade.duration = 190L;
        }
        if (state == 2) {
            n.slide.update(0.0F);
            n.position.update(0.0F);
            n.fade.update(0.0F);
            if (n.fade.finished) {
                n.state = 3;
                return false;
            }
            return true;
        }
        if (state == 0) {
            n.slide.update(x + 6.0F);
            n.position.update(z + (y + 6.0F));
            n.fade.update(1.15F);
        }
        return true;
    }

    /** Per frame entry point: does nothing for an empty list, otherwise resets {@link #hovered} and runs the update / render pass under the lock. */
    @com.adjustclient.core.events.EventHandler
    public void render(com.adjustclient.core.events.HudMouseRenderEvent args) {
        boolean empty;
        synchronized (this.list) {
            empty = this.list.isEmpty();
        }
        if (empty) {
            return;
        }
        this.hovered = null;
        synchronized (this.list) {
            this.renderPass(this.list, args.scaledResolution, args.getMouseX(), args.mouseY);
        }
    }

    /** Removes finished notifications; draw retains the stacking loop to preserve this call's shape. */
    private void renderPass(List<Notification> list, Object context, float x, float y) {
        list.removeIf(n -> this.advance(n));
        this.draw(context, x, y);
    }

    private void draw(Object context, float x, float y) {
        ScaledResolution resolution = (ScaledResolution) context;
        float offset = Minecraft.getMinecraft().currentScreen instanceof GuiChat
                ? 13.0F / Math.max(1.0F, (float) (resolution.getScaleFactor_double() - 1.0D)) : 0.0F;
        int limit = (int) ((resolution.getScaledHeight() / 2.0F) / 30.0F);
        if (limit < 1) return;
        for (int index = 0; index < this.list.size() && index < limit; index++) {
            int more = index == 0 && this.list.size() > limit ? this.list.size() - limit : -1;
            boolean instant = this.list.size() > limit;
            Notification card = this.list.get(index);
            float height = more == -1 ? 20.0F : 27.0F;
            if (card.actions.length != 0) height += 7.0F;
            NvgFontRenderer font = Fonts.TAHOMA_12;
            float width = 5.0F;
            for (TextComponent part : card.parts) {
                String text = StreamerModule.replaceKnownNames(part.text);
                width = font.getStringWidth(text) + width;
            }
            width = Math.max(width, font.getStringWidth(card.title) + 12.0F);
            float left = resolution.getScaledWidth() - card.slide.value;
            if (!this.animate(card, instant, width, height, offset)) continue;
            float alpha = Math.max(card.fade.value - 0.15F, 0.0F);
            Color accent = HUDModule.animatedColor != null
                    ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            accent = ColorUtil.withAlpha(accent, (int) (accent.getAlpha() * (double) alpha));
            float top = resolution.getScaledHeight() - card.position.value;
            float right = width + left;
            float bottom = height + top;
            NvgDraw.fillRect(new Color(25, 25, 28, (int) (alpha * 130.0F)).getRGB(), left, top, right, bottom);
            NvgDraw.strokeRect(true, new Color(25, 25, 28, (int) (alpha * 190.0F)).getRGB(),
                    left, top, right, bottom, 0.5F);
            RenderUtil.drawShadowRect(accent, left, top, width);
            Color darkAccent = new Color((int) (accent.getRed() * 0.7),
                    (int) (accent.getGreen() * 0.7), (int) (accent.getBlue() * 0.7), accent.getAlpha());
            RenderUtil.drawShadowRect(darkAccent, left, top + 0.5F, width);
            float textX = left + 2.5F;
            font.drawString(ChatColor.BOLD + card.title, textX, top + 5.0F, accent.getRGB());
            float actionY = top + 21.5F;
            if (card.actions.length != 0) {
                RenderUtil.pushScissor(resolution, 1, left, top, width, height);
                float actionOffset = 0.0F;
                for (NotificationAction action : card.actions) {
                    float labelWidth = Fonts.TAHOMA_10.getStringWidth(action.label);
                    float actionX = textX + actionOffset;
                    if (this.hovered == null && x >= actionX && actionX + labelWidth >= x
                            && y >= actionY && actionY + 5.0F >= y) {
                        this.hovered = action;
                    }
                    action.hover.update(this.hovered == action ? 1.0F : 0.0F);
                    Color color = ColorUtil.blend(new Color(action.color), new Color(167, 166, 165),
                            action.hover.value);
                    Fonts.TAHOMA_10.drawString(action.label, actionX, actionY, color.getRGB());
                    actionOffset = (labelWidth + 5.0F) + actionOffset;
                }
                RenderUtil.popScissor();
            }
            if (more != -1) {
                RenderUtil.pushScissor(resolution, 1, left, top, width, height);
                String text = more + " more " + (more == 1 ? "notification" : "notification" + "s") + "...";
                float moreY = (card.actions.length == 0 ? 0.0F : 7.0F) + actionY;
                Fonts.TAHOMA_10.drawString(text, textX, moreY, new Color(167, 166, 165).getRGB());
                RenderUtil.popScissor();
            }
            float bodyY = top + 13.0F;
            int bodyAlpha = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
            for (TextComponent part : card.parts) {
                int color = part.getColor() | 0xFF000000;
                String text = StreamerModule.replaceKnownNames(part.text);
                if (bodyAlpha != 255) color = (color & 0x00FFFFFF) | (bodyAlpha << 24);
                font.drawString(text, textX, bodyY, color);
                textX = font.getStringWidth(text) + textX;
            }
            offset = (height + 4.0F) + offset;
        }
    }
}
