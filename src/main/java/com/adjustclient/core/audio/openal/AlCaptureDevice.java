package com.adjustclient.core.audio.openal;

import java.util.Collections;
import java.util.List;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;
import com.adjustclient.core.audio.device.AudioFormat;
public final class AlCaptureDevice extends AlDevice {
    public boolean started;
    public Object field16;
    public int availableSamples() {
        int result = ALC10.alcGetInteger(handle, 786);
        OpenAlErrors.checkAlcError(handle);
        return result;
    }
    public void close() {
        ALC11.alcCaptureCloseDevice(handle);
        OpenAlErrors.checkAlcError(handle);
        handle = 0L;
    }
    public boolean open(String name, AudioFormat format, int bufferSize) {
        AlContext.ensureAlcLoaded();
        handle = ALC11.alcCaptureOpenDevice(name, format.sampleRate, 65552, bufferSize);
        OpenAlErrors.checkAlcError(handle);
        return handle != 0L;
    }
    public static String defaultName() {
        AlContext.ensureAlcLoaded();
        String result = ALC10.alcGetString(0L, 785);
        OpenAlErrors.checkAlcError(0L);
        return result;
    }
    public static List<String> deviceNames() {
        AlContext.ensureAlcLoaded();
        List<String> result = ALUtil.getStringList(0L, 784);
        OpenAlErrors.checkAlcError(0L);
        return result == null ? Collections.emptyList() : result;
    }
}
