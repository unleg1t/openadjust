package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MoveFlyingEvent;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.MovementUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public final class NormalScaffoldMode extends ScaffoldMode {

    boolean placedUp;

    @Override
    public void reset() {
        super.reset();
        this.placedUp = false;
    }

    private boolean canTowerNow() {
        ScaffoldSettings settings = this.module.settings;
        if (!settings.isTower()) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.inWater || player.isInLava()) {
            return false;
        }
        if (player.isPotionActive(Potion.jump)) {
            return false;
        }
        return settings.isTowerKeyDown();
    }

    @EventHandler
    public void onMotion(MoveFlyingEvent e) {
        if (!this.isActive() || e.pre) {
            return;
        }
        if (!this.canTowerNow()) {
            this.towering = false;
            this.placedUp = false;
            return;
        }
        if (this.placedUp) {
            double jumpMotion = MovementUtil.getJumpMotion((double) 0.42F);
            AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().offset(0.0D, jumpMotion, 0.0D);
            if (!BlockUtil.isColliding(box)) {
                mc.thePlayer.motionY = jumpMotion;
            }
            this.towering = true;
        }
        TowerMovement movement = this.module.settings.towerMovement.getValue();
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
        this.placedUp = false;
    }

    @Override
    public void onPlace(PlacementTarget placement) {
        if (!this.canTowerNow()) {
            return;
        }
        if (placement.facing == EnumFacing.UP) {
            this.placedUp = true;
        }
    }

    @Override
    public boolean canSprint() {
        return this.module.settings.isAllowSprint();
    }
}
