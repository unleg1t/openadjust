package com.adjustclient.module.impl.player.voicechat.protocol.transport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;

public final class VoiceReceiveThread extends Thread {
    public volatile boolean running = true;
    public final DatagramSocket socket;
    public final PacketReceiver packetReceiver;
    public final CloseHandler closeHandler;

    public VoiceReceiveThread(DatagramSocket socket, PacketReceiver packetReceiver, CloseHandler closeHandler) {
        this.socket = socket;
        this.packetReceiver = packetReceiver;
        this.closeHandler = closeHandler;
    }

    public void receive(byte[] buffer) {
        try {
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
            socket.receive(datagram);
            if (packetReceiver != null) {
                try {
                    packetReceiver.handle(socket, datagram.getData(), datagram.getOffset(), datagram.getLength());
                } catch (Exception exception) {
                    if (closeHandler != null) closeHandler.handle(socket, exception);
                }
            }
        } catch (Exception exception) {
            if (exception instanceof IOException) running = false;
        }
    }

    @Override public void run() {
        byte[] buffer = new byte[1024];
        while (running) receive(buffer);
    }
}
