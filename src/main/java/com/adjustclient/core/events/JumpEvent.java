package com.adjustclient.core.events;

import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.utilities.MovementUtil;

public final class JumpEvent extends CancellableEvent {

    public static boolean isJumpCancelled() {
        if (ScaffoldModule.INSTANCE != null && ScaffoldModule.INSTANCE.isEnabled()) {
            ScaffoldModule.INSTANCE.onJump(new JumpEvent());
        }
        return MovementUtil.isAutoJumping();
    }
}
