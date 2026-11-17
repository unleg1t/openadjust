package com.adjustclient.module.impl.player;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.hypixel.game.PixelPartyGameState;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import com.adjustclient.game.player.InventoryStateTracker;

public final class PixelPartyModule extends Module {
    public static PixelPartyModule INSTANCE;
    private BlockPos target;
    private Float rotation;
    private boolean active;

    public PixelPartyModule() { super("Pixel Party", Category.PLAYER); INSTANCE = this; }

    public boolean isTargetBlock() {
        if (!(Adjust.gameStateTracker.state instanceof PixelPartyGameState)) return true;
        IBlockState expected = ((PixelPartyGameState) Adjust.gameStateTracker.state).targetBlock;
        if (expected == null) return true;
        return matchesPattern(expected, mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ)));
    }

    public void onInput() {
        if (isEnabled() && active && !InventoryStateTracker.isInServersideGui()) mc.gameSettings.keyBindForward.pressed = true;
    }

    @EventHandler public void updateTarget(com.adjustclient.event.PreClientTickEvent event) {
        if (!isEnabled()) return;
        if (!(Adjust.gameStateTracker.state instanceof PixelPartyGameState)) {
            target = null; active = false; rotation = null; return;
        }
        if (!(mc.thePlayer.posY >= 0.0) || mc.thePlayer.capabilities.isFlying) {
            active = false; rotation = null; return;
        }
        IBlockState expected = ((PixelPartyGameState) Adjust.gameStateTracker.state).targetBlock;
        if (expected != null) target = findTarget(expected);
        if (isTargetBlock()) { active = false; target = null; rotation = null; }
        else if (target != null) {
            active = true;
            rotation = RotationUtil.getRotations(new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5)).x;
        }
        if (rotation == null || InventoryStateTracker.isInServersideGui()) return;
        Vec2f current = RenderRotationTracker.INSTANCE.rotation;
        RotationHandler.rotate(InstantRotationModel.INSTANCE, rotation,
                current == null ? mc.thePlayer.rotationPitch : current.y);
        RenderRotationTracker.INSTANCE.disabled = true;
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled() || !active || InventoryStateTracker.isInServersideGui()) return;
        event.back = false; event.forward = true; event.left = false; event.right = false; event.jump = false;
        event.sneak = ServerTracker.INSTANCE.field198 && !ServerTracker.INSTANCE.field197;
        if (target == null || !mc.thePlayer.onGround) return;
        Simulation jumping = new Simulation(mc.thePlayer);
        jumping.player.movementInput.moveForward = 1.0F;
        jumping.player.movementInput.moveStrafe = 0.0F;
        jumping.player.movementInput.jump = true;
        for (int i = 0; i < 20; i++) {
            jumping.player.tick();
            if (jumping.player.onGround) break;
        }
        BlockPos landing = new BlockPos(jumping.player.posX, jumping.player.posY + 0.5, jumping.player.posZ);
        if (jumping.player.onGround && landing.getX() == target.getX() && landing.getZ() == target.getZ()) {
            event.jump = true; return;
        }
        Simulation walking = new Simulation(mc.thePlayer);
        walking.player.movementInput.moveForward = 1.0F;
        walking.player.movementInput.moveStrafe = 0.0F;
        walking.player.movementInput.jump = false;
        walking.player.tick();
        if (!walking.player.onGround) event.jump = true;
    }

    public BlockPos findTarget(IBlockState state) {
        BlockPos closest = null;
        double distance = Double.MAX_VALUE;
        for (int x = -32; x < 32; x++) {
            for (int z = -32; z < 32; z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                if (!matchesPattern(state, mc.theWorld.getBlockState(pos))) continue;
                double dx = mc.thePlayer.posX - (x + 0.5);
                double dz = mc.thePlayer.posZ - (z + 0.5);
                double candidateDistance = dx * dx + dz * dz;
                if (distance > candidateDistance) { distance = candidateDistance; closest = pos; }
            }
        }
        return closest;
    }

    public boolean matchesPattern(IBlockState expected, IBlockState actual) {
        if (actual == null || expected.getBlock() != actual.getBlock()) return false;
        Block block = expected.getBlock();
        IProperty<?> property;
        if (block.getClass() == BlockColored.class) property = BlockColored.COLOR;
        else if (block.getClass() == BlockQuartz.class) property = BlockQuartz.VARIANT;
        else if (block.getClass() == BlockPlanks.class) property = BlockPlanks.VARIANT;
        else if (block.getClass() == BlockStainedGlass.class) property = BlockStainedGlass.COLOR;
        else if (block.getClass() == BlockStone.class) property = BlockStone.VARIANT;
        else return true;
        return BlockUtil.hasSameProperties(expected, actual, new IProperty<?>[] { property });
    }

    @Override public void onEnable() { target = null; rotation = null; active = false; }
}
