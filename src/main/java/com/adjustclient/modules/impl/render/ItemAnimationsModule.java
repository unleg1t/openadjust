package com.adjustclient.modules.impl.render;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import java.awt.Color;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.EnumAction;
import org.lwjgl.opengl.GL11;

public final class ItemAnimationsModule extends Module {
    public static ItemAnimationsModule INSTANCE;
    public final ExpandSetting enchantmentGlint;
    public final BooleanSetting enchantmentGlintEnabled;
    public final ColorSetting glintColor;
    public final ExpandSetting swingSpeed;
    public final NumberSetting swingSpeedUsing;
    public final NumberSetting swingSpeedNormal;
    public final BooleanSetting ignoreEffects;
    public final ExpandSetting position;
    public final ExpandSetting positionUsing;
    public final NumberSetting positionUsingX;
    public final NumberSetting positionUsingY;
    public final ExpandSetting positionNormal;
    public final NumberSetting positionNormalX;
    public final NumberSetting positionNormalY;
    public final NumberSetting scale;
    public final BooleanSetting swordOnly;
    public final ExpandSetting sword;
    public final BooleanSetting swordEnabled;
    public final ModeSetting<SwordAnimation> animation;
    public final NumberSetting equipAnimation;
    public final BooleanSetting accurate;
    public final BooleanSetting swingDuringUse;
    public final BooleanSetting oldTransform;
    public final BooleanSetting hideSwinging;

    public ItemAnimationsModule() {
        super("Item Animations", Category.RENDER);
        INSTANCE = this;
        setHidden(false);
        enchantmentGlint = new ExpandSetting(this, "Enchantment Glint");
        enchantmentGlint.enabledPredicate = new ItemAnimationsGlintCondition(this);
        enchantmentGlintEnabled = new BooleanSetting(enchantmentGlint, "Enabled", false, "Yes", "No");
        glintColor = new ColorSetting(enchantmentGlint, "Color", new Color(67, 90, 209), false, false, ColorSetting.Mode.PLAIN, null);
        swingSpeed = new ExpandSetting(this, "Swing Speed");
        swingSpeedUsing = new NumberSetting(swingSpeed, "Using", 0, -15, 4, 1);
        swingSpeedNormal = new NumberSetting(swingSpeed, "Normal", 0, -15, 4, 1);
        ignoreEffects = new BooleanSetting(swingSpeed, "Ignore effects", false);
        position = new ExpandSetting(this, "Position");
        positionUsing = new ExpandSetting(position, "Using");
        positionUsingX = new NumberSetting(positionUsing, "X", 0.0F, -0.8F, 0.5F, 0.01F);
        positionUsingY = new NumberSetting(positionUsing, "Y", 0.0F, -0.3F, 0.5F, 0.01F);
        positionNormal = new ExpandSetting(position, "Normal");
        positionNormalX = new NumberSetting(positionNormal, "X", 0.0F, -0.8F, 0.5F, 0.01F);
        positionNormalY = new NumberSetting(positionNormal, "Y", 0.0F, -0.3F, 0.5F, 0.01F);
        scale = new NumberSetting(position, "Scale", 1.0F, 0.5F, 1.0F, 0.01F);
        swordOnly = new BooleanSetting(position, "Sword only", false);
        sword = new ExpandSetting(this, "Sword");
        swordEnabled = new BooleanSetting(sword, "Enabled", true, "Yes", "No");
        animation = new ModeSetting<>(sword, "Animation", SwordAnimation.VANILLA, SwordAnimation.values());
        animation.setDisplayName("Type");
        equipAnimation = new NumberSetting(sword, "Equip animation", 0.5F, 0.0F, 1.0F, 0.05F);
        equipAnimation.setDescription("Controls the animation played when your item is updated.");
        accurate = new BooleanSetting(sword, "Accurate", true,
                () -> animation.getValue() != SwordAnimation.VANILLA);
        swingDuringUse = new BooleanSetting(this, "Swing during use", true);
        swingDuringUse.setDescription("Controls swinging during blocking with items (clientside only).");
        oldTransform = new BooleanSetting(this, "Old transform", true);
        oldTransform.setDescription("Controls 1.7 block hitting animation.");
        hideSwinging = new BooleanSetting(this, "Hide swinging", false);
        hideSwinging.setDescription("Controls swing animation.");
    }

    public boolean shouldSwingDuringUse() { return isEnabled() && swingDuringUse.isEnabled(); }

    public void transformBlocking(boolean reverse) {
        if (!oldTransform.isEnabled()) return;
        if (reverse) {
            GL11.glRotatef(-335.0f, 0, 0, 1);
            GL11.glRotatef(-50.0f, 0, 1, 0);
            GL11.glTranslatef(0, 0.5f, 0);
        } else {
            GL11.glTranslatef(0, -0.5f, 0);
            GL11.glRotatef(50.0f, 0, 1, 0);
            GL11.glRotatef(335.0f, 0, 0, 1);
        }
    }

    public void applyPosition(ItemStack stack) {
        if (!(stack != null && stack.getItem() instanceof ItemSword) && swordOnly.isEnabled()) return;
        GL11.glTranslatef(0, (1.0f - scale.getFloat()) * 0.5f, 0);
        if (mc.thePlayer.getItemInUseCount() > 0) GL11.glTranslatef(positionUsingX.getFloat(), positionUsingY.getFloat(), 0);
        else GL11.glTranslatef(positionNormalX.getFloat(), positionNormalY.getFloat(), 0);
    }

    public boolean transformItem(ItemStack stack) {
        if (!oldTransform.isEnabled()) return true;
        if (mc.thePlayer.getItemInUseCount() > 0 && stack.getItemUseAction() == EnumAction.BLOCK
                && swordEnabled.isEnabled() && !(animation.getValue() == SwordAnimation.VANILLA && accurate.isEnabled())) return true;
        if (stack.getItem().shouldRotateAroundWhenRendering()) GL11.glRotatef(180.0f, 0, 1, 0);
        GL11.glTranslatef(0.58800083f, 0.36999986f, -0.77000016f);
        GL11.glTranslatef(0, -0.30000001f, 0);
        GL11.glScalef(1.5f, 1.5f, 1.5f);
        GL11.glRotatef(50.0f, 0, 1, 0);
        GL11.glRotatef(335.0f, 0, 0, 1);
        GL11.glTranslatef(-0.9375f, -0.0625f, 0);
        GL11.glScalef(-2.0f, 2.0f, -2.0f);
        if (mc.getRenderItem().shouldRenderItemIn3D(stack)) {
            GL11.glScalef(0.58823526f, 0.58823526f, 0.58823526f);
            GL11.glRotatef(-25.0f, 0, 0, 1);
            GL11.glRotatef(0, 1, 0, 0);
            GL11.glRotatef(135.0f, 0, 1, 0);
            GL11.glTranslatef(0, -0.25f, -0.125f);
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            return true;
        }
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        return false;
    }

    public void transformBow() {
        if (!oldTransform.isEnabled()) return;
        GL11.glTranslatef(-0.15000001f, -0.2f, 0);
        GL11.glRotatef(70.0f, 1, 0, 0);
        GL11.glTranslatef(0.119f, 0.2f, -0.024f);
    }

    public boolean shouldHideSwinging() { return isEnabled() && hideSwinging.isEnabled(); }
}
