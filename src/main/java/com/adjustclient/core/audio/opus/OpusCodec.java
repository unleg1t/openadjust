package com.adjustclient.core.audio.opus;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.opus.Opus;
public class OpusCodec implements AutoCloseable {
    static {
        ensureNativeLoaded();
    }

    private static void ensureNativeLoaded() {
        try {
            String configured = Configuration.OPUS_LIBRARY_NAME.get();
            if (configured != null && !configured.isEmpty() && new File(configured).isFile()) {
                return;
            }
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (!os.contains("win")) {
                return;
            }
            try (InputStream in = OpusCodec.class.getResourceAsStream("/assets/adjust/natives/opus.dll")) {
                if (in == null) {
                    return;
                }
                Path temp = Files.createTempFile("adjust-opus-", ".dll");
                temp.toFile().deleteOnExit();
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                Configuration.OPUS_LIBRARY_NAME.set(temp.toAbsolutePath().toString());
            }
        } catch (Throwable ignored) {
        }
    }

    public OpusDecoderConfig config;
    public long encoder;
    public long decoder;
    public Object field24;

    public OpusCodec(OpusDecoderConfig config) {
        try (org.lwjgl.system.MemoryStack stack = org.lwjgl.system.MemoryStack.stackPush()) {
            java.nio.IntBuffer error = stack.mallocInt(1);
            encoder = Opus.opus_encoder_create(config.sampleRate, config.channels, 2049, error);
            checkError(error.get(0));
            decoder = Opus.opus_decoder_create(config.sampleRate, config.channels, error);
            checkError(error.get(0));
            Opus.opus_encoder_ctl(encoder, Opus.OPUS_SET_BITRATE(config.bitrate));
        }
        this.config = config;
    }
    public byte[] encode(byte[] samples) {
        ShortBuffer pcm = MemoryUtil.memAllocShort(samples.length / 2);
        ByteBuffer output = null;
        try {
            for (int i = 0; i < samples.length; i += 2) pcm.put((short) ((samples[i] & 255) | (samples[i + 1] << 8)));
            pcm.flip();
            output = MemoryUtil.memAlloc(config.maxPacketSize);
            int size = Opus.opus_encode(encoder, pcm, config.frameSize, output);
            if (size < 0) throw new IllegalStateException("Opus encode failed: " + Opus.opus_strerror(size));
            byte[] result = new byte[size];
            output.get(result);
            return result;
        } finally {
            MemoryUtil.memFree(pcm);
            if (output != null) MemoryUtil.memFree(output);
        }
    }
    public byte[] decode(byte[] data) {
        ByteBuffer input = MemoryUtil.memAlloc(data.length);
        ShortBuffer pcm = null;
        try {
            input.put(data).flip();
            pcm = MemoryUtil.memAllocShort(config.channels * config.maxFrameSize);
            int size = Opus.opus_decode(decoder, input, pcm, config.maxFrameSize, 0);
            if (size < 0) throw new IllegalStateException("Opus decode failed: " + Opus.opus_strerror(size));
            byte[] result = new byte[2 * config.channels * size];
            for (int i = 0; i < config.channels * size; i++) {
                short sample = pcm.get(i);
                result[2 * i] = (byte) sample;
                result[2 * i + 1] = (byte) (sample >>> 8);
            }
            return result;
        } finally {
            MemoryUtil.memFree(input);
            if (pcm != null) MemoryUtil.memFree(pcm);
        }
    }
    public void close() {
        if (encoder != 0L) { Opus.opus_encoder_destroy(encoder); encoder = 0L; }
        if (decoder != 0L) { Opus.opus_decoder_destroy(decoder); decoder = 0L; }
    }
    public static void checkError(int error) {
        if (error != 0) throw new IllegalStateException("Opus error: " + Opus.opus_strerror(error));
    }
}
