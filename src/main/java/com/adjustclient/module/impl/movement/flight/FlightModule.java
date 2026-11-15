package com.adjustclient.module.impl.movement.flight;

import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.MovementUtil;

public final class FlightModule extends Module {
    public static FlightModule INSTANCE;
    public final ModeSetting<FlightMode> mode;

    public FlightModule() {
        super("Flight", Category.MOVEMENT);
        INSTANCE = this;
        notifyToggle = false;
        VanillaFlightMode vanilla = new VanillaFlightMode(this);
        AirWalkFlightMode airWalk = new AirWalkFlightMode(this);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, airWalk);
        mode.addListener((previous, next) -> {
            if (!isEnabled() || next == previous) return;
            if (previous instanceof VanillaFlightMode && mc.thePlayer != null) {
                MovementUtil.setSpeed(Math.min(MovementUtil.getSpeed(),
                        MovementUtil.getSpeedWithPotion(0.164D, 0.2D)));
                mc.thePlayer.motionY = 0.0D;
            }
            java.util.Objects.requireNonNull(next);
        });
        settings.remove(mode);
        settings.add(0, mode);
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (!com.adjustclient.anticheat.feature.Features.FLIGHT.isAllowed()) setEnabled(false, true);
    }

    @Override public boolean canToggle(boolean enabling) {
        return !enabling || com.adjustclient.anticheat.feature.Features.FLIGHT.isAllowed();
    }

    @Override public void onEnable() { java.util.Objects.requireNonNull(mode.getValue()); }

    @Override public void onDisable() {
        if (mode.getValue() instanceof VanillaFlightMode && mc.thePlayer != null) {
            MovementUtil.setSpeed(Math.min(MovementUtil.getSpeed(),
                    MovementUtil.getSpeedWithPotion(0.164D, 0.2D)));
            mc.thePlayer.motionY = 0.0D;
        }
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }
}
