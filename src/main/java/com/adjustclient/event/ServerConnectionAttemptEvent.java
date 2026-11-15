package com.adjustclient.event;

public final class ServerConnectionAttemptEvent extends Event {
    public String host;
    public java.net.InetAddress address;
    public net.minecraft.client.multiplayer.ServerData serverIdentity;
    public int port;

    public ServerConnectionAttemptEvent(String host, java.net.InetAddress address, net.minecraft.client.multiplayer.ServerData serverIdentity, int port) {
        this.host = host;
        this.address = address;
        this.serverIdentity = serverIdentity;
        this.port = port;
    }

    // Minecraft.launchIntegratedServer.
    public ServerConnectionAttemptEvent(String host) {
        this(host, null, null, 0);
    }
}
