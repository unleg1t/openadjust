package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.module.impl.movement.speed.SpeedModule;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import com.adjustclient.Adjust;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.util.MovementUtil;

public final class NcpScaffoldMode extends ScaffoldMode {

    @Override
    public boolean isAboveBuildLimit(int y) {
        return y >= mc.theWorld.getHeight();   // 256
    }

    @com.adjustclient.event.EventHandler
    public void onTowerStart(com.adjustclient.event.JumpEvent event) {
        if (!this.isActive()) {
            return;
        }
        ScaffoldSettings settings = this.module.settings;
        if (!settings.towerEnabled.isEnabled()) {
            return;
        }
        if ((((ScaffoldMode) settings.mode.getValue()).capabilities & 8) == 0) {
            return;
        }
        if (SpeedModule.INSTANCE.isEnabled()) {                    // Speed module
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if ((int) player.posY >= 256) {
            return;
        }
        if (player.isPotionActive(Potion.jump)) {
            return;
        }
        if (this.module.noAscendActive) {
            return;
        }
        if (Adjust.movementPredictor.historyTicks < 61 || this.module.towering) {
            return;
        }
        if (!player.onGround) {
            return;
        }
        if (settings.isTowerKeyDown()) {
            event.cancelled = true;
        }
    }

    /**
     * While towering, lets the motion packet claim ground contact on the third tick off the ground
     * (offGroundTicks == 3, y at a whole number).
     */
    @com.adjustclient.event.EventHandler
    public void onMotion(MotionEvent e) {
        if (!this.isActive()) {
            return;
        }
        if (e.pre) {
            if (this.module.towering) {
                if (e.y % 1.0D == 0.0D) {
                    if (mc.thePlayer.offGroundTicks == 3) {
                        e.onGround = true;
                    }
                }
            }
        }
    }

    @com.adjustclient.event.EventHandler
    public void onMotionTower(MoveFlyingEvent e) {
        if (!this.isActive() || e.pre) {
            return;
        }
        ScaffoldModule module = this.module;
        Boolean ready = module.placeReady;
        if (ready != null && ready) {
            EntityPlayerSP player = mc.thePlayer;
            if (player.onGroundTicks >= 2 && player.movementInput == null) {
                throw new NullPointerException();
            }
        }
        boolean canTower = this.m();
        if (!canTower) {
            module.towering = false;
            return;
        }
        boolean keyDown = module.settings.isTowerKeyDown();
        if (!module.towering) {
            EntityPlayerSP player = mc.thePlayer;
            if (player.onGround && keyDown) {
                module.field130 = false;
                player.motionY = MovementUtil.getJumpMotion((double) 0.42F);
                module.towering = true;
            }
        } else if (!keyDown) {
            module.towering = false;
            return;
        } else {
            EntityPlayerSP player = mc.thePlayer;
            if (!player.onGround && !player.isCollidedVertically) {
                if (player.offGroundTicks == 2) {
                    double frac = player.posY - (int) player.posY;
                    if (frac > 1.0D - frac) {
                        player.motionY = 1.0D - frac;
                    }
                }
            } else {
                module.towering = false;
                return;
            }
        }
        if (!module.towering) {
            return;
        }
        TowerMovement movement = (TowerMovement) module.settings.towerMovement.getValue();
        EntityPlayerSP player = mc.thePlayer;
        switch (movement) {
            case NORMAL:
                if (player.offGroundTicks < 2) {
                    MovementUtil.setSpeed(MovementUtil.getSpeedWithPotion(0.2872D, 0.2D));
                }
                break;
            case STOP:
                MovementUtil.stop();
                break;
            case CENTER:
                if (player.offGroundTicks >= 2) {
                    MovementUtil.stop();
                } else {
                    MovementUtil.center(MovementUtil.getSpeedWithPotion(0.2872D, 0.2D));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Ground snap after a tower block was placed: if the block that was just placed is the one directly below the
     * player's feet and the player's box touches it, the player is put onto it (motionY = -0.0784, onGround, off-ground
     * counter reset).
     */
    @Override
    public void onPlace(PlacementTarget placement) {
        ScaffoldSettings settings = this.module.settings;
        if (!settings.towerEnabled.isEnabled()) {
            return;
        }
        if ((((ScaffoldMode) settings.mode.getValue()).capabilities & 8) == 0) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.inWater || player.isInLava()) {
            return;
        }
        BlockPos placed = placement.pos.offset(placement.facing);
        if (placed.getY() != (int) player.posY - 1) {
            return;
        }
        AxisAlignedBB feet = player.getEntityBoundingBox().addCoord(0.0D, -0.0625D, 0.0D);
        AxisAlignedBB blockBox = new AxisAlignedBB(placed, placed.add(1, 1, 1));
        if (!feet.intersectsWith(blockBox) || !this.n()) {
            return;
        }
        if (this.module.towering) {
            player.motionY = (double) -0.0784F;
            player.onGround = true;
            player.offGroundTicks = 0;
            this.module.towering = false;
        }
    }

    /**
     * Sprint rule: while on the ground sprinting is allowed when the "Allow sprint" addon is on and the game counter is
     * &gt;= 41, or when jump is held, or when the player moves within 45 degrees of the look direction.
     */
    @Override
    public boolean canSprint() {
        EntityPlayerSP player = mc.thePlayer;
        if (!player.onGround) {
            return false;
        }
        ScaffoldSettings settings = this.module.settings;
        if (settings.mode.getValue() == this) {
            if (settings.isAllowSprint() && Adjust.movementPredictor.historyTicks >= 41) {
                return true;
            }
        }
        GameSettings gs = mc.gameSettings;
        if (gs.keyBindJump.pressed) {
            return true;
        }
        if (player.movementInput != null && player.movementInput.jump) {
            return true;
        }
        if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory) {
            return false;
        }
        float yaw = RenderRotationTracker.INSTANCE.rotation != null
                ? RenderRotationTracker.INSTANCE.rotation.x
                : player.rotationYaw;
        float forward = (gs.keyBindForward.pressed ? 1.0F : 0.0F) - (gs.keyBindBack.pressed ? 1.0F : 0.0F);
        float strafe = (gs.keyBindLeft.pressed ? 1.0F : 0.0F) - (gs.keyBindRight.pressed ? 1.0F : 0.0F);
        float angle;
        if (forward < 0.0F) {
            yaw += 180.0F;
            angle = -45.0F;
        } else if (forward > 0.0F) {
            angle = 45.0F;
        } else {
            angle = 90.0F;
        }
        if (strafe > 0.0F) {
            yaw -= angle;
        }
        if (strafe < 0.0F) {
            yaw += angle;
        }
        float diff = MathHelper.wrapAngleTo180_float(player.rotationYaw - yaw);
        return Math.abs(diff) <= 45.0F;
    }

    @Override
    public boolean e() {
        return true;
    }

    /**
     * Tower is possible right now: Tower addon on (and supported by the mode), Speed module off, below the build limit,
     * no jump boost, no No-ascend, and the game counter is at least 61.
     */
    private boolean m() {
        ScaffoldSettings settings = this.module.settings;
        if (!settings.isTower()) {
            return false;
        }
        if (SpeedModule.INSTANCE.isEnabled()) {           // Speed module
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        if ((int) player.posY >= 256) {
            return false;
        }
        if (player.isPotionActive(Potion.jump)) {
            return false;
        }
        if (this.module.noAscendActive) {
            return false;
        }
        return Adjust.movementPredictor.historyTicks >= 61;
    }

    /** {@link #m()} and the tower key (Tower bind, or jump when no bind is set) is held. */
    private boolean n() {
        return this.m() && this.module.settings.isTowerKeyDown();
    }
}
