package com.adjustclient.modules.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.UserInputVerificationEvent;
import com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.utilities.Timer;

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
