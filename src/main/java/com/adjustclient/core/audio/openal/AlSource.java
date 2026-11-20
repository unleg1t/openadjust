package com.adjustclient.core.audio.openal;

import org.lwjgl.openal.AL10;

public final class AlSource {
    public int id;
    public Object field8;
    public AlSource(int id) { this.id = id; }
    public void disableLooping() { AL10.alSourcei(id, 4103, 0); OpenAlErrors.checkAlError(); }
    public void setSampleOffset(int offset) { AL10.alSourcei(id, 4133, offset); OpenAlErrors.checkAlError(); }
    public void setGain(float gain) { AL10.alSourcef(id, 4106, gain); OpenAlErrors.checkAlError(); }
    public static void setDistanceModel(AlDistanceModel model) { AL10.alDistanceModel(model.value); OpenAlErrors.checkAlError(); }
    public void queueBuffer(AlBufferHandle buffers, int index) { AL10.alSourceQueueBuffers(id, buffers.ids[index]); OpenAlErrors.checkAlError(); }
    public void setPosition(float x, float y, float z) { AL10.alSource3f(id, 4100, x, y, z); OpenAlErrors.checkAlError(); }
    public void play() { AL10.alSourcePlay(id); OpenAlErrors.checkAlError(); }
    public void setMaxGain(float gain) { AL10.alSourcef(id, 4110, gain); OpenAlErrors.checkAlError(); }
    public void setMinGain(float gain) { AL10.alSourcef(id, 4109, gain); OpenAlErrors.checkAlError(); }
    public void stop() { AL10.alSourceStop(id); OpenAlErrors.checkAlError(); }
    public static void setListenerPosition(float x, float y, float z) { AL10.alListener3f(4100, x, y, z); OpenAlErrors.checkAlError(); }
    public void setMaxDistance(float distance) { AL10.alSourcef(id, 4131, distance); OpenAlErrors.checkAlError(); }
    public AlSourceState getState() {
        int state = AL10.alGetSourcei(id, 4112);
        OpenAlErrors.checkAlError();
        for (AlSourceState value : AlSourceState.values()) if (value.value == state) return value;
        return null;
    }
    public int getSampleOffset() { int result = AL10.alGetSourcei(id, 4133); OpenAlErrors.checkAlError(); return result; }
    public void unqueueBuffer() { AL10.alSourceUnqueueBuffers(id); OpenAlErrors.checkAlError(); }
    public void delete() {
        if (getState() == AlSourceState.PLAYING) stop();
        AL10.alDeleteSources(id);
        OpenAlErrors.checkAlError();
    }
}

