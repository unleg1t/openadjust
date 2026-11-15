package com.adjustclient.util.anticheat.model;
import com.adjustclient.server.ServerType;
public final class VanillaModel extends AntiCheatModel {
    public Object field20;
    public VanillaModel(ServerType serverType) { super(serverType); }
    public boolean allowsCapabilityC() { return true; }
    public boolean allowsCapabilityD() { return true; }
}
