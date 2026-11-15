package com.adjustclient.script.api;

import com.adjustclient.script.api.Entity;
import com.adjustclient.script.api.EntityPlayer;
import com.adjustclient.script.api.Vec3;

public final class Simulation {
    public Simulation(EntityPlayer player) {
    }

    public Simulation() {
    }

    public static native Simulation fromPlayer(EntityPlayer var0);

    public native void initializeFrom(EntityPlayer var1, boolean var2);

    public native Simulation includeSpeedModule();

    public native boolean update();

    public native void setPosition(Vec3 var1);

    public native void setMotion(Vec3 var1);

    public native void setState(boolean var1, boolean var2);

    public native void setInput(float var1, float var2);

    public native void setRotation(float var1, float var2);

    public native void setRotationYaw(float var1, float var2);

    public native void setIsJumping(boolean var1);

    public native void setHorizontalSpeed(double var1, boolean var3);

    public native void setSpeed(double var1, boolean var3, float var4);

    public native Vec3 getPositionVector();

    public native Vec3 getMotion();

    public native double getHorizontalSpeed();

    public native float getForwardInput();

    public native float getStrafeInput();

    public native boolean isOnGround();

    public native boolean isCollidedVertically();

    public native boolean isCollidedHorizontally();

    public native Entity getSimulationPlayer();
}

