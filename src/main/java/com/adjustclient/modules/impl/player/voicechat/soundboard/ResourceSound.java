package com.adjustclient.modules.impl.player.voicechat.soundboard;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ResourceSound implements SoundboardSound {
    public final String name;
    public final short[] samples;
    public int position;

    public ResourceSound(String name, byte[] data) {
        this.name = name;
        ByteBuffer encoded = MemoryUtil.memAlloc(data.length);
        encoded.put(data).flip();
        short[] decoded;
        int rate;
        int channels;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer error = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(encoded, error, null);
            if (decoder == 0) throw new IllegalStateException("Failed to open Vorbis: error=" + error.get(0));
            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            STBVorbis.stb_vorbis_get_info(decoder, info);
            rate = info.sample_rate();
            channels = info.channels();
            ShortBuffer buffer = MemoryUtil.memAllocShort(channels * STBVorbis.stb_vorbis_stream_length_in_samples(decoder));
            STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, buffer);
            STBVorbis.stb_vorbis_close(decoder);
            decoded = new short[buffer.remaining()];
            buffer.get(decoded);
            MemoryUtil.memFree(buffer);
        }
        MemoryUtil.memFree(encoded);
        short[] mono = decoded;
        if (channels == 2) {
            mono = new short[decoded.length / 2];
            for (int i = 0; i < mono.length; i++) mono[i] = (short) ((decoded[2 * i] + decoded[2 * i + 1]) / 2);
        }
        if (rate != 48000) {
            double ratio = 48000.0 / rate;
            samples = new short[(int) (mono.length * ratio)];
            for (int i = 0; i < samples.length; i++) {
                double index = i / ratio;
                int left = (int) Math.floor(index);
                int right = Math.min(left + 1, mono.length - 1);
                double fraction = index - left;
                samples[i] = (short) ((1.0 - fraction) * mono[left] + mono[right] * fraction);
            }
        } else samples = mono;
    }

    public String getName() { return name; }

    public static ResourceSound load(ResourceLocation resource) throws IOException {
        try (InputStream input = Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream()) {
            String[] parts = resource.getResourcePath().split("/");
            return new ResourceSound(parts[parts.length - 1], input.readAllBytes());
        }
    }

    public short[] nextFrame() {
        short[] frame = new short[960];
        int length = Math.min(960, samples.length - position);
        System.arraycopy(samples, position, frame, 0, length);
        position += length;
        return frame;
    }
    public boolean hasRemaining() { return samples.length - position >= 1; }
}
