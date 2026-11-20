package com.adjustclient.modules.impl.render.tweaks;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;

public final class TweaksModule extends Module {
    public static TweaksModule INSTANCE;
    public final Tweak[] tweaks;
    public final ScoreboardTweak scoreboard;
    public final ChatTweak chat;
    public final BowTweak bow;
    public final TabOverlayTweak tabOverlay;
    public final ZoomTweak zoom;
    public final NoRenderTweak noRender;
    public final SmoothRotationYawTweak smoothRotationYaw;
    public final MinimalBobbingTweak minimalBobbing;
    public final DisconnectConfirmTweak disconnectConfirm;
    public final NoHotbarScrollTweak noHotbarScroll;
    public final FullscreenTweak fullscreen;
    public Object field80;

    public TweaksModule() {
        super("Tweaks", Category.RENDER);
        INSTANCE = this;
        setHidden(false);
        scoreboard = new ScoreboardTweak(this);
        chat = new ChatTweak(this);
        bow = new BowTweak(this);
        tabOverlay = new TabOverlayTweak(this);
        zoom = new ZoomTweak(this);
        noRender = new NoRenderTweak(this);
        smoothRotationYaw = new SmoothRotationYawTweak(this);
        minimalBobbing = new MinimalBobbingTweak(this);
        disconnectConfirm = new DisconnectConfirmTweak(this);
        noHotbarScroll = new NoHotbarScrollTweak(this);
        fullscreen = new FullscreenTweak(this);
        tweaks = new Tweak[] {
                scoreboard, chat, bow, tabOverlay, zoom, noRender,
                smoothRotationYaw, minimalBobbing, disconnectConfirm, noHotbarScroll, fullscreen
        };
    }

    @Override
    public void onEnable() {
        for (Tweak tweak : tweaks) tweak.onEnable();
    }

    @Override
    public void onDisable() {
        for (Tweak tweak : tweaks) tweak.onDisable();
    }
}
