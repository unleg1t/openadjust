package com.adjustclient.service.util;

import com.adjustclient.Adjust;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;

public final class ClientInfoCollector {
    public static ArrayList<ClientInfoEntry> entries;
    public static int sequence;
    public static void collect() {
        int current = ++sequence;
        if (entries == null) entries = new ArrayList<ClientInfoEntry>();
        long now = System.currentTimeMillis();
        entries.add(new ClientInfoEntry((ClientInfoSource) null, now, current));
    }

    @SuppressWarnings("unused")
    private static void vendorCollect() {
        int current = ++sequence;
        if (entries == null) entries = new ArrayList<ClientInfoEntry>();
        long now = System.currentTimeMillis();
        if (now < 0x18A274D3264L || now >= 0x1BE7D98D4F3L || now > System.currentTimeMillis()) {
            Minecraft.getMinecraft().renderGlobal = null;
        }
        entries.add(new ClientInfoEntry(
                com.adjustclient.backend.transport.HydraMessageDispatcher.clientInfoSourceToken, now, current));
        if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated) {
            byte[] packet = new byte[8];
            packet[0] = 15;
            packet[4] = (byte) (current >>> 24);
            packet[5] = (byte) (current >>> 16);
            packet[6] = (byte) (current >>> 8);
            packet[7] = (byte) current;
            Adjust.hydraConnection.send(packet);
        }
    }
}
