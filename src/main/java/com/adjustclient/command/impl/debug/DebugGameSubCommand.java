package com.adjustclient.command.impl.debug;

import com.adjustclient.command.ParentCommand;
import com.adjustclient.command.SubCommand;
import com.adjustclient.Adjust;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.game.player.InventoryStateTracker;

public final class DebugGameSubCommand extends SubCommand {
    public Object field16;

    public DebugGameSubCommand(ParentCommand parent, String name) { super(parent, name); }

    @Override public void run(String label, String[] args) {
        var state = Adjust.gameStateTracker.state;
        var tracker = ServerTracker.INSTANCE;
        var server = tracker.serverState;
        int ping = server == null ? 0 : server.packetWatcher.getAverageDelay();
        String data = "Type: " + state.game.name()
                + "\nLobby/Game: " + (state.lobby == null ? "Undetermined" : state.lobby ? "Lobby" : "Game")
                + "\nStarted: " + (state.startTimer == null ? "No" : "Yes (" + state.startTimer.formatElapsed() + ")")
                + "\nCreated: " + state.locationTimer.formatElapsed()
                + "\nEnded: " + state.ended
                + "\nSpectator: " + state.isSpectator()
                + "\nTournament: " + state.tournament
                + "\nSpawn: " + state.isSpecialState()
                + "\nhasDug(): " + tracker.dugSinceMovement
                + "\nhasPlaced(): " + tracker.usedItemSinceMovement
                + "\nhasAttacked(): " + (tracker.attackedEntity != null)
                + "\nisInServersideGui(): " + InventoryStateTracker.isInServersideGui()
                + "\nPing: " + ping
                + "\nIssued By: " + Adjust.username;
        ((DebugCommand) parent).printDebugData("Game debug", data);
    }
}
