package com.adjustclient.core.commands.impl.debug;

import com.adjustclient.core.commands.ParentCommand;
import com.adjustclient.core.commands.SubCommand;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.anticheat.ServerTracker;

public class DebugServerSubCommand extends SubCommand {
    public Object field16;

    public DebugServerSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        var server = ServerTracker.INSTANCE.serverState;
        if (server == null) { ((DebugCommand) parent).printDebugData("Server debug", null); return; }
        var watcher = server.packetWatcher;
        int ping = watcher.delayCount == 0 ? 0 : watcher.totalDelay / watcher.delayCount;
        String data = "IP: " + server.field12 + "\nPort: " + server.field48 + "\nPing: " + ping + "\n";
        var model = server.antiCheatModel;
        if (model != null) {
            data += "Known Server: " + model.serverType.displayName
                    + "\nisBypassesEnabled(): " + model.isAllChecksPassed()
                    + "\ncanRunInventoryModules(): " + model.allowsCapabilityC()
                    + "\nhasBotEntities(): " + model.allowsCapabilityD() + "\n";
        }
        data += "Issued By: " + Adjust.username;
        ((DebugCommand) parent).printDebugData("Server debug", data);
    }
}
