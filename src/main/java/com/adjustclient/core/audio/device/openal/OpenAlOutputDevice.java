package com.adjustclient.core.audio.device.openal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.adjustclient.core.audio.device.*;
import com.adjustclient.core.audio.openal.*;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.joml.Vector3f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;

public class OpenAlOutputDevice extends AudioDevice {
    public DeviceInfo info;
    public Vec3 origin = new Vec3(0.0, 0.0, 0.0);
    public Map<UUID, AlStreamingSource> sources = new HashMap<>();
    public Map<UUID, Long> lastUsed = new HashMap<>();
    public AlPlaybackDevice device;
    public AlContext context;
    public Vec3 listenerPosition;
    public Vector3f listenerDirection;
    public float maxDistance;
    public float referenceDistance;
    public float rolloff;
    public int sampleRate;
    public int bufferSize;
    public Object field56;

    public OpenAlOutputDevice(DeviceInfo info) {
        this.info = info;
        sampleRate = info.format.sampleRate;
        int millis = System.getProperty("org.graalvm.nativeimage.imagecode") == null ? 20 : 15;
        bufferSize = 2 * info.format.channels.channels * (sampleRate / 1000) * millis;
    }
    public boolean canSubmit(UUID id) {
        AlStreamingSource stream = sources.get(id);
        if (stream == null) return true;
        int queued = AL10.alGetSourcei(stream.source.id, 4117);
        OpenAlErrors.checkAlError();
        return stream.buffers.ids.length > queued;
    }
    public void beginFrame(Vec3 position, float yaw) {
        context.makeCurrent();
        float angle = (float) ((yaw + 90.0) * 0.0174532925199433);
        listenerDirection = new Vector3f(MathHelper.cos(angle), 0.0f, MathHelper.sin(angle));
        listenerPosition = position;
        for (AlStreamingSource stream : sources.values()) stream.submitted = false;
    }
    public void submit(UUID id, ChannelMode channels, short[] samples, Vec3 position, int offset, int length, double volume) {
        AlStreamingSource stream = sources.computeIfAbsent(id,
            key -> {
                AlSource source = new AlSource(AL10.alGenSources());
                OpenAlErrors.checkAlError();
                source.disableLooping();
                AlSource.setDistanceModel(AlDistanceModel.INVERSE_CLAMPED);
                OpenAlErrors.checkAlError();
                int[] buffers = new int[32];
                AL10.alGenBuffers(buffers);
                OpenAlErrors.checkAlError();
                source.disableLooping();
                AlSource.setDistanceModel(AlDistanceModel.EXPONENT_CLAMPED);
                OpenAlErrors.checkAlError();
                source.setMinGain(0.0f);
                return new AlStreamingSource(channels, source, new AlBufferHandle(buffers), sampleRate, bufferSize, 1);
            });
        boolean valid = AL10.alIsSource(stream.source.id);
        OpenAlErrors.checkAlError();
        if (!valid) return;
        AL10.alSourcef(stream.source.id, 4129, rolloff);
        OpenAlErrors.checkAlError();
        stream.source.setMaxDistance(maxDistance);
        AL10.alSourcef(stream.source.id, 4128, referenceDistance);
        OpenAlErrors.checkAlError();
        stream.source.setGain((float) volume * 4.0f);
        stream.source.setMaxGain((float) volume * 4.0f);
        stream.setListenerOrientation(listenerDirection);
        AlSource.setListenerPosition((float) listenerPosition.xCoord, (float) listenerPosition.yCoord, (float) listenerPosition.zCoord);
        stream.source.setPosition((float) position.xCoord, (float) position.yCoord, (float) position.zCoord);
        stream.submit(channels, samples, 0, length);
        touch(id);
    }
    public void touch(UUID id) {
        lastUsed.put(id, System.currentTimeMillis());
        lastUsed.entrySet().removeIf(
            entry -> {
                if (System.currentTimeMillis() - entry.getValue() < 300001L) return false;
                AlStreamingSource stream = sources.remove(entry.getKey());
                if (stream != null) {
                    boolean valid = AL10.alIsSource(stream.source.id);
                    OpenAlErrors.checkAlError();
                    if (valid) stream.delete();
                }
                return true;
            });
    }
    public void close() {
        if (device != null && device.handle != 0L && context != null && context.handle != 0L) {
            if (context.makeCurrent()) {
                try {
                    for (AlStreamingSource stream : sources.values()) {
                        boolean valid = AL10.alIsSource(stream.source.id);
                        OpenAlErrors.checkAlError();
                        if (valid) stream.delete();
                    }
                } finally {
                    context.clearCurrent();
                }
            }
            sources.clear();
            lastUsed.clear();
            ALC10.alcDestroyContext(context.handle);
            OpenAlErrors.checkAlcError(context.device.handle);
            context.handle = 0L;
            context.releaseCapabilities();
            ALC10.alcCloseDevice(device.handle);
            OpenAlErrors.checkAlcError(device.handle);
        }
        device = null;
    }
    public void endFrame() {
        try {
            for (AlStreamingSource stream : sources.values()) {
                boolean valid = AL10.alIsSource(stream.source.id);
                OpenAlErrors.checkAlError();
                if (!valid || stream.submitted) continue;
                stream.unqueueProcessed();
                AlSourceState state = stream.source.getState();
                int queued = AL10.alGetSourcei(stream.source.id, 4117);
                OpenAlErrors.checkAlError();
                if (state == AlSourceState.INITIAL || state == AlSourceState.PAUSED || state == AlSourceState.STOPPED || queued < 1) {
                    stream.prefill();
                    stream.source.play();
                }
            }
        } finally {
            context.clearCurrent();
        }
    }
    public void open(AudioFormat format) throws AudioDeviceException {
        if (device != null && device.handle != 0L && context != null && context.handle != 0L)
            throw new AudioDeviceException("Device is already open");
        AlContext.ensureAlcLoaded();
        device = new AlPlaybackDevice();
        if (!device.open(info.identifier.name)) {
            Logger.log(LogLevel.ERROR, true, "Failed to find target device {}, using default", info.identifier);
            if (!device.open(AlPlaybackDevice.defaultName()))
                throw new AudioDeviceException("Couldn't find any device with the specified format: " + format);
        }
        context = new AlContext();
        context.device = device;
        context.handle = ALC10.alcCreateContext(device.handle, (java.nio.IntBuffer) null);
        OpenAlErrors.checkAlcError(device.handle);
        if (context.handle == 0L) {
            long deviceHandle = device.handle;
            context = null;
            device = null;
            ALC10.alcCloseDevice(deviceHandle);
            OpenAlErrors.checkAlcError(deviceHandle);
            throw new AudioDeviceException("Failed to create OpenAL context for format: " + format);
        }
    }
}
