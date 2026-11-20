package com.adjustclient.core.gui.hud.notification;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.Cooldown;
import com.adjustclient.core.rendering.font.TextComponent;

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
