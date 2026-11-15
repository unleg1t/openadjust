package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;

public final class TweaksModule extends Module {
    public static TweaksModule INSTANCE;
    public final Tweak[] tweaks;
    public final ScoreboardTweak scoreboard;
    public final ChatTweak chat;
    public final TabOverlayTweak tabOverlay;
    public final ZoomTweak zoom;
    public final SmoothRotationYawTweak smoothRotationYaw;
    public final MinimalBobbingTweak minimalBobbing;
    public final DisconnectConfirmTweak disconnectConfirm;
    public final NoHotbarScrollTweak noHotbarScroll;
    public final NoTitleTweak noTitle;
    public final NoBossBarTweak noBossBar;
    public final FullscreenTweak fullscreen;
    public Object field80;

    public TweaksModule() {
        super("Tweaks", Category.RENDER);
        INSTANCE = this;
        setHidden(false);
        scoreboard = new ScoreboardTweak(this);
        chat = new ChatTweak(this);
        tabOverlay = new TabOverlayTweak(this);
        zoom = new ZoomTweak(this);
        smoothRotationYaw = new SmoothRotationYawTweak(this);
        minimalBobbing = new MinimalBobbingTweak(this);
        disconnectConfirm = new DisconnectConfirmTweak(this);
        noHotbarScroll = new NoHotbarScrollTweak(this);
        noTitle = new NoTitleTweak(this);
        noBossBar = new NoBossBarTweak(this);
        fullscreen = new FullscreenTweak(this);
        tweaks = new Tweak[] { scoreboard, chat, tabOverlay, zoom, smoothRotationYaw,
                minimalBobbing, disconnectConfirm, noHotbarScroll, noTitle, noBossBar, fullscreen };
    }

    @Override public void onEnable() { for (Tweak tweak : tweaks) tweak.onEnable(); }

    @Override public void onDisable() { for (Tweak tweak : tweaks) tweak.onDisable(); }

}
