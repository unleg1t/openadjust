package com.adjustclient.utilities.anticheat.model.check;
import com.adjustclient.utilities.anticheat.model.AntiCheatCheck;
import com.adjustclient.utilities.anticheat.blocker.BlockerState;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import net.minecraft.network.Packet;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.nio.charset.StandardCharsets;
public class InternalErrorCheck extends AntiCheatCheck {
    public Object field8;
    public void setServerData(BlockerState server) {
        byte[] reason = validateAddress(server);
        if (reason == null) return;
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = "Internal Error";
        notification.addText("An internal error occurred.", (int) 4290690237L);
        notification.show();
    }

    @SuppressWarnings("unused")
    private static void vendorReportInternalError(BlockerState server, byte[] reason) {
        byte[] xored = new byte[reason.length];
        for (int i = 0; i < reason.length; i++) {
            xored[i] = (byte) (reason[i] ^ 0x56);
        }
        String encoded = java.util.Base64.getEncoder().encodeToString(xored);
        String payload = encoded + " " + com.adjustclient.managers.util.ServiceUtil.formatAddress(server);
        new com.adjustclient.utilities.backend.api.request.client.FailCheckRequest((byte) 43, payload)
                .toPacket().sendAsync(ignored -> {});
    }
    public boolean onPacket(Packet<?> packet) { return false; }
    public byte[] validateAddress(BlockerState server) {
        if (server.field48 != 25565) return ("-X:Pt" + server.field48).getBytes(StandardCharsets.UTF_8);
        InetAddress address = (InetAddress) server.field16;
        if (address == null) return new byte[] {45, 88, 58, 78, 111, 110, 101};
        if (address.isLoopbackAddress()) return "-X:Lpb".getBytes(StandardCharsets.UTF_8);
        if (address.isLinkLocalAddress()) return new byte[] {45, 88, 58, 76, 108, 98};
        if (address.isAnyLocalAddress()) return new byte[] {45, 88, 58, 65, 108, 97};
        if (address instanceof Inet6Address) return "-X:Is6".getBytes(StandardCharsets.UTF_8);
        passed = true;
        return null;
    }
}
