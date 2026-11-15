package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.Adjust;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.Timer;

final class BlinkAntiVoidState {
    public final PacketHold hold;
    public final Simulation simulation;
    public final FallSimulationResult safeState;
    public final MovementStateSnapshot walkingState;
    public final Timer timer;

    BlinkAntiVoidState(Simulation simulation, FallSimulationResult safeState, MovementStateSnapshot walkingState) {
        long now = System.currentTimeMillis();
        this.hold = Adjust.outboundPacketQueue.addHold(null, false);
        this.simulation = simulation;
        this.safeState = safeState;
        this.walkingState = walkingState;
        this.timer = new Timer(now);
    }
}
