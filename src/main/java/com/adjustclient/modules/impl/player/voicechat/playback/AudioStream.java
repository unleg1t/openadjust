package com.adjustclient.modules.impl.player.voicechat.playback;

import com.adjustclient.core.audio.filter.AudioFilter;
import com.adjustclient.core.audio.opus.OpusCodec;
import com.adjustclient.core.audio.opus.OpusDecoderConfig;
import com.adjustclient.core.audio.opus.OpusDecoderHandle;
import com.adjustclient.modules.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.utilities.MinecraftInstance;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class AudioStream implements MinecraftInstance {
    public final ConcurrentLinkedDeque<Runnable> pending = new ConcurrentLinkedDeque<>();
    public final PriorityQueue<DecodedFrame> frames = new PriorityQueue<>(11,
        (left, right) -> {
            if (left == null || right == null) return 0;
            return Long.compare(left.sequence, right.sequence);
        });
    public final AudioLevelMeter levelMeter = new AudioLevelMeter();
    public java.util.UUID playerId;
    public OpusDecoderHandle decoder;
    public DecodedFrame lastFrame;
    public VoiceFilterChain filterChain;
    public long lastPacketTime;
    public long field40;
    public long receivedBytes;
    public int field56;
    public int receivedPackets;
    public boolean closed;
    public boolean talking;

    public void initialize() {
        OpusDecoderConfig config = new OpusDecoderConfig();
        config.frameSize = 960;
        config.sampleRate = 48000;
        config.channels = 1;
        config.bitrate = 48000;
        config.maxFrameSize = 57600;
        config.maxPacketSize = 38280;
        decoder = new OpusDecoderHandle();
        try {
            decoder.codec = new OpusCodec(config);
        } catch (Throwable exception) {
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true, "Couldn't create Opus decoder {}", exception);
            decoder = null;
        }
        PlayerAudioStream playerStream = (PlayerAudioStream) this;
        if (this instanceof com.adjustclient.modules.impl.player.voicechat.playback.realistic.RealisticAudioStream) {
            com.adjustclient.modules.impl.player.voicechat.playback.realistic.RealisticAudioStream realistic =
                (com.adjustclient.modules.impl.player.voicechat.playback.realistic.RealisticAudioStream) this;
            filterChain = new com.adjustclient.modules.impl.player.voicechat.playback.realistic.RealisticSoundProcessor(realistic.entity, playerStream.userInfo);
        } else {
            filterChain = new VoiceFilterChain(playerStream.userInfo);
        }
        lastPacketTime = System.currentTimeMillis();
    }

    public void accept(byte[] packet, long sequence) {
        if (frames.size() >= 16 || packet.length == 0 || decoder == null || closed) return;
        short[] samples;
        synchronized (decoder) {
            if (decoder.codec == null) throw new IllegalStateException("Decoder is closed");
            byte[] decoded = decoder.codec.decode(packet);
            samples = new short[decoded.length / 2];
            for (int i = 0; i < samples.length; i++) {
                samples[i] = (short) ((decoded[i * 2] & 255) | (decoded[i * 2 + 1] << 8));
            }
        }
        DecodedFrame frame = new DecodedFrame(samples, sequence);
        pending.add(() -> frames.add(frame));
        mc.addScheduledTask(() -> {
            int length = frame.samples.length;
            if (levelMeter.levels.length != length * 2) levelMeter.levels = new float[length * 2];
            if (length == 0) return;
            boolean oldHalf = levelMeter.secondHalf;
            int offset = oldHalf ? length : 0;
            float peak = Math.abs((int) frame.samples[0]);
            levelMeter.levels[offset] = frame.samples[0] / 32768.0f;
            for (int i = 2; i < length; i += 2) {
                short sample = frame.samples[i];
                levelMeter.levels[offset + (i >> 1)] = sample / 32768.0f;
                peak = Math.max(peak, Math.abs((int) sample));
            }
            float scale = 2000.0f / peak;
            if (scale < 1.0f) {
                for (int i = 0; i < length; i++) levelMeter.levels[offset + i] *= scale;
            }
            levelMeter.secondHalf = !oldHalf;
            if (!oldHalf) levelMeter.timer.reset();
        });
        lastFrame = frame;
        lastPacketTime = System.currentTimeMillis();
    }

    public void read(short[] destination, int offset, int length) {
        DecodedFrame frame = frames.peek();
        if (frame == null) throw new IllegalStateException("No frame available");
        if (length > frame.samples.length - frame.readOffset) throw new IllegalStateException("Not enough samples to read");
        System.arraycopy(frame.samples, frame.readOffset, destination, 0, length);
        frame.readOffset += length;
        if (frame.readOffset == frame.samples.length) frames.remove(frame);
    }

    public int availableSamples() {
        int count = 0;
        for (DecodedFrame frame : frames) count += frame.samples.length - frame.readOffset;
        return count;
    }

    public void drainPending() {
        while (!pending.isEmpty()) pending.poll().run();
    }

    public void update() {
        if (filterChain != null) filterChain.update();
        boolean active = isTalking();
        if (talking != active) talking = active;
    }

    public boolean isTalking() { return lastPacketTime + 100L > System.currentTimeMillis(); }

    public boolean isExpired() {
        return System.currentTimeMillis() > lastPacketTime + 3000L && (filterChain == null || filterChain.isActive());
    }

    public void close() {
        frames.clear();
        pending.clear();
        if (filterChain != null) {
            for (AudioFilter filter : filterChain.filters) filter.reset();
        }
        lastFrame = null;
        closed = true;
        if (decoder != null) {
            boolean open;
            synchronized (decoder) { open = decoder.codec != null; }
            if (open) decoder.close();
        }
    }

    public static double getVolume() {
        float volume = VoiceChatModule.INSTANCE.settings.speakerVolume.getFloat() / 100.0f;
        double decibels = volume == 0.0f ? -127.0 : Math.max(-127.0, Math.min(32.0, (volume - 1.0) * 30.0));
        return Math.pow(10.0, decibels / (decibels > 1.0 ? 20.0 : 10.0));
    }
}
