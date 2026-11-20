package com.adjustclient.utilities.anticheat.model;

import com.adjustclient.core.events.ProtocolWarningType;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.blocker.BlockerState;
import net.minecraft.network.Packet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AntiCheatModel {
    public ServerType serverType;
    public String field8;
    public AntiCheatCheck[] checks;
    public Set<ProtocolWarningType> field16 = new HashSet<>();
    
    protected AntiCheatModel(ServerType serverType, AntiCheatCheck... checks) {
        this.serverType = serverType;
        this.checks = checks;
    }

    public void onActivate(com.adjustclient.core.events.DisconnectTeardownEvent event) { }

    public void setServerData(BlockerState server) {
        for (AntiCheatCheck check : checks) check.setServerData(server);
    }

    public boolean isAllChecksPassed() {
        return Arrays.stream(checks).allMatch(check -> check.passed);
    }

    public void onPacket(Packet<?> packet) {
        for (AntiCheatCheck check : checks) {
            if (!check.passed && check.onPacket(packet)) check.passed = true;
        }
    }

    public abstract boolean allowsCapabilityC();
    public abstract boolean allowsCapabilityD();
}
