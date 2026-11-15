package com.adjustclient.audio.openal;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.EXTThreadLocalContext;
public final class AlContext {
    public AlDevice device;
    public long handle;
    public Object field16;
    public volatile ALCapabilities capabilities;
    private static final Object ALC_LOCK = new Object();

    public boolean makeCurrent() {
        boolean result = EXTThreadLocalContext.alcSetThreadContext(handle);
        OpenAlErrors.checkAlcError(device.handle);
        if (result) bindCapabilities();
        return result;
    }
    public void clearCurrent() {
        AL.setCurrentThread(null);
        EXTThreadLocalContext.alcSetThreadContext(0L);
        OpenAlErrors.checkAlcError(device.handle);
    }

    private void bindCapabilities() {
        ALCapabilities caps = capabilities;
        if (caps == null) {
            synchronized (this) {
                caps = capabilities;
                if (caps == null) {
                    ALCCapabilities alcCaps = ALC.createCapabilities(device.handle);
                    caps = AL.createCapabilities(alcCaps);
                    capabilities = caps;
                }
            }
        }
        AL.setCurrentThread(caps);
    }

    public void releaseCapabilities() {
        capabilities = null;
    }

    public static void ensureAlcLoaded() {
        synchronized (ALC_LOCK) {
            try {
                ALC.getFunctionProvider();
            } catch (IllegalStateException notLoaded) {
                try {
                    ALC.create();
                } catch (IllegalStateException alreadyCreated) {
                    // created concurrently by the sound system
                }
            }
        }
    }
}
