package com.adjustclient.module.impl.player.voicechat.protocol.transport.reliable;

@FunctionalInterface
public interface DatagramSender {
    void processCompleteFrame(byte[] data) throws Exception;
}
