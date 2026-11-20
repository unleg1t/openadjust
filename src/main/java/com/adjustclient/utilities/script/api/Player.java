package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.EntityPlayer;
import com.adjustclient.utilities.script.api.GuiScreen;
import com.adjustclient.utilities.script.api.Inventory;
import com.adjustclient.utilities.script.api.Vec2f;
import com.adjustclient.utilities.script.api.Vec3;

public final class Player
extends EntityPlayer {
    public final Inventory inventory = null;

    public native boolean isAllowEdit();

    public native boolean allowFlying();

    public native void chat(String var1);

    public native void closeScreen();

    public native void dropItem(boolean var1);

    public native float getForward();

    public native Vec3 getMotion();

    public native String getRecordPlaying();

    public native float getRenderArmPitch();

    public native float getRenderArmYaw();

    public native Vec2f getRotation(Vec3 var1);

    public native GuiScreen getScreen();

    public native float getStrafe();

    public native String getSubTitle();

    public native String getTabFooter();

    public native String getTabHeader();

    public native String getTitle();

    @Override
    public native boolean isCreative();

    public native boolean isJump();

    public native boolean isMoving();

    public native void setMotion(Vec3 var1);

    public native void jump();

    public native void setSprinting(boolean var1);

    public native void swingItem();

    public native void swingItemServerside();

    public native void respawnPlayer();

    public native void sendPlayerAbilities();

    public native void changeCurrentItem(int var1);

    public native void playSound(String var1, float var2, float var3);

    private native Inventory $script$read$inventory();
}

