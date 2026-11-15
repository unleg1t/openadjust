package com.adjustclient.module.impl.player.voicechat.protocol.transport;

@FunctionalInterface
public interface CloseHandler {
    void handle(Object source, Throwable error);
}
