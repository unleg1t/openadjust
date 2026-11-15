package com.adjustclient.module.impl.combat.autoblock.mode;

import com.adjustclient.Adjust;
import com.adjustclient.event.CancellableInputActionStageEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.impl.combat.autoblock.AutoBlockMode;
import com.adjustclient.module.impl.combat.autoblock.AutoBlockModule;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.WorldUtil;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.MovingObjectPosition;

public final class FullAutoBlockMode extends AutoBlockMode {
    public static FullAutoBlockMode INSTANCE;
    private Integer holdTicks;
    private boolean blocking;
    public FullAutoBlockMode(AutoBlockModule module) { super(module, "Full"); INSTANCE = this; }

    @EventHandler(priority = 82) public void onTick(IngameActionEvent event) {
        if (event.suppressActions) return;
        // the hub only calls this while the mode is enabled (AutoBlockMode.isEnabled gate at :3826): nothing is reset
        // while disabled
        if (!isEnabled()) return;
        blocking = false;
        if (mc.thePlayer == null) { holdTicks = null; return; }
        var use = Adjust.mouseClickHelper.right;
        if (!module.shouldBlock() || use.disabled) {
            if (holdTicks != null) {
                if (module.hasSword(true)) {
                    holdTicks--;
                    if (holdTicks == 0) holdTicks = null;
                } else {
                    if (!use.previousDown) use.clicks = 1;
                    use.down = true;
                    use.holdTicks = 0;
                    holdTicks = null;
                }
            }
            return;
        }
        if (holdTicks == null) holdTicks = RandomUtil.nextInt(RandomUtil.RANDOM, 2, 4);
        blocking = true;
        if (module.hasSword(false)) use.clicks = 1;
        else if (!use.previousDown) use.clicks = 1;
        use.down = true;
        use.holdTicks = holdTicks;
    }

    @EventHandler public void onUseItemTick(CancellableInputActionStageEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.thePlayer == null || !blocking) return;
        var use = Adjust.mouseClickHelper.right;
        if (!use.isClicked(false)) return;
        if (!WorldUtil.canInteract()) {
            use.clicks = null;
            use.down = false;
            use.holdTicks = 0;
            use.key.pressTime = 0;
            use.key.pressed = false;
            return;
        }
        if (Adjust.gameStateTracker.state.game == HypixelGame.HOUSING
                && mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityArmorStand) {
            mc.objectMouseOver.typeOfHit = MovingObjectPosition.MovingObjectType.MISS;
            mc.pointedEntity = null;
            com.adjustclient.util.anticheat.ServerTracker.INSTANCE.mouseOverOverridden = true;
        }
    }
}
