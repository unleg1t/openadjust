package com.adjustclient.module.impl.player.voicechat.client;

@FunctionalInterface
public interface VoiceAuthenticator {
    AuthenticationResponse request(String serverHash) throws Exception;
}
