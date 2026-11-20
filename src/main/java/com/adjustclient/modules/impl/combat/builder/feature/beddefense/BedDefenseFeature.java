package com.adjustclient.modules.impl.combat.builder.feature.beddefense;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MovementInputEvent;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.core.events.CollisionBoxesEvent;
import com.adjustclient.modules.impl.player.breaker.BreakerModule;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.modules.impl.combat.builder.BuilderModule;
import com.adjustclient.modules.impl.combat.builder.BuildTarget;
import com.adjustclient.modules.impl.combat.builder.BuilderBlockFilter;
import com.adjustclient.modules.impl.combat.builder.feature.BuilderFeature;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import com.adjustclient.modules.impl.combat.killaura.KillAuraModule;

public final class BedDefenseFeature extends BuilderFeature {
    public boolean jumping;
    public final Cooldown searchCooldown = new Cooldown(0);
    public final BooleanSetting optimizeMovement;
    public final NumberSetting waitToPlace;
    public final BedDefenseSetting defenses;
    public Vec3 bedSearchOrigin;
    public String presetName;
    public BlockPos movementTarget;
    public double jumpDirectionX;
    public double jumpDirectionZ;
    public int movementTicks;
    public BedDefenseRecorder recorder;

    public BedDefenseFeature(BuilderModule owner, String bindId, String bindDescription) {
        super(owner, "Bed Defense", bindId, bindDescription, false);
        optimizeMovement = new BooleanSetting(settings, "Optimize movement", false);
        waitToPlace = new NumberSetting(settings, "Wait to place", 0, 0, 3, 1);
        defenses = new BedDefenseSetting(settings, "Defenses", BedDefensePresets.PRESETS);
        recorder = new BedDefenseRecorder(this, defenses);
        defenses.recordingCallback = name -> {
            recorder.beginRecording(name);
        };
    }

    public void updateBuild() {
        Minecraft mc = Minecraft.getMinecraft();
        var placer = owner.placer;
        if (placer.target == null) {
            if (mc.playerController.getIsHittingBlock()
                    || com.adjustclient.modules.impl.combat.killaura.KillAuraModule.INSTANCE.isEnabled()
                    && com.adjustclient.modules.impl.combat.killaura.KillAuraModule.INSTANCE.targetSelector.rotationTarget != null) {
                placer.target = null;
                placer.placement = null;
                placer.slot = -1;
                placer.waitTicks = 0;
                deactivate();
                return;
            }
            if (!searchCooldown.isExpired()) return;
            BlockPos bed = BlockUtil.findBed(
                    pos -> {
                        if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.bed) return false;
                        com.adjustclient.utilities.hypixel.HypixelGameState state = Adjust.gameStateTracker.state;
                        if (state instanceof BedWarsGameState game && game.startTimer != null) {
                            if (game.spawnPosition == null) return false;
                            return pos.distanceSq(game.spawnPosition.xCoord,
                                    game.spawnPosition.yCoord,
                                    game.spawnPosition.zCoord) <= 512.0D;
                        }
                        return true;
                    });
            if (bed == null) {
                searchCooldown.timer.reset();
                searchCooldown.durationMs = 100;
                return;
            }
            BedAnchor anchor = BedAnchor.fromBed(bed, bedSearchOrigin);
            if (anchor == null) {
                new NotificationBuilder("Builder").addText("Could not determine your bed orientation.", 0xFFBEBCBD).show();
                deactivate();
                return;
            }
            BedDefensePlan plan = selectPlan(anchor);
            if (plan == null) {
                new NotificationBuilder("Builder").addText("You do not have enough materials in your hotbar.", 0xFFBEBCBD).show();
                deactivate();
                return;
            }
            if (plan.positions().isEmpty()) {
                if (plan.complete()) {
                    new NotificationBuilder("Builder").append(plan.preset().name)
                            .addText(" is already complete at your bed.", 0xFFBEBCBD).show();
                } else {
                    new NotificationBuilder("Builder").addText("The remaining defense positions are obstructed.", 0xFFBEBCBD).show();
                }
                deactivate();
                return;
            }
            presetName = plan.preset().name;
            placer.target = new BuildTarget(List.copyOf(plan.positions()), plan.positions(),
                    waitToPlace.getValue().intValue(), true, true, false, false);
        } else if (placer.target.pending.isEmpty()) {
            new NotificationBuilder("Builder").addText("Finished building ", 0xFFBEBCBD)
                    .append(presetName).addText(" at your bed.", 0xFFBEBCBD).show();
            placer.target = null;
            placer.placement = null;
            placer.slot = -1;
            placer.waitTicks = 0;
            deactivate();
        }
    }

    @EventHandler
    public void onInput(MovementInputEvent event) {
        if (!active || !owner.isEnabled()) return;
        if (!optimizeMovement.isEnabled() || isMovementKeyDown()) {
            jumping = false;
            jumpDirectionX = jumpDirectionZ = 0;
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        var placer = owner.placer;
        BuildTarget target = placer.target;
        if (target == null || !target.ordered || target.pending.isEmpty()) {
            movementTarget = null;
            movementTicks = 0;
            jumping = false;
            jumpDirectionX = jumpDirectionZ = 0;
            return;
        }
        BlockPos pos = target.pending.get(0).position;
        if (!pos.equals(movementTarget)) {
            movementTarget = pos;
            movementTicks = 0;
            jumping = false;
            jumpDirectionX = jumpDirectionZ = 0;
        }
        if (placer.placement != null) {
            movementTicks = 0;
            jumping = false;
            jumpDirectionX = jumpDirectionZ = 0;
            return;
        }
        if (jumping) {
            if (!mc.thePlayer.onGround) {
                BedDefenseMoveInput input = movementInput(jumpDirectionX, jumpDirectionZ);
                event.back = input.forward() < 0;
                event.forward = input.forward() > 0;
                event.left = input.strafe() > 0;
                event.right = input.strafe() < 0;
                return;
            }
            jumping = false;
            jumpDirectionX = jumpDirectionZ = 0;
        }
        if (++movementTicks < 2) return;
        AxisAlignedBB box = new AxisAlignedBB(pos);
        Vec3 playerPos = mc.thePlayer.getPositionVector();
        Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        double x = center.xCoord - playerPos.xCoord;
        double z = center.zCoord - playerPos.zCoord;
        double horizontal = Math.hypot(x, z);
        if (mc.thePlayer.getEntityBoundingBox().intersectsWith(box)) {
            if (horizontal >= 0.01D) {
                steer(event, -x, -z);
            } else {
                double yaw = mc.thePlayer.rotationYaw * 0.0174532925199433D;
                steer(event, Math.sin(yaw), -Math.cos(yaw));
            }
            return;
        }
        Vec3 nearest = DistanceUtil.closestPoint(playerPos, box);
        double distanceSq = playerPos.squareDistanceTo(nearest);
        double reach = mc.playerController.isInCreativeMode() ? 5.0D : 4.5D;
        double nearReach = Math.max(0.0D, reach - 0.3D);
        double height = center.yCoord - mc.thePlayer.getPositionEyes(1.0F).yCoord;
        if (height > 0.75D && distanceSq < reach * reach && mc.thePlayer.onGround && canJumpInPlace()) {
            if (horizontal < 0.75D) {
                event.jump = true;
                return;
            }
            if (startJump(event, x, z)) return;
        }
        if (mc.thePlayer.onGround && jumpToward(event, x, z)) return;
        if (distanceSq >= nearReach * nearReach || horizontal > 2.25D) {
            steer(event, x, z);
            return;
        }
        if (height > 0.75D && mc.thePlayer.onGround && canJumpInPlace()) event.jump = true;
    }

    public BedDefensePlan selectPlan(BedAnchor anchor) {
        Minecraft mc = Minecraft.getMinecraft();
        BedDefensePlan obstructed = null;
        for (BedDefensePreset preset : defenses.getValue()) {
            if (!preset.enabled) continue;
            int[] remaining = new int[9];
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
                remaining[slot] = stack == null ? 0 : mc.playerController.isInCreativeMode() ? 256 : stack.stackSize;
            }
            List<BuilderBlockFilter> positions = new ArrayList<>();
            boolean complete = true;
            boolean enough = true;
            for (BedDefensePlacement placement : preset.placements) {
                BlockPos pos = new BlockPos(
                        anchor.origin.getX() + placement.z * anchor.forwardX
                                + placement.x * anchor.lateralX,
                        anchor.origin.getY() + placement.y,
                        anchor.origin.getZ() + placement.z * anchor.forwardZ
                                + placement.x * anchor.lateralZ);
                var state = mc.theWorld.getBlockState(pos);
                var type = placement.type;
                boolean matches = type == BedDefenseBlockType.OTHER
                        ? net.minecraft.block.Block.getIdFromBlock(state.getBlock()) == placement.blockId
                        : type.blocks.contains(state.getBlock());
                if (matches) continue;
                com.adjustclient.utilities.Timer timer = ServerTracker.INSTANCE.bedTimers.get(pos);
                if (timer != null && !timer.hasElapsed(2500L)) {
                    complete = false;
                    continue;
                }
                if (!state.getBlock().isReplaceable(mc.theWorld, pos)) {
                    complete = false;
                    continue;
                }
                int chosen = -1;
                for (int slot = 0; slot < 9; slot++) {
                    if (remaining[slot] < 1) continue;
                    ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
                    if (stack == null || !(stack.getItem() instanceof ItemBlock)) continue;
                    net.minecraft.block.Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (type == BedDefenseBlockType.OTHER
                            ? net.minecraft.block.Block.getIdFromBlock(block) == placement.blockId
                            : type.blocks.contains(block)) {
                        chosen = slot;
                        break;
                    }
                }
                if (chosen == -1) {
                    enough = false;
                    break;
                }
                --remaining[chosen];
                positions.add(new BuilderBlockFilter(pos, net.minecraft.util.EnumFacing.NORTH, type,
                        placement.blockId));
                complete = false;
            }
            if (!enough) continue;
            BedDefensePlan plan = new BedDefensePlan(preset, positions, complete);
            if (complete || !positions.isEmpty()) return plan;
            if (obstructed == null) obstructed = plan;
        }
        return obstructed;
    }

    public static boolean canWalk(double x, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        AxisAlignedBB next = player.getEntityBoundingBox().offset(x * 0.35D, 0, z * 0.35D);
        List<AxisAlignedBB> collisions = mc.theWorld.getCollidingBoundingBoxes(player, next);
        if (!collisions.isEmpty()) {
            next = next.offset(0, player.stepHeight, 0);
            if (!mc.theWorld.getCollidingBoundingBoxes(player, next, true, false).isEmpty()) return false;
        }
        return !mc.theWorld.getCollidingBoundingBoxes(player, next.offset(0, -0.25D, 0), true, false).isEmpty();
    }

    public void steer(MovementInputEvent event, double x, double z) {
        if (!Minecraft.getMinecraft().thePlayer.onGround) return;
        double length = Math.hypot(x, z);
        if (length < 0.001D) return;
        x /= length;
        z /= length;
        BedDefenseMoveInput input = movementInput(x, z);
        if (!canWalk(input.directionX(), input.directionZ())) {
            if (canJump(input.directionX(), input.directionZ())) {
                event.back = input.forward() < 0;
                event.forward = input.forward() > 0;
                event.left = input.strafe() > 0;
                event.right = input.strafe() < 0;
                event.jump = true;
                jumping = true;
                jumpDirectionX = input.directionX();
                jumpDirectionZ = input.directionZ();
                return;
            }
            input = movementInput(x * 0.7071067811865476D - z * 0.7071067811865475D,
                    x * 0.7071067811865475D + z * 0.7071067811865476D);
            if (!canWalk(input.directionX(), input.directionZ())) {
                input = movementInput(x * 0.7071067811865476D + z * 0.7071067811865475D,
                        -x * 0.7071067811865475D + z * 0.7071067811865476D);
                if (!canWalk(input.directionX(), input.directionZ())) {
                    input = movementInput(x * 6.123233995736766E-17D - z,
                            x + z * 6.123233995736766E-17D);
                    if (!canWalk(input.directionX(), input.directionZ())) {
                        input = movementInput(x * 6.123233995736766E-17D + z,
                                -x + z * 6.123233995736766E-17D);
                        if (!canWalk(input.directionX(), input.directionZ())) return;
                    }
                }
            }
        }
        event.back = input.forward() < 0;
        event.forward = input.forward() > 0;
        event.left = input.strafe() > 0;
        event.right = input.strafe() < 0;
    }

    public void activate() {
        Minecraft mc = Minecraft.getMinecraft();
        com.adjustclient.utilities.hypixel.HypixelGameState state = Adjust.gameStateTracker.state;
        bedSearchOrigin = state instanceof BedWarsGameState
                && ((BedWarsGameState) state).spawnPosition != null
                ? ((BedWarsGameState) state).spawnPosition
                : mc.thePlayer.getPositionVector();
        presetName = null;
        movementTarget = null;
        movementTicks = 0;
        jumping = false;
        jumpDirectionX = jumpDirectionZ = 0;
        searchCooldown.timer.reset();
        searchCooldown.durationMs = 0;
    }

    public static boolean canJump(double x, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        AxisAlignedBB original = player.getEntityBoundingBox();
        AxisAlignedBB next = original.offset(x * 0.35D, 0, z * 0.35D);
        List<AxisAlignedBB> collisions = mc.theWorld.getCollidingBoundingBoxes(player, next);
        if (collisions.isEmpty()) return false;
        double highest = Double.NEGATIVE_INFINITY;
        for (AxisAlignedBB collision : collisions) highest = Math.max(highest, collision.maxY);
        double height = highest - original.minY;
        if (player.stepHeight >= height || height > 1.05D) return false;
        double rise = height + 0.01D;
        if (!mc.theWorld.getCollidingBoundingBoxes(player, original.offset(0, rise, 0), true, false).isEmpty()) return false;
        AxisAlignedBB raised = next.offset(0, rise, 0);
        if (!mc.theWorld.getCollidingBoundingBoxes(player, raised, true, false).isEmpty()) return false;
        return !mc.theWorld.getCollidingBoundingBoxes(player, raised.offset(0, -0.09D, 0), true, false).isEmpty();
    }

    public boolean startJump(MovementInputEvent event, double x, double z) {
        double length = Math.hypot(x, z);
        if (length < 0.001D) return false;
        BedDefenseMoveInput input = movementInput(x / length, z / length);
        if (!canWalk(input.directionX(), input.directionZ())
                && !canJump(input.directionX(), input.directionZ())) return false;
        event.back = input.forward() < 0;
        event.forward = input.forward() > 0;
        event.left = input.strafe() > 0;
        event.right = input.strafe() < 0;
        event.jump = true;
        jumping = true;
        jumpDirectionX = input.directionX();
        jumpDirectionZ = input.directionZ();
        return true;
    }

    public static BedDefenseMoveInput movementInput(double x, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 origin = mc.thePlayer.getPositionVector();
        Vec3 target = origin.addVector(x, 0, z);
        float yaw = RotationUtil.getYawBetween(target, origin).x;
        float difference = MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);
        float forward;
        float strafe;
        if (Math.abs(difference) <= 70.0F) {
            forward = 1.0F;
            strafe = 0.0F;
        } else if (Math.abs(difference) < 110.0F) {
            forward = 0.0F;
            strafe = difference < 0 ? 1.0F : -1.0F;
        } else {
            forward = -1.0F;
            strafe = 0.0F;
        }
        float direction = mc.thePlayer.rotationYaw;
        float strafeAngle = 90.0F;
        if (forward < 0) {
            direction += 180.0F;
            strafeAngle = -45.0F;
        } else if (forward > 0) {
            strafeAngle = 45.0F;
        }
        if (strafe > 0) direction -= strafeAngle;
        if (strafe < 0) direction += strafeAngle;
        double radians = direction * 0.0174532925199433D;
        return new BedDefenseMoveInput(forward, strafe, -Math.sin(radians), Math.cos(radians));
    }

    public boolean jumpToward(MovementInputEvent event, double x, double z) {
        double length = Math.hypot(x, z);
        if (length < 0.001D) return false;
        BedDefenseMoveInput input = movementInput(x / length, z / length);
        if (!canJump(input.directionX(), input.directionZ())) return false;
        event.back = input.forward() < 0;
        event.forward = input.forward() > 0;
        event.left = input.strafe() > 0;
        event.right = input.strafe() < 0;
        event.jump = true;
        jumping = true;
        jumpDirectionX = input.directionX();
        jumpDirectionZ = input.directionZ();
        return true;
    }

    public static boolean isMovementKeyDown() {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        return settings.keyBindForward.isKeyDown() || settings.keyBindLeft.isKeyDown()
                || settings.keyBindBack.isKeyDown() || settings.keyBindRight.isKeyDown();
    }

    public static boolean canJumpInPlace() {
        Minecraft mc = Minecraft.getMinecraft();
        AxisAlignedBB raised = mc.thePlayer.getEntityBoundingBox().offset(0, 0.42D, 0);
        return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, raised).isEmpty();
    }
}
