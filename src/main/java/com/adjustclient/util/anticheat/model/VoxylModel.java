package com.adjustclient.util.anticheat.model;
import com.adjustclient.server.ServerType;
public final class VoxylModel extends AntiCheatModel {
    public Object field20;
    public VoxylModel(ServerType serverType) { super(serverType); }
    public boolean allowsCapabilityC() { return true; }
    public boolean allowsCapabilityD() { return true; }
}
