package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.RotationMode;
import com.adjustclient.utilities.script.api.RotationModel;

public final class RotationSetting {
    public native RotationModel createModel();

    public native RotationMode getValue();

    public native void setValue(RotationMode var1);

    public native RotationSetting description(String var1);
}

