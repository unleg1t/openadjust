package com.adjustclient.ui.hud.notification;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Cooldown;
import com.adjustclient.render.font.TextComponent;

public final class Notification {

    public String title;
    public TextComponent[] parts;
    public Timer stepTimer;
    public Animation slide;
    public Animation position;
    public Animation fade;
    public NotificationAction[] actions;
    public Cooldown duration;
    public Object key;
    public int lifetimeSteps;
    public int step;
    public byte state;
    public boolean actionRun;
    public boolean clearable;
    public Object field52;
}
