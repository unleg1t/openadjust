package com.adjustclient.event;

import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.util.MovementUtil;

public final class JumpEvent extends CancellableEvent {

    public static boolean isJumpCancelled() {
        if (ScaffoldModule.INSTANCE != null && ScaffoldModule.INSTANCE.isEnabled()) {
            ScaffoldModule.INSTANCE.onJump(new JumpEvent());
        }
        return MovementUtil.isAutoJumping();
    }
}
