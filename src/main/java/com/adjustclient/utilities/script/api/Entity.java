package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.EnumFacing;
import com.adjustclient.utilities.script.api.ItemStack;
import com.adjustclient.utilities.script.api.NetworkPlayerInfo;
import com.adjustclient.utilities.script.api.PotionEffect;
import com.adjustclient.utilities.script.api.Vec2f;
import com.adjustclient.utilities.script.api.Vec3;
import java.util.List;

public class Entity {
    public native double distanceTo(Vec3 var1);

    public native double distanceToSq(Vec3 var1);

    public native float getAbsorption();

    public native float getAir();

    public native Entity getAngler();

    public native ItemStack getArmorInSlot(int var1);

    public native Vec3 getBlockPosition();

    public native String getDisplayName();

    public native float getExperience();

    public native int getExperienceLevel();

    public native float getEyeHeight();

    public native EnumFacing getFacing();

    public native float getFallDistance();

    public native int getFireResistance();

    public native float getHealth();

    public native float getHeight();

    public native ItemStack getHeldItem();

    public native double getHorizontalSpeed();

    public native int getHunger();

    public native int getHurtTime();

    public native Vec3 getLastPosition();

    public native float getMaxHealth();

    public native int getMaxHurtTime();

    public native String getName();

    public native NetworkPlayerInfo getNetworkPlayer();

    public native Vec3 getPosition();

    public native List<PotionEffect> getPotionEffects();

    public native Vec2f getPrevRotations();

    public native float getPrevSwingProgress();

    public native Entity getRiddenByEntity();

    public native Entity getRidingEntity();

    public native Vec2f getRotations();

    public native float getSaturation();

    public native Vec3 getServerPosition();

    public native float getSwingProgress();

    public native int getTicksExisted();

    public native float getWidth();

    public native boolean isBurning();

    public native boolean isCollided();

    public native boolean isCollidedHorizontally();

    public native boolean isCollidedVertically();

    public native boolean isCreative();

    public native boolean isDead();

    public native boolean isHoldingBlock();

    public native boolean isHoldingWeapon();

    public native boolean isInLava();

    public native boolean isInLiquid();

    public native boolean isInvisible();

    public native boolean isInWater();

    public native boolean isOnLadder();

    public native boolean isRiding();

    public native boolean isSleeping();

    public native boolean isSneaking();

    public native boolean isSprinting();

    public native boolean isUsingItem();

    public native boolean onGround();

    public native void setPosition(Vec3 var1);

    public native void setPosition(double var1, double var3, double var5);

    public native void setRotations(Vec2f var1);
}

