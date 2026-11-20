package com.adjustclient.modules.impl.render.hud.playerlist;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.utilities.Timer;

public final class HoverCardState {
    public Animation visible;
    public Animation pinFade;
    public Animation grow;
    public Timer hoverTimer;
    public com.adjustclient.modules.impl.render.hud.playerlist.menu.HoverMenu menu;
    public PlayerEntry entry;
    public com.adjustclient.utilities.anticheat.entity.TrackedEntity trackedPlayer;
    public com.adjustclient.utilities.Vec2f position;
    public boolean pinned;
    public boolean menuOpen;
    public HoverCardState(PlayerEntry entry) {
        this.entry = entry;
        this.visible = new Animation(com.adjustclient.core.rendering.animation.Easing.EASE_OUT_SINE, 175L);
        this.pinFade = new Animation(com.adjustclient.core.rendering.animation.Easing.EASE_OUT_EXPO, 560L);
        this.grow = new Animation(com.adjustclient.core.rendering.animation.Easing.EASE_OUT_EXPO, 0L);
    }
}
