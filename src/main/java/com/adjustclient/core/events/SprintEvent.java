package com.adjustclient.core.events;

import com.adjustclient.modules.impl.movement.noslowdown.NoSlowMode;
import com.adjustclient.modules.impl.movement.sprint.SprintModule;
import com.adjustclient.modules.impl.movement.noslowdown.CustomNoSlowMode;
import com.adjustclient.modules.impl.movement.noslowdown.SwapNoSlowMode;
import com.adjustclient.modules.impl.movement.noslowdown.VanillaNoSlowMode;

public final class SprintEvent extends CancellableEvent {
    public SprintState state;

    public SprintEvent(SprintState state) {
        this.state = state;
    }

    public static boolean isSprintAllowed() {
        NoSlowMode first = CustomNoSlowMode.INSTANCE;
        if (first != null) first.isActive();
        NoSlowMode second = SwapNoSlowMode.INSTANCE;
        if (second != null) second.isActive();
        NoSlowMode third = VanillaNoSlowMode.INSTANCE;
        if (third != null) third.isActive();
        SprintModule sprint = SprintModule.INSTANCE;
        return sprint == null || !sprint.isEnabled() || !sprint.ignoreBlindness.isEnabled();
    }
}
