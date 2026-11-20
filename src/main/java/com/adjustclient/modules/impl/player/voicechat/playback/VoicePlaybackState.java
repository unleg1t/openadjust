package com.adjustclient.modules.impl.player.voicechat.playback;

public enum VoicePlaybackState {
    NOT_TALKING(true), TALKING(true), OUTPUT_DISABLED(false), OUTPUT_MUTED(false),
    INPUT_DISABLED(true), INPUT_MUTED(true), INPUT_GLOBAL_MUTED(true),
    NETWORK_ERROR(false), IN_ANOTHER_CHANNEL(false);

    VoicePlaybackState(boolean receiveAllowed) { this.receiveAllowed = receiveAllowed; }

    public boolean receiveAllowed;
}
