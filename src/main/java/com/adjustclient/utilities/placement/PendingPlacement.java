package com.adjustclient.utilities.placement;
import net.minecraft.client.Minecraft;
public class PendingPlacement {
    public Object source;
    public String reason;
    public String detail;
    public double weight;
    public Object field24;
    public PendingPlacement(Object source, String reason, String detail, double weight) {
        this.source = source; this.reason = reason; this.detail = detail; this.weight = weight;
    }
    public void report() {
        var player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;
        var connection = player.sendQueue;
        if (connection == null || connection.getNetworkManager() == null
                || connection.getNetworkManager().sunshineAnticheat == null) return;
        com.adjustclient.utilities.sunshine.SunshineAnticheat anticheat =
                (com.adjustclient.utilities.sunshine.SunshineAnticheat) connection.getNetworkManager().sunshineAnticheat;
        if (anticheat.intave == null) return;
        anticheat.intave.recordViolation(new PlacementRecord(source, reason, detail, weight));
    }
}
