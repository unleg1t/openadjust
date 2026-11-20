package com.adjustclient.utilities.anticheat.model;
import com.adjustclient.utilities.server.ServerType;
public final class VanillaModel extends AntiCheatModel {
    public Object field20;
    public VanillaModel(ServerType serverType) { super(serverType); }
    public boolean allowsCapabilityC() { return true; }
    public boolean allowsCapabilityD() { return true; }
}
