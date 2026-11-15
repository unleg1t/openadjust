package com.adjustclient.module.impl.player.voicechat.audio;

import com.adjustclient.audio.device.ChannelMode;
import com.adjustclient.audio.device.openal.OpenAlOutputDevice;
import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.module.impl.player.voicechat.playback.*;
import com.adjustclient.module.impl.player.voicechat.playback.realistic.RealisticAudioStream;
import com.adjustclient.util.MinecraftInstance;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.Vec3;

public final class SpeakerWorker extends AudioWorker implements MinecraftInstance {
    public final AudioStreamManager streams;
    public final VoiceChatController controller;
    public short[] samples;
    public final int frameSize;

    public SpeakerWorker(VoiceAudioManager manager, int frameSize) {
        super(manager);
        streams = VoiceChatModule.INSTANCE.getAudioStreamManager();
        controller = VoiceChatModule.INSTANCE.getController();
        this.frameSize = frameSize;
    }

    public void beginFrame(OpenAlOutputDevice output) {
        if (mc.thePlayer == null) { output.beginFrame(output.origin, 0.0f); return; }
        float eyeHeight = mc.thePlayer.getEyeHeight();
        Vec3 position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
        output.beginFrame(position, mc.thePlayer.rotationYaw);
    }

    public void submit(OpenAlOutputDevice output, AudioStream stream) {
        if (stream.closed) return;
        double volume = AudioStream.getVolume();
        Vec3 position = output.origin;
        if (mc.thePlayer != null && stream instanceof RealisticAudioStream realistic) {
            position = new Vec3(realistic.entity.posX, realistic.entity.posY, realistic.entity.posZ);
            Vec3 listener = mc.thePlayer.getPositionEyes(1.0f);
            if (!VoiceChatModule.INSTANCE.settings.stereoSound.isEnabled())
                position = listener.addVector(0.0, listener.distanceTo(position), 0.0);
        }
        output.submit(stream.playerId, ChannelMode.MONO, samples, position, 0, frameSize, volume);
    }

    @Override public void closeDevice() {
        super.closeDevice();
        OpenAlOutputDevice output = manager.outputDevice;
        if (output != null && output.device != null && output.device.handle != 0L
                && output.context != null && output.context.handle != 0L) output.close();
    }

    @Override public void processFrame() {
        if (!VoiceChatModule.INSTANCE.isEnabled() || streams.streams.isEmpty()) return;
        OpenAlOutputDevice output = manager.outputDevice;
        if (output == null || output.device == null || output.device.handle == 0L
                || output.context == null || output.context.handle == 0L) return;
        boolean testing = VoiceChatModule.INSTANCE.settings.micTester.isEnabled();
        if (!testing && controller.isCurrentChannelProximity() && mc.thePlayer == null) return;
        beginFrame(output);
        try {
            output.referenceDistance = VoiceChatModule.INSTANCE.settings.c() / 2.0f;
            output.rolloff = 3.4f;
            output.maxDistance = 100.0f;
            int length = frameSize * output.info.format.channels.channels;
            if (samples == null || samples.length != length) samples = new short[length];
            for (PlayerAudioStream stream : streams.streams.values()) {
                stream.drainPending();
                if (stream.availableSamples() < frameSize || !output.canSubmit(stream.playerId)) continue;
                stream.read(samples, 0, frameSize);
                if (stream.userInfo.isSelf() && !testing) continue;
                stream.filterChain.processInput(samples, 0, frameSize);
                submit(output, stream);
            }
            for (Map.Entry<UUID, PlayerAudioStream> entry : streams.streams.entrySet()) {
                PlayerAudioStream stream = entry.getValue();
                if (!stream.filterChain.hasTail() || stream.filterChain.isActive()) continue;
                UUID tailId = new UUID(entry.getKey().getMostSignificantBits(), entry.getKey().getLeastSignificantBits() + 1L);
                if (!output.canSubmit(tailId)) continue;
                Arrays.fill(samples, (short) 0);
                stream.filterChain.process(samples, 0, samples.length);
                output.submit(tailId, ChannelMode.STEREO, samples, output.origin, 0, frameSize * 2, AudioStream.getVolume());
            }
        } finally {
            output.endFrame();
        }
    }
}
