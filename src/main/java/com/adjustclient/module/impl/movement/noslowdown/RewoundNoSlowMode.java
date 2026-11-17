package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.AttackEntityEvent;
import com.adjustclient.event.ControllerUpdatePhaseEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.ItemSlowdownEvent;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.UserInputVerificationEvent;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule;
import com.adjustclient.module.impl.player.breaker.BreakerModule;
import com.adjustclient.util.WorldUtil;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MovingObjectPosition;

public final class RewoundNoSlowMode extends NoSlowMode {
    public static RewoundNoSlowMode INSTANCE;
    public PacketHold hold;
    public boolean holdSlotChange;
    public boolean blocking;

    public RewoundNoSlowMode(NoSlowdownModule module, Feature feature) {
        super(module, "Rewound", feature);
        INSTANCE = this;
    }

    @EventHandler(priority = 72)
    public void onControllerUpdate(IngameActionEvent event) {
        if (!isActive() || event.suppressActions || mc.thePlayer.ridingEntity != null) return;
        if (InventoryStateTracker.isInServersideGui()) {
            return;
        }
        if (module.getItemType(false) != NoSlowItemType.SWORD) return;
        if (!Adjust.mouseClickHelper.right.isClicked(false)) return;
        if (!BreakerModule.INSTANCE.isBreakingOnRewound() && !WorldUtil.canInteract()) {
            if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
                Adjust.mouseClickHelper.right.disabled = true;
            }
            return;
        }
        acquireHold();
        if (mc.thePlayer.isUsingItem()) {
            mc.playerController.onStoppedUsingItem(mc.thePlayer);
        }
        mc.rightClickDelayTimer = 0;
        blocking = true;
    }

    @EventHandler(priority = 11)
    public void onUseItem(IngameActionEvent event) {
        if (isActive() && blocking && module.getItemType(true) == NoSlowItemType.SWORD
                && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            WorldUtil.clearMouseOver();
        }
    }

    @EventHandler
    public void onUserInput(UserInputVerificationEvent event) {
        if (isActive()) {
            blocking = false;
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (isActive() && !event.pre) {
            releaseHold();
        }
    }

    @EventHandler
    public void onUpdate(ControllerUpdatePhaseEvent event) {
        if (!isActive()) return;
        holdSlotChange = event.pre && hold != null;
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        if (!isActive()) return;
        holdSlotChange = event.pre && hold != null;
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (!isActive()) return;
        event.apply = false;
        event.allowSprinting = true;
        event.ignoreSneakForSprint = true;
    }

    @Override
    public void onDisable() {
        releaseHold();
    }

    public void acquireHold() {
        if (hold != null) return;
        hold = Adjust.outboundPacketQueue.addHold(packet -> packet != null
                && ((holdSlotChange && packet.getClass() == C09PacketHeldItemChange.class)
                || packet.getClass() == C0APacketAnimation.class
                || packet.getClass() == C08PacketPlayerBlockPlacement.class
                || packet.getClass() == C02PacketUseEntity.class), false);
    }

    public void releaseHold() {
        holdSlotChange = false;
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            hold = null;
        }
    }

    @Override
    public boolean removesSlowdown() {
        return false;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
