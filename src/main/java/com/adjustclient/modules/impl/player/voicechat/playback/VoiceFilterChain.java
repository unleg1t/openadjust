package com.adjustclient.modules.impl.player.voicechat.playback;

import com.adjustclient.core.audio.filter.chain.AudioFilterChain;
import com.adjustclient.core.audio.filter.impl.*;
import com.adjustclient.modules.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.modules.impl.player.voicechat.client.user.VoiceUserInfo;

public class VoiceFilterChain extends AudioFilterChain {
    public final VoiceUserInfo userInfo;
    public final AutomaticGainFilter automaticGain = new AutomaticGainFilter();
    public final MuffleFilter muffle = new MuffleFilter();
    public final PassThroughFilter passThrough = new PassThroughFilter();
    public final CompressorFilter compressor = new CompressorFilter();
    public final NoiseFilter noise = new NoiseFilter();
    public final ToneTestFilter toneTest = new ToneTestFilter();

    public VoiceFilterChain(VoiceUserInfo userInfo) {
        this.userInfo = userInfo;
        filters.add(muffle);
        filters.add(passThrough);
        filters.add(compressor);
        filters.add(noise);
        filters.add(toneTest);
        filters.add(automaticGain);
    }

    public void update() {
        automaticGain.enabled = VoiceChatModule.INSTANCE.settings.volumeCompressor.getAsBoolean();
        automaticGain.targetLevel = VoiceChatModule.INSTANCE.settings.speakerVolume.getFloat() / 100.0f;
        if (muffle.strength != 0.0f) {
            muffle.previousStrength = muffle.strength;
            muffle.strength = 0.0f;
            muffle.fadeStart = System.currentTimeMillis();
            muffle.fadeDuration = 0L;
        }
        passThrough.enabled = false;
        compressor.enabled = false;
        noise.enabled = false;
        toneTest.enabled = false;
    }
}
