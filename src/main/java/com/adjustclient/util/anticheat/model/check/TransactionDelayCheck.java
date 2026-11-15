package com.adjustclient.util.anticheat.model.check;
import com.adjustclient.util.anticheat.model.AntiCheatCheck;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
public class TransactionDelayCheck extends AntiCheatCheck {
    public boolean field6;
    public BlockerState server;
    public Object field12;
    public void setServerData(BlockerState server) { this.server = server; }
    public boolean onPacket(Packet<?> packet) {
        if (!(packet instanceof S01PacketJoinGame) || field6) return false;
        field6 = true;
        if (server == null) {
            return false;
        }
        if (server.field40 < 9L) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private static void vendorFailTransactionDelay(BlockerState server) {
        String details;
        if (server == null) {
            details = "no server";
        } else {
            String prefix = server.field40 == -1L ? "no login" : "low delay: " + server.field40;
            details = prefix + " " + com.adjustclient.service.util.ServiceUtil.formatAddress(server);
        }
        new com.adjustclient.backend.api.request.client.FailCheckRequest((byte) 77, details)
                .toPacket().sendAsync(null);
    }
}
