package com.adjustclient.module.impl.player.voicechat.audio;

import com.adjustclient.audio.AudioUtil;
import com.adjustclient.audio.Denoiser;
import com.adjustclient.audio.opus.*;
import com.adjustclient.logging.*;
import com.adjustclient.module.impl.player.voicechat.*;
import com.adjustclient.module.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.module.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.module.impl.player.voicechat.client.*;
import com.adjustclient.module.impl.player.voicechat.playback.AudioStreamManager;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import java.nio.ByteBuffer;
import net.minecraft.util.Session;

public final class MicrophoneWorker extends AudioWorker implements MinecraftInstance {
    public final VoiceChatController controller;
    public final VoiceSessionManager sessionManager;
    public final VoiceSender sender;
    public final AudioStreamManager streams;
    public OpusEncoderHandle encoder;
    public Denoiser denoiser;
    public short[] samples;
    public short[] previousSamples;
    public final Timer soundboardTimer = new Timer(0L);
    public final int frameSize;
    public long lastVoiceTime;
    public int sequence;
    public boolean previousActive;

    public MicrophoneWorker(VoiceAudioManager manager, int frameSize) {
        super(manager);
        this.frameSize = frameSize;
        controller = VoiceChatModule.INSTANCE.getController();
        sessionManager = VoiceChatModule.INSTANCE.getSessionManager();
        sender = sessionManager.sender;
        streams = VoiceChatModule.INSTANCE.getAudioStreamManager();
    }

    public void sendFrame(byte[] packet, int sequence) {
        Session session = VoiceChatModule.INSTANCE.getSessionManager().session;
        if (session == null) session = mc.getSession();
        streams.accept(session.getProfile().getId(), packet, (long) sequence);
        if (sessionManager.client.udpClient == null
                || !sessionManager.client.udpClient.open.get()
                || !sessionManager.handler.authenticated
                || VoiceChatModule.INSTANCE.settings.micTester.isEnabled()) return;
        ByteBuffer buffer = ByteBuffer.allocate(packet.length + 8);
        buffer.putLong((long) sequence).put(packet).flip();
        sender.sendAudio(buffer.array());
    }

    @Override public void closeDevice() {
        super.closeDevice();
        try {
            if (manager.captureDevice != null && manager.captureDevice.isOpen()) {
                org.lwjgl.openal.ALC11.alcCaptureStop(manager.captureDevice.device.handle);
                com.adjustclient.audio.openal.OpenAlErrors.checkAlcError(manager.captureDevice.device.handle);
                manager.captureDevice.device.started = false;
                manager.captureDevice.device.close();
            }
            if (isEncoderOpen()) encoder.close();
            if (denoiser != null) {
                synchronized (denoiser) {
                    if (denoiser.denoiser != null) denoiser.close();
                }
            }
        } catch (Exception ignored) { }
    }

    @Override public void processFrame() {
        VoiceChatModule module = VoiceChatModule.INSTANCE;
        if (!module.isEnabled()) { sequence = 0; return; }
        boolean reset = true;
        boolean captured = false;
        if (manager.captureDevice != null && manager.captureDevice.isOpen()) {
            if (!manager.captureDevice.device.started) manager.captureDevice.start();
            if (manager.captureDevice.availableSamples() < frameSize) reset = false;
            else {
                if (samples == null || samples.length != frameSize) {
                    samples = new short[frameSize]; previousSamples = new short[frameSize];
                }
                manager.captureDevice.read(samples, frameSize);
                float volume = module.settings.i();
                double decibels = volume == 0.0f ? -127.0 : Math.max(-127.0, Math.min(32.0, (volume - 1.0) * 30.0));
                if (volume != 1.0f) {
                    double gain = Math.pow(10.0, decibels / (decibels > 1.0 ? 20.0 : 10.0));
                    for (int i = 0; i < samples.length; i++) samples[i] = (short) (int) (samples[i] * gain);
                }
                if (denoiser != null) {
                    try {
                        synchronized (denoiser) {
                            if (denoiser.denoiser != null && module.settings.noiseSuppression.isEnabled()) denoiser.denoise(samples);
                        }
                    } catch (Exception ignored) { denoiser.close(); }
                }
                reset = false; captured = true;
            }
        }
        boolean soundboard = false;
        int interval = System.getProperty("org.graalvm.nativeimage.imagecode") == null ? 20 : 15;
        if (soundboardTimer.hasElapsed(interval)) {
            if (module.soundboard == null) module.soundboard = new com.adjustclient.module.impl.player.voicechat.soundboard.Soundboard();
            short[] pcm = module.soundboard.nextFrame();
            if (pcm != null) {
                if (samples == null || samples.length != frameSize) {
                    samples = new short[frameSize]; previousSamples = new short[frameSize];
                }
                if (pcm.length != frameSize) throw new IllegalStateException("Soundboard PCM data must contain " + frameSize + " frame samples");
                for (int i = 0; i < samples.length; i++) samples[i] = (short) Math.max(-32768, Math.min(32767, samples[i] + pcm[i]));
                soundboard = true; captured = true; reset = false;
            }
            soundboardTimer.reset();
        }
        if (captured && (module.settings.micTester.isEnabled() || shouldTransmit() || soundboard) && isEncoderOpen()) {
            if (sequence < 1) sequence = 0;
            byte[] encoded = encoder.encode(samples);
            sendFrame(encoded, sequence++);
        }
        if (reset) sequence = 0;
    }

    public boolean shouldTransmit() {
        Channel channel = controller.getCurrentChannel();
        Session session = sessionManager.session;
        if (session != null && channel != null && !channel.properties.canTalk(session.getProfile().getId())) return true;
        return switch (VoiceChatModule.INSTANCE.settings.inputActivation.getValue()) {
            case PUSH -> { previousActive = false; yield VoiceChatModule.INSTANCE.settings.pushToTalk.isDown(); }
            case DETECT -> {
                if (detectVoice()) {
                    boolean active = previousActive;
                    AudioUtil.swap(samples, previousSamples, 0, samples.length);
                    previousActive = true;
                    yield active;
                }
                System.arraycopy(samples, 0, previousSamples, 0, samples.length);
                previousActive = true;
                yield false;
            }
            case ALWAYS -> { previousActive = false; yield true; }
        };
    }

    public boolean detectVoice() {
        boolean detected = false;
        for (int start = 0; start < samples.length; start += 100) {
            int end = Math.min(start + 100, samples.length);
            if (AudioUtil.level(samples, start, end) >= -50.0) { detected = true; break; }
        }
        long now = System.nanoTime();
        if (detected) { lastVoiceTime = now; return true; }
        return now - lastVoiceTime < 501L;
    }

    public void initialize() {
        try {
            denoiser = new Denoiser();
            denoiser.denoiser = new de.maxhenkel.rnnoise4j.Denoiser();
        } catch (java.io.IOException | de.maxhenkel.rnnoise4j.UnknownPlatformException exception) {
            Logger.log(LogLevel.ERROR, true, "Couldn't create denoiser {}", exception);
        }
        OpusDecoderConfig config = new OpusDecoderConfig();
        config.frameSize = 960; config.sampleRate = 48000; config.channels = 1;
        config.bitrate = 48000; config.maxFrameSize = 57600; config.maxPacketSize = 38280;
        OpusCodec codec = new OpusCodec(config);
        encoder = new OpusEncoderHandle();
        encoder.codec = codec;
        synchronized (this) {
            if (loopFuture != null && !loopFuture.isDone() && isEncoderOpen()) return;
            if (loopFuture != null) loopFuture.cancel(true);
            loopFuture = executor.submit(() -> runLoop());
        }
    }

    public boolean isEncoderOpen() {
        if (encoder == null) return false;
        synchronized (encoder) { return encoder.codec != null; }
    }
}
