package com.adjustclient.core.audio.openal;

import java.util.Collections;
import java.util.List;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALUtil;
public final class AlPlaybackDevice extends AlDevice {
    public Object field4;
    public boolean open(String name) {
        AlContext.ensureAlcLoaded();
        handle = ALC10.alcOpenDevice(name);
        OpenAlErrors.checkAlcError(handle);
        return handle != 0L;
    }
    public static String defaultName() {
        AlContext.ensureAlcLoaded();
        String result = ALC10.alcGetString(0L, 4114);
        OpenAlErrors.checkAlcError(0L);
        return result;
    }
    public static List<String> deviceNames() {
        AlContext.ensureAlcLoaded();
        List<String> result = ALUtil.getStringList(0L, 4115);
        OpenAlErrors.checkAlcError(0L);
        return result == null ? Collections.emptyList() : result;
    }
}
