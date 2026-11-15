package com.adjustclient.module.impl.player.nofall.mode;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.module.impl.player.nofall.*;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.WorldUtil;
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
        com.adjustclient.service.util.ServiceUtil.reportFeatureUse(
                noGroundMode
                        ? com.adjustclient.service.telemetry.BypassFeature.NOFALL_SPOOF_NOGROUND
                        : com.adjustclient.service.telemetry.BypassFeature.NOFALL_SPOOF);
    }
}
