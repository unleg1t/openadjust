package com.adjustclient.utilities.hypixel.game.zombies.weapon;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public enum ZombiesWeapon {
    KNIFE("Knife", ZombiesWeaponType.KNIFE),
    PUNCHER("Puncher", ZombiesWeaponType.KNIFE),
    PISTOL("Pistol", ZombiesWeaponType.GUN),
    UNKNOWN("Unknown", ZombiesWeaponType.GUN);
    public final String weaponId;
    public final ZombiesWeaponType field20;
    public Object field24;
    ZombiesWeapon(String weaponId, ZombiesWeaponType type) {
        this.weaponId = weaponId;
        this.field20 = type;
    }
    public static ZombiesWeapon fromStack(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("ExtraAttributes", 10)) return null;
        NBTTagCompound extra = tag.getCompoundTag("ExtraAttributes");
        if (!extra.hasKey("weapon", 8)) return null;
        String id = extra.getString("weapon");
        for (ZombiesWeapon weapon : values()) if (weapon.weaponId.equalsIgnoreCase(id)) return weapon;
        return null;
    }
}
