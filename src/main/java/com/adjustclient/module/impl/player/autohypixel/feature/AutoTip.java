package com.adjustclient.module.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.UserInputVerificationEvent;
import com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.Timer;

public final class AutoTip extends AutoHypixelFeature {
    public static AutoTip INSTANCE;
    public final BooleanSetting enabled;
    public final Timer timer = new Timer();

    public AutoTip(AutoHypixelModule owner) {
        super(owner);
        INSTANCE = this;
        enabled = new BooleanSetting(owner, "Auto Tip", true);
    }

    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    @EventHandler public void onTick(UserInputVerificationEvent event) {
        if (!isActive() || mc.thePlayer == null || !mc.thePlayer.sendQueue.doneLoadingTerrain
                || mc.thePlayer.ticksExisted < 61 || !timer.hasElapsed(900000L)) return;
        Adjust.chatMessageSender.sendMessage("/tip all", false);
        timer.reset();
    }
}
