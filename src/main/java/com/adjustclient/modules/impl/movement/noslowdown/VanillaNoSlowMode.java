package com.adjustclient.modules.impl.movement.noslowdown;

import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.ItemSlowdownEvent;
import com.adjustclient.core.events.MovementInputEvent;
import com.adjustclient.core.events.SprintEvent;
import com.adjustclient.core.events.SprintState;
import com.adjustclient.core.settings.BooleanSetting;

public final class VanillaNoSlowMode extends NoSlowMode {
    public static VanillaNoSlowMode INSTANCE;
    public final BooleanSetting sneaking;
    public final BooleanSetting slowness;

    public VanillaNoSlowMode(NoSlowdownModule module, Feature feature) {
        super(module, "Vanilla", feature);
        INSTANCE = this;
        sneaking = new BooleanSetting(module, "Sneaking", true,
                () -> module.mode.getValue() != this);
        sneaking.setDisplayName("vanillaSneaking");
        slowness = new BooleanSetting(module, "Slowness", true,
                () -> module.mode.getValue() != this);
        slowness.setDisplayName("vanillaSlowness");
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (isActive()) event.apply = false;
    }

    @EventHandler
    public void onSprint(SprintEvent event) {
        if (isActive() && event.state == SprintState.SLOWNESS && slowness.isEnabled()) event.cancelled = true;
    }

    @EventHandler
    public void onSneakSlowdown(MovementInputEvent event) {
        if (isActive() && sneaking.isEnabled()) event.sneakMultiplier = 1.0;
    }

    @Override
    public boolean removesSlowdown() {
        return true;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
