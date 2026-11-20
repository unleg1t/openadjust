package com.adjustclient.modules.impl.player.nofall.mode;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.modules.impl.player.nofall.NoFallMode;
import com.adjustclient.modules.impl.player.nofall.NoFallModule;
import com.adjustclient.utilities.WorldUtil;

public final class RewoundNoFallMode extends NoFallMode {
    public RewoundNoFallMode(NoFallModule module) {
        super(module, "Rewound");
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive() || !event.pre || !module.canUse()) return;
        int safeFall = WorldUtil.safeFallDistance(mc.thePlayer);
        if (mc.thePlayer.fallDistance - module.lastFallDistance >= (double) safeFall + mc.thePlayer.motionY) {
            event.onGround = true;
            module.lastFallDistance = mc.thePlayer.fallDistance;
        }
    }
}
