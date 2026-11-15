package com.adjustclient.module.impl.player.voicechat.protocol.transport;

@FunctionalInterface
public interface PacketReceiver {
    void handle(java.net.DatagramSocket socket, byte[] data, int offset, int length) throws Exception;
}
