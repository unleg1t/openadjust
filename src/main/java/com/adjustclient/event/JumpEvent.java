package com.adjustclient.event;

import com.adjustclient.module.impl.movement.PearlAssistModule;
import com.adjustclient.module.impl.movement.scaffold.NcpScaffoldMode;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldMode;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldSettings;

public final class JumpEvent extends CancellableEvent {

    public static boolean isJumpCancelled() {
        PearlAssistModule pearl = PearlAssistModule.INSTANCE;
        boolean blocked = false;
        if (pearl != null && pearl.isEnabled() && pearl.safetyActive) {
            double seconds = ((Number) pearl.jumpRestrict.getValue()).doubleValue();
            if (seconds > 0.0) {
                blocked = pearl.targetTimer.lastMs + (long) (seconds * 1000.0)
                        > System.currentTimeMillis();
            }
        }
        JumpEvent event = new JumpEvent();
        event.cancelled = blocked;
        if (blocked) return event.cancelled;
        ScaffoldMode ncp = ScaffoldSettings.NCP;
        if (ncp != null) {
            ScaffoldModule scaffold = ncp.module;
            if (scaffold.isEnabled() && scaffold.settings.mode.getValue() == ncp) {
                ((NcpScaffoldMode) ncp).onTowerStart(event);
            }
        }
        return event.cancelled;
    }
}
