package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.BlockUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public final class NormalScaffoldMode extends ScaffoldMode {

    /** Set by {@link #onPlace} when a block was placed on the UP face (the player towered); consumed next tick. */
    boolean towering;

    @Override
    public boolean isAboveBuildLimit(int y) {
        return y >= mc.theWorld.getHeight();   // 256
    }

    @com.adjustclient.event.EventHandler
    public void onMotion(MoveFlyingEvent e) {
        if (!this.isActive() || e.pre) {          // *(byte *)(e + 5)
            return;
        }
        ScaffoldSettings settings = this.module.settings;
        boolean active = false;
        if (settings.towerEnabled.isEnabled()
                && (((ScaffoldMode) settings.mode.getValue()).capabilities & 8) != 0) {
            EntityPlayerSP player = mc.thePlayer;
            if (!(player.inWater || player.isInLava())) {
                if (!player.isPotionActive(Potion.jump) && settings.isTowerKeyDown()) {
                    active = true;
                }
            }
        }
        if (!active) {
            this.module.towering = false;
            this.towering = false;
            return;
        }
        if (this.towering) {
            // jump motion (0.42 plus jump boost), only if there is room above the player
            double jumpMotion = MovementUtil.getJumpMotion((double) 0.42F);
            AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().offset(0.0D, jumpMotion, 0.0D);
            if (!BlockUtil.isColliding(box)) {
                mc.thePlayer.motionY = jumpMotion;
            }
            this.module.towering = true;
        }
        TowerMovement movement = (TowerMovement) settings.towerMovement.getValue();
        switch (movement) {
            case STOP:
                MovementUtil.stop();
                break;
            case CENTER:
                MovementUtil.center(1.0D);
                break;
            default:
                break;
        }
        this.towering = false;
    }

    /**
     * Remembers that the player towers when a block is placed on the UP face while the tower conditions of
     * {@link #onMotion} hold.
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
        if (player.isPotionActive(Potion.jump)) {
            return;
        }
        if (settings.isTowerKeyDown()) {
            if (placement.facing == EnumFacing.UP) {
                this.towering = true;
            }
        }
    }

    @Override
    public boolean canSprint() {
        return this.module.settings.isAllowSprint();
    }

    @Override
    public boolean e() {
        return false;
    }
}
