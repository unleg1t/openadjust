package com.adjustclient.modules.impl.combat.autoblock.mode;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.CancellableInputActionStageEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.IngameActionEvent;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.impl.combat.autoblock.AutoBlockMode;
import com.adjustclient.modules.impl.combat.autoblock.AutoBlockModule;
import com.adjustclient.modules.impl.player.breaker.BreakerModule;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.WorldUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.entity.item.EntityArmorStand;

public final class FullAutoBlockMode extends AutoBlockMode {
    public static FullAutoBlockMode INSTANCE;
    private Integer holdTicks;
    private boolean blocking;

    public FullAutoBlockMode(AutoBlockModule module) {
        super(module, "Full");
        INSTANCE = this;
    }

    @EventHandler(priority = 82)
    public void onTick(IngameActionEvent event) {
        if (event.suppressActions) return;
        if (!isEnabled()) return;
        blocking = false;
        if (mc.thePlayer == null) {
            holdTicks = null;
            return;
        }
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

    @EventHandler
    public void onUseItemTick(CancellableInputActionStageEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.thePlayer == null || !blocking) return;
        var use = Adjust.mouseClickHelper.right;
        if (!use.isClicked(false)) return;
        var server = ServerTracker.INSTANCE.serverState;
        boolean breakingOnHypixel = server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == ServerType.HYPIXEL
                && BreakerModule.INSTANCE.isEnabled()
                && BreakerModule.INSTANCE.handler.target != null;
        if (!WorldUtil.canInteract() || breakingOnHypixel) {
            use.clicks = null;
            use.down = false;
            use.holdTicks = 0;
            use.key.pressTime = 0;
            use.key.pressed = false;
            return;
        }
        if (Adjust.gameStateTracker.state.game == HypixelGame.HOUSING
                && mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityArmorStand) {
            WorldUtil.clearMouseOver();
        }
    }
}
