package com.adjustclient.module.impl.render.hud.playerlist;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.util.Timer;

public final class HoverCardState {
    public Animation visible;
    public Animation pinFade;
    public Animation grow;
    public Timer hoverTimer;
    public com.adjustclient.module.impl.render.hud.playerlist.menu.HoverMenu menu;
    public PlayerEntry entry;
    public com.adjustclient.util.anticheat.entity.TrackedEntity trackedPlayer;
    public com.adjustclient.util.Vec2f position;
    public boolean pinned;
    public boolean menuOpen;
    public HoverCardState(PlayerEntry entry) {
        this.entry = entry;
        this.visible = new Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 175L);
        this.pinFade = new Animation(com.adjustclient.render.animation.Easing.EASE_OUT_EXPO, 560L);
        this.grow = new Animation(com.adjustclient.render.animation.Easing.EASE_OUT_EXPO, 0L);
    }
}
