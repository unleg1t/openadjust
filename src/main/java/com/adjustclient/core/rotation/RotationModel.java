package com.adjustclient.core.rotation;

import com.adjustclient.utilities.Vec2f;

public interface RotationModel {
    Vec2f apply(Vec2f from, Vec2f to, float factor);
}
