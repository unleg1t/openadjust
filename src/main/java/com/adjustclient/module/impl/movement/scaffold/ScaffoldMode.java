package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.event.Listener;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.RotationModel;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;

public abstract class ScaffoldMode implements Listener, MinecraftInstance, ModeOption {

    public ScaffoldModule module;
    protected String name;
    protected int capabilities;
    public boolean towering;

    @Override
    public String getName() {
        return this.name;
    }

    public boolean isActive() {
        ScaffoldModule module = this.module;
        if (!module.isEnabled()) {
            return false;
        }
        return module.settings.mode.getValue() == this;
    }

    public void reset() {
        this.towering = false;
    }

    public boolean isAboveBuildLimit(int y) {
        return y >= mc.theWorld.getHeight();
    }

    public abstract void onPlace(PlacementTarget placement);

    public abstract boolean canSprint();

    public void onPostRotation() {
    }

    public boolean canPlaceInGame() {
        return true;
    }

    public boolean e() {
        return false;
    }

    public boolean canNoAscend() {
        return true;
    }

    public boolean canAutoJump() {
        return mc.thePlayer.onGround && mc.thePlayer.onGroundTicks >= 1;
    }

    public boolean allowForwardTurn() {
        return true;
    }

    public boolean shouldLookForward() {
        return false;
    }

    public RotationModel getForwardRotationModel() {
        return InstantRotationModel.INSTANCE;
    }

    public RotationModel getPlaceRotationModel() {
        return null;
    }

    public boolean canSnap() {
        return true;
    }

    public boolean useFastRotations() {
        return false;
    }

    public boolean forceRaycast() {
        return false;
    }

    public boolean isTowerLowOnly() {
        return false;
    }
}
