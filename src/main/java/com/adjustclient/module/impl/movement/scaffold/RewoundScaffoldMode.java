package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.util.MovementUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public final class RewoundScaffoldMode extends ScaffoldMode {

    public static RewoundScaffoldMode INSTANCE;

    public RewoundScaffoldMode() {
        INSTANCE = this;
    }

    public boolean shouldAutoJump() {
        return this.isActive()
                && this.canTower()
                && !this.towering
                && mc.thePlayer.onGround
                && this.module.settings.isTowerKeyDown();
    }

    @EventHandler
    public void onMotion(MotionEvent e) {
        if (!this.isActive()) {
            return;
        }
        if (e.pre && this.towering && e.y % 1.0D == 0.0D && mc.thePlayer.offGroundTicks == 3) {
            e.onGround = true;
        }
    }

    @EventHandler
    public void onMotionTower(MoveFlyingEvent e) {
        if (!this.isActive() || e.pre) {
            return;
        }
        if (!this.canTower()) {
            this.towering = false;
            return;
        }
        ScaffoldModule module = this.module;
        boolean keyDown = module.settings.isTowerKeyDown();
        if (!this.towering) {
            EntityPlayerSP player = mc.thePlayer;
            if (player.onGround && keyDown) {
                module.field130 = false;
                player.motionY = MovementUtil.getJumpMotion((double) 0.42F);
                this.towering = true;
            }
        } else if (!keyDown) {
            this.towering = false;
            return;
        } else {
            EntityPlayerSP player = mc.thePlayer;
            if (!player.onGround && !player.isCollidedVertically) {
                if (player.offGroundTicks == 2) {
                    double frac = player.posY - MathHelper.floor_double(player.posY);
                    if (frac > 1.0D - frac) {
                        player.motionY = 1.0D - frac;
                    }
                }
            } else {
                this.towering = false;
                return;
            }
        }
        if (!this.towering) {
            return;
        }
        TowerMovement movement = module.settings.towerMovement.getValue();
        switch (movement) {
            case NORMAL:
                MovementUtil.setSpeed(MovementUtil.getSpeedWithPotion(0.2873D, 0.2D));
                break;
            case STOP:
                MovementUtil.stop();
                break;
            case CENTER:
                MovementUtil.center(MovementUtil.getSpeedWithPotion(0.2873D, 0.2D));
                break;
            default:
                break;
        }
    }

    @Override
    public void onPlace(PlacementTarget placement) {
        if (!this.module.settings.isTower()) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.inWater || player.isInLava()) {
            return;
        }
        BlockPos placed = placement.pos.offset(placement.facing);
        if (placed.getY() != MathHelper.floor_double(player.posY) - 1) {
            return;
        }
        AxisAlignedBB feet = player.getEntityBoundingBox().addCoord(0.0D, -0.0625D, 0.0D);
        AxisAlignedBB blockBox = new AxisAlignedBB(placed, placed.add(1, 1, 1));
        if (!feet.intersectsWith(blockBox) || !this.shouldTower()) {
            return;
        }
        if (this.towering) {
            player.motionY = (double) -0.0784F;
            player.onGround = true;
            player.offGroundTicks = 0;
            this.towering = false;
        }
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    public boolean canTower() {
        if (!this.module.settings.isTower()) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            return false;
        }
        if (MathHelper.floor_double(player.posY) >= 256) {
            return false;
        }
        if (player.isPotionActive(Potion.jump)) {
            return false;
        }
        if (this.module.noAscendActive) {
            return false;
        }
        return Adjust.movementPredictor.historyTicks >= 11;
    }

    public boolean shouldTower() {
        return this.canTower() && this.module.settings.isTowerKeyDown();
    }
}
