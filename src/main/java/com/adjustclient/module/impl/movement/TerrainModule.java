package com.adjustclient.module.impl.movement;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MoveEvent;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.event.BlockCollisionCandidateEvent;
import com.adjustclient.event.SlimeSlowdownEvent;
import com.adjustclient.event.SlimeBounceEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockFire;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import com.adjustclient.service.util.ServiceUtil;

public final class TerrainModule extends Module {
    public static TerrainModule INSTANCE;
    public final ModeSetting<TerrainMode> mode;
    public final ExpandSetting damage;
    public final BooleanSetting antiCactus;
    public final BooleanSetting antiFire;
    public final BooleanSetting soulSand;
    public final BooleanSetting slime;
    public final BooleanSetting web;
    public final BooleanSetting jumpOffGround;

    public TerrainModule() {
        super("Terrain", Category.MOVEMENT);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", TerrainMode.LEGIT, TerrainMode.values());
        damage = new ExpandSetting(this, "Damage",
                () -> mode.getValue() == TerrainMode.LEGIT || mode.getValue() == TerrainMode.HYPIXEL);
        antiCactus = new BooleanSetting(damage, "Anti Cactus", true,
                () -> mode.getValue() == TerrainMode.LEGIT || mode.getValue() == TerrainMode.HYPIXEL);
        antiFire = new BooleanSetting(damage, "Anti Fire", true,
                () -> mode.getValue() == TerrainMode.LEGIT || mode.getValue() == TerrainMode.HYPIXEL);
        soulSand = new BooleanSetting(this, "Soul Sand", true,
                () -> mode.getValue() == TerrainMode.LEGIT || mode.getValue() == TerrainMode.HYPIXEL);
        slime = new BooleanSetting(this, "Slime", true,
                () -> mode.getValue() == TerrainMode.LEGIT || mode.getValue() == TerrainMode.HYPIXEL);
        web = new BooleanSetting(this, "Web", true,
                () -> mode.getValue() == TerrainMode.LEGIT || mode.getValue() == TerrainMode.HYPIXEL);
        jumpOffGround = new BooleanSetting(this, "Jump Off Ground", true,
                () -> mode.getValue() != TerrainMode.LEGIT && mode.getValue() != TerrainMode.HYPIXEL);
        setHidden(false);
    }

    @EventHandler public void onSlimeBounce(SlimeBounceEvent event) {
        if (!isEnabled() || !slime.isEnabled() || mc.thePlayer.onGroundTicks < 2 || mc.thePlayer.inWater) return;
        if (mc.thePlayer.isInLava()) return;
        event.bounceAllowed = false;
        mc.thePlayer.motionY = -0.0784D;
    }

    @EventHandler public void onSlimeSlowdown(SlimeSlowdownEvent event) {
        if (!isEnabled() || !slime.isEnabled()) return;
        event.horizontalDamping = 1.0D;
        event.slipperiness = 0.6F;
    }

    @EventHandler public void onBlockCollision(BlockCollisionCandidateEvent event) {
        if (!isEnabled()) return;
        if (event.block instanceof BlockCactus) {
            if (antiCactus.isEnabled()) event.boundingBox = new AxisAlignedBB(event.position, event.position.add(1, 1, 1));
        } else if (event.block instanceof BlockFire && antiFire.isEnabled()
                && !mc.thePlayer.isPotionActive(Potion.fireResistance) && !mc.thePlayer.isImmuneToFire()) {
            BlockPos pos = event.position;
            if (pos.getY() >= mc.thePlayer.posY) event.boundingBox = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        }
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled()) return;
        TerrainMode selected = mode.getValue();
        if (selected != TerrainMode.LEGIT && selected != TerrainMode.HYPIXEL) return;
        if (selected == TerrainMode.HYPIXEL && mc.thePlayer.isInWeb && ServerTracker.INSTANCE.ticksSinceTabUpdate >= 2) {
            event.forward = false;
            event.left = false;
            event.right = false;
            event.back = false;
            return;
        }
        if (!MovementUtil.isMoving() || !mc.thePlayer.onGround || event.jump || mc.thePlayer.isInWeb || event.sneak
                || !mc.thePlayer.isSprinting() || mc.thePlayer.jumpTicks >= 1) return;
        Simulation simulation = new Simulation(mc.thePlayer);
        double startY = simulation.player.posY;
        simulation.setRotation(event.getMovement(), true);
        simulation.player.tick();
        double jumpingSpeed = new Vec3(simulation.player.motionX, simulation.player.motionY, simulation.player.motionZ).lengthVector();
        if (simulation.player.posY % 0.5D == 0.0D) {
            simulation.simulateTick(mc.thePlayer, false);
            simulation.setRotation(event.getMovement(), false);
            simulation.player.tick();
            double walkingSpeed = new Vec3(simulation.player.motionX, simulation.player.motionY, simulation.player.motionZ).lengthVector();
            if (jumpingSpeed > walkingSpeed) event.jump = true;
        } else if (jumpOffGround.isEnabled() && !simulation.player.onGround) {
            double rise = simulation.player.posY - startY;
            if (rise > 0.0D && rise < 0.21D) {
                simulation.player.tick();
                if (!simulation.player.onGround) {
                    simulation.player.tick();
                    if (!simulation.player.onGround) {
                        simulation.player.tick();
                        if (!simulation.player.onGround) return;
                    }
                }
                simulation.simulateTick(mc.thePlayer, false);
                simulation.setRotation(event.getMovement(), false);
                simulation.player.tick();
                double walkingSpeed = new Vec3(simulation.player.motionX, simulation.player.motionY, simulation.player.motionZ).lengthVector();
                if (jumpingSpeed > walkingSpeed) event.jump = true;
            }
        }
    }

    @EventHandler public void onWebMove(MoveEvent event) {
        if (!isEnabled() || !web.isEnabled()) return;
        switch (mode.getValue()) {
            case VULCAN:
                double base = 0.08989999999999999D;
                float potion = mc.thePlayer.isPotionActive(Potion.moveSpeed)
                        ? (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) * 0.067500003F : 0.0F;
                float extra = (2.0F * mc.thePlayer.capabilities.getWalkSpeed() - 0.2F) * 3.5F + potion;
                double speed = base;
                if (base > (double) extra + base) speed = (double) potion + base;
                MovementUtil.setSpeed(true, speed, MovementUtil.getDirection() * 0.0174532925199433D);
                // fall through: both non-legit modes replace web-scaled displacement.
            case VANILLA:
                event.x = mc.thePlayer.motionX;
                event.y = mc.thePlayer.motionY;
                event.z = mc.thePlayer.motionZ;
                event.cancelled = true;
                break;
            default: break;
        }
    }

    @EventHandler public void onHypixelWebMove(MoveEvent event) {
        if (!isEnabled() || mode.getValue() != TerrainMode.HYPIXEL || !web.isEnabled() || !mc.thePlayer.isInWeb
                || ServerTracker.INSTANCE.ticksSinceTabUpdate >= 2 || Adjust.movementPredictor.historyTicks < 21) return;
        if (MovementUtil.isMoving()) MovementUtil.addSpeed(0.000000041723251D);
        else mc.thePlayer.motionY = -(3.95D - 0.001D * ServiceUtil.movementAdjustment);
        event.x = mc.thePlayer.motionX;
        event.y = mc.thePlayer.motionY;
        event.z = mc.thePlayer.motionZ;
    }

    public boolean isActive() {
        if (!isEnabled()) return false;
        Feature feature = mode.getValue().field20;
        return feature == null || feature.isAllowed();
    }
}
