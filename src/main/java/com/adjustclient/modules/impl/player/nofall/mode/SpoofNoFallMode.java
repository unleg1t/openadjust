package com.adjustclient.modules.impl.player.nofall.mode;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.modules.impl.player.nofall.*;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.utilities.WorldUtil;
public final class SpoofNoFallMode extends NoFallMode {
    public final BooleanSetting noGround;
    public SpoofNoFallMode(NoFallModule module) {
        super(module, "Spoof");
        noGround = new BooleanSetting(module, "No ground", false,
                () -> {
                    return !(module.mode.getValue() instanceof SpoofNoFallMode);
                });
    }
    @EventHandler public void onMotion(MotionEvent event) {
        if (!isActive() || !event.pre) return;
        if (noGround.isEnabled()) {
            event.onGround = false;
        } else if (mc.thePlayer.fallDistance - module.lastFallDistance >= WorldUtil.safeFallDistance(mc.thePlayer)) {
            module.lastFallDistance = mc.thePlayer.fallDistance;
            event.onGround = true;
        }
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass(boolean noGroundMode) {
        com.adjustclient.managers.util.ServiceUtil.reportFeatureUse(
                noGroundMode
                        ? com.adjustclient.managers.telemetry.BypassFeature.NOFALL_SPOOF_NOGROUND
                        : com.adjustclient.managers.telemetry.BypassFeature.NOFALL_SPOOF);
    }
}
