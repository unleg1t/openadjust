package com.adjustclient.ui.hud.notification;

import java.util.ArrayList;
import java.util.List;
import com.adjustclient.render.font.AccentText;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.util.Cooldown;
import com.adjustclient.util.Timer;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;

public class NotificationBuilder {

    public String title;
    public List<TextComponent> parts;
    public Object key;
    public Long duration;
    public Integer lifetimeSteps;
    public List<NotificationAction> actions;
    public boolean clearable;
    public Object field32;

    public NotificationBuilder() {
        this.parts = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    public NotificationBuilder(String title) {
        this();
        this.title = title;
    }

    public NotificationBuilder(String title, List<TextComponent> parts, Integer lifetimeSteps) {
        this(title);
        this.parts = parts;
        this.lifetimeSteps = lifetimeSteps;
    }

    public void show() {
        Notification notification = this.build();
        Notifications manager = com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.notifications;
        manager.add(notification);
    }

    /** Appends a coloured text part. */
    public NotificationBuilder addText(String text, int color) {
        this.parts.add(new TextComponent(text, color));
        return this;
    }

    public NotificationBuilder append(String object) {
        this.parts.add(new AccentText(object));
        return this;
    }

    public NotificationBuilder format(String pattern, Object... args) {
        StringBuilder literal = new StringBuilder();
        int argIndex = 0;
        for (int i = 0; i < pattern.length(); ) {
            char c = pattern.charAt(i);
            if (c == '{' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '}') {
                if (literal.length() > 0) {
                    this.parts.add(new TextComponent(literal.toString(), 0xFFBEBCBD));
                    literal.setLength(0);
                }
                String argStr = argIndex < args.length ? String.valueOf(args[argIndex++]) : "{}";
                this.append(argStr);
                i += 2;
            } else {
                literal.append(c);
                i++;
            }
        }
        if (literal.length() > 0) {
            this.parts.add(new TextComponent(literal.toString(), 0xFFBEBCBD));
        }
        return this;
    }

    /** Creates the {@link Notification}: parts / actions copied to arrays, timers and animations created, lifetime computed. */
    public Notification build() {
        Notification n = new Notification();
        long now1 = System.currentTimeMillis();
        long now2 = System.currentTimeMillis();
        long now3 = System.currentTimeMillis();
        long now4 = System.currentTimeMillis();
        n.title = this.title;
        n.parts = this.parts.toArray(new TextComponent[0]);
        int steps;
        if (this.lifetimeSteps != null) {
            steps = this.lifetimeSteps;
        } else {
            // reading time: 7 + 1.32 per character over all parts, at least 22 steps of 45 ms
            int sum = 7;
            for (TextComponent part : this.parts) {
                sum += (int) ((float) part.text.length() * 1.32F);
            }
            steps = Math.max(22, sum);
        }
        n.stepTimer = new Timer(now1);
        n.slide = new Animation(Easing.EASE_OUT_SINE, 0L, now2);
        n.position = new Animation(Easing.EASE_OUT_SINE, 0L, now3);
        n.fade = new Animation(Easing.LINEAR, 0L, now4);
        n.key = this.key;
        n.lifetimeSteps = steps;
        n.state = 0;
        n.actionRun = false;
        n.clearable = this.clearable;
        n.field52 = null;
        if (this.duration != null) {
            n.duration = new Cooldown(this.duration, new Timer(System.currentTimeMillis()));
        }
        n.actions = this.actions.toArray(new NotificationAction[0]);
        for (NotificationAction action : n.actions) {
            action.owner = n;
        }
        return n;
    }
}
