package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.RotationModel;
import com.adjustclient.utilities.script.api.Vec2f;

public final class RotationHelper {
    public static native void rotate(float var0, float var1, RotationModel var2);

    public static native void rotate(Vec2f var0, RotationModel var1);

    public static native boolean isActive();

    public static native Vec2f getTargetRotation();

    public static native Vec2f getRenderRotation();
}

