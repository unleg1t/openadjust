package com.adjustclient.script.api;

import com.adjustclient.script.api.RotationModel;

public final class OrganicRotationModel
extends RotationModel {
    public OrganicRotationModel(double speed, double driftIntensity, double jitterIntensity) {
    }

    public native double getSpeed();

    public native double getDriftIntensity();

    public native double getJitterIntensity();
}

