package com.adjustclient.modules.impl.movement.flight;

import com.adjustclient.utilities.anticheat.feature.Features;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.ModeSetting;
import java.util.Objects;

public final class FlightModule extends Module {
    public static FlightModule INSTANCE;
    public final ModeSetting<FlightMode> mode;

    public FlightModule() {
        super("Flight", Category.MOVEMENT);
        INSTANCE = this;
        notifyToggle = false;
        VanillaFlightMode vanilla = new VanillaFlightMode(this);
        AirWalkFlightMode airWalk = new AirWalkFlightMode(this);
        RewoundFlightMode rewound = new RewoundFlightMode(this);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, airWalk, rewound);
        mode.addListener((previous, next) -> {
            FlightMode oldMode = (FlightMode) previous;
            FlightMode newMode = (FlightMode) next;
            if (!isEnabled() || newMode == oldMode) return;
            if (oldMode != null) oldMode.onDisable();
            Objects.requireNonNull(newMode).onEnable();
        });
        settings.remove(mode);
        settings.add(0, mode);
    }

    @EventHandler
    public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (!Features.FLIGHT.isAllowed()) setEnabled(false, true);
    }

    @Override
    public boolean canToggle(boolean enabling) {
        return !enabling || Features.FLIGHT.isAllowed();
    }

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
    }

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }
}
