package com.adjustclient.module.impl.player.zombies;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.MouseKey;
import com.adjustclient.event.*;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.module.util.CpsSettings;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.setting.*;
import com.adjustclient.util.*;
import com.adjustclient.hypixel.game.zombies.ZombiesGameState;
import com.adjustclient.hypixel.game.zombies.entity.prediction.*;
import com.adjustclient.hypixel.game.zombies.weapon.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import java.util.*;
import java.util.function.ToDoubleFunction;
import net.minecraft.init.Blocks;
import com.adjustclient.hypixel.game.zombies.weapon.ZombiesWeaponType;

public final class ZombiesAutoShoot implements Listener, MinecraftInstance {
    public static ZombiesAutoShoot INSTANCE;
    public final ZombiesModule owner;
    public final BooleanSetting enabled;
    public final Bind bind;
    public final CpsSettings cps;
    public final ModeSetting<Sorting> sorting;
    public final NumberSetting fieldOfView;
    public final BooleanSetting wallBang;
    public final BooleanSetting autoSwap;
    public final BooleanSetting predictionEnabled;
    public final NumberSetting pingMultiplier;
    public final NumberSetting regression;
    public final NumberSetting motionBlend;
    public final NumberSetting maxTime;
    public EntityLivingBase target;
    public int lastSlot;
    public boolean shooting;

    public ZombiesAutoShoot(ZombiesModule owner) {
        this.owner = owner;
        INSTANCE = this;
        bind = Adjust.INSTANCE.bindManager.register("zombies.autoshootbind",
                mode -> {
                }, "Zombies Auto Shoot Bind");
        bind.setKey(new MouseKey(2));
        ExpandSetting group = new ExpandSetting(owner, "Auto Shoot");
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        group.enabledPredicate = () -> enabled.isEnabled();
        new KeybindSetting(group, "Bind", bind);
        cps = new CpsSettings(group, 14, 17);
        sorting = new ModeSetting<>(group, "Sorting", Sorting.AIM, Sorting.values());
        fieldOfView = new NumberSetting(group, "Field of view", 180, 5, 180, 1);
        wallBang = new BooleanSetting(group, "Wall Bang", true, "On", "Off");
        autoSwap = new BooleanSetting(group, "Auto Swap", true, "On", "Off");
        ExpandSetting prediction = new ExpandSetting(group, "Prediction");
        predictionEnabled = new BooleanSetting(prediction, "Enabled", true, "Yes", "No");
        prediction.enabledPredicate = () -> predictionEnabled.isEnabled();
        pingMultiplier = new NumberSetting(prediction, "Ping Multiplier", 1.0D, 0.0D, 2.0D, 0.05D);
        regression = new NumberSetting(prediction, "Regression", 0.6D, 0.0D, 2.0D, 0.05D);
        motionBlend = new NumberSetting(prediction, "Motion Blend", 0.3D, 0.0D, 1.0D, 0.05D);
        maxTime = new NumberSetting(prediction, "Max. Time", 0.5D, 0.0D, 1.0D, 0.05D);
    }

    @EventHandler(priority = 42) public void onUseItem(IngameActionEvent event) {
        if (!owner.isEnabled() || !enabled.isEnabled() || !shooting) return;
        Adjust.mouseClickHelper.right.clicks = Integer.valueOf(1);
        Adjust.mouseClickHelper.right.down = Boolean.TRUE;
        Adjust.mouseClickHelper.right.holdTicks = 0;
        Adjust.cpsCounter.recordClick();
        event.actionStage = false;
    }

    public void updateAutoSwap() {
        if (target == null || !autoSwap.isEnabled()) return;
        int slot = lastSlot % 9;
        for (int checked = 0; checked < 9; checked++) {
            slot = (slot + 1) % 9;
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (!canShoot(stack)) continue;
            Adjust.slotSpoofer.slot = slot;
            Adjust.slotSpoofer.active = true;
            Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
            Adjust.slotSpoofer.field17 = false;
            Adjust.slotSpoofer.renderSpoofed = false;
            lastSlot = slot;
            return;
        }
    }

    @EventHandler public void onUpdate(PreClientTickEvent event) {
        target = null;
        shooting = false;
        if (!owner.isEnabled() || !enabled.isEnabled() || mc.thePlayer == null || !bind.isHeld()) return;
        if (Adjust.gameStateTracker.state.startTimer == null) return;
        for (PredictedTarget tracked : getTargets()) {
            Entity entity = tracked.getEntity();
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;
            // isInFov returns true for a target outside the requested angle in this utility.
            if (RotationUtil.isInFov(fieldOfView.getInt(), living)) continue;
            PredictedPosition prediction = predictionEnabled.isEnabled()
                    ? tracked.predictor.predict(pingMultiplier.getDouble(), regression.getDouble(), motionBlend.getDouble(), maxTime.getDouble())
                    : new PredictedPosition(living.getPositionVector(), 1.0F);
            double eyeHeight = living.getEyeHeight();
            if (living.isChild()) eyeHeight /= 2.0D;
            Vec3 aim = prediction.position().addVector(0.0D, eyeHeight * 0.5D, 0.0D);
            if (!canSee(aim)) continue;
            Vec2f rotation = RotationUtil.getRotations(aim);
            RotationHandler.rotate(InstantRotationModel.INSTANCE, rotation.x, rotation.y);
            target = living;
            shooting = canShoot(mc.thePlayer.inventory.getCurrentItem()) && Adjust.cpsCounter.canClick(cps);
            return;
        }
    }

    public boolean canShoot(ItemStack stack) {
        ZombiesWeapon weapon = ZombiesWeapon.fromStack(stack);
        if (weapon == null || weapon.field20 != ZombiesWeaponType.GUN) return false;
        return stack.getItemDamage() == 0 || stack.getItemDamage() == stack.getMaxDamage() - 1 || stack.stackSize >= 2;
    }

    public boolean canWallBang(BlockPos position) {
        if (!wallBang.isEnabled()) return false;
        Block block = BlockUtil.getBlock(position);
        if (block == Blocks.dark_oak_stairs || block == Blocks.spruce_door) return false;
        return block instanceof BlockStairs || block instanceof BlockFence || block instanceof BlockChest
                || block instanceof BlockSlab || block instanceof BlockSign // runtime type IDs 14239..14241
                || block == Blocks.barrier || block == Blocks.iron_bars;
    }

    public boolean canSee(Vec3 destination) {
        double height = mc.thePlayer.isPlayerSleeping() ? 0.2000000029802322D
                : mc.thePlayer.movementInput.sneak ? 1.5399999618530273D : 1.620000004768372D;
        Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + height, mc.thePlayer.posZ);
        MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(eyes, destination, false, true, false);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return true;
        return canWallBang(hit.getBlockPos());
    }

    public List<PredictedTarget> getTargets() {
        if (!(Adjust.gameStateTracker.state instanceof ZombiesGameState)) return List.of();
        ZombiesGameState state = (ZombiesGameState) Adjust.gameStateTracker.state;
        ToDoubleFunction<PredictedTarget> score;
        switch (sorting.getValue()) {
            case AIM:
                score = tracked -> {
                    Entity entity = tracked.getEntity();
                    if (entity == null) return -1.0D;
                    Vec2f rotation = RotationUtil.getRotations(entity.getPositionVector().addVector(0.0D, entity.getEyeHeight(), 0.0D));
                    float yaw = mc.thePlayer.rotationYaw;
                    float unwrapped = MathHelper.wrapAngleTo180_float(rotation.x - yaw) + yaw;
                    Vec2f rendered = RenderRotationTracker.INSTANCE.rotation;
                    return Math.abs(MathHelper.wrapAngleTo180_float(unwrapped - (rendered == null ? yaw : rendered.x)));
                };
                break;
            case DISTANCE:
                score = tracked -> {
                    Entity entity = tracked.getEntity();
                    return entity instanceof EntityLivingBase ? entity.getDistanceSqToEntity(mc.thePlayer) : -536870912.0D;
                };
                break;
            case HEALTH:
                score = tracked -> {
                    Entity entity = tracked.getEntity();
                    return entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getHealth() : 0.0D;
                };
                break;
            default: throw new IllegalStateException();
        }
        return ((List<PredictedTarget>) (List<?>) state.getPowerUps()).stream().filter(tracked -> {
            Entity entity = tracked.getEntity();
            return entity instanceof EntityLivingBase && !entity.isDead && ((EntityLivingBase) entity).getHealth() > 0.0F;
        }).sorted(Comparator.comparingDouble(score)).toList();
    }

    public boolean isActive() { return owner.isEnabled() && enabled.isEnabled(); }

    public enum Sorting implements ModeOption {
        AIM("Aim"), DISTANCE("Distance"), HEALTH("Health");
        private final String name;
        Sorting(String name) { this.name = name; }
        public String getName() { return name; }
    }
}
