package com.adjustclient.module.impl.combat.antifireball;

import com.adjustclient.Adjust;
import com.adjustclient.event.AttackInputStageEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.UserInputVerificationEvent;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.util.MovingObjectPosition;

public final class AntiFireballModule extends Module {
    public static AntiFireballModule INSTANCE;
    final AntiFireballSettings settings;
    FireballInterceptSolver solver;

    public AntiFireballModule() {
        super("Anti Fireball", Category.COMBAT);
        INSTANCE = this;
        settings = new AntiFireballSettings(this);
    }

    private boolean unavailable() {
        var blocker = ServerTracker.INSTANCE.serverState;
        if (blocker != null && blocker.antiCheatModel != null
                && blocker.antiCheatModel.serverType == ServerType.HYPIXEL) {
            var game = Adjust.gameStateTracker.state;
            if (game.game != HypixelGame.BED_WARS
                    || game.isSpecialState() != 0 || game.isSpectator()
                    || game.lobby == null
                    || game.lobby.booleanValue()
                    || game.startTimer == null
                    || game.ended) return true;
        }
        if (ScaffoldModule.INSTANCE.isEnabled()
                && ScaffoldModule.INSTANCE.placeReady != null
                && ScaffoldModule.INSTANCE.placeReady.booleanValue()) return true;
        return mc.thePlayer == null || mc.thePlayer.getHealth() <= 0.0F
                || InventoryStateTracker.inServerGui
                || InventoryStateTracker.inServerInventory
                || ServerTracker.INSTANCE.attackedEntity != null
                || ServerTracker.INSTANCE.usedItemSinceMovement
                || ServerTracker.INSTANCE.dugSinceMovement;
    }

    @EventHandler(priority = 52)
    public void onGameLoop(IngameActionEvent event) {
        if (!isEnabled() || event.suppressActions || unavailable()) return;
        FireballAim target = solver.target;
        if (target == null) return;
        Vec2f angles = target.aim.angles();
        RotationHandler.rotate(InstantRotationModel.INSTANCE, angles.x, angles.y);
        if (target != solver.target) return;
        mc.pointedEntity = target.fireball.entity;
        mc.objectMouseOver = new MovingObjectPosition(target.fireball.entity, target.aim.hitVec());
        ServerTracker.INSTANCE.mouseOverOverridden = true;
    }

    @EventHandler(priority = 80)
    public void onTick(AttackInputStageEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (unavailable() || mc.thePlayer.isUsingItem()) {
            solver.target = null;
            return;
        }
        FireballAim target = solver.target;
        if (ServerTracker.INSTANCE.usedItemSinceMovement
                || Adjust.mouseClickHelper.left.disabled
                || target == null || !Adjust.cpsCounter.canClick(settings.cps)) return;
        RotationUtil.forceMouseOverEntity(target.fireball.entity);
        Adjust.mouseClickHelper.left.clicks = 1;
        Adjust.mouseClickHelper.left.down = Boolean.TRUE;
        Adjust.mouseClickHelper.left.holdTicks = 0;
        Adjust.cpsCounter.recordClick();
    }

    @EventHandler(priority = 1)
    public void onControllerUpdate(UserInputVerificationEvent event) {
        if (!isEnabled()) return;
        if (unavailable()) {
            solver.target = null;
            return;
        }
        solver.update();
        if (solver.target != null) event.inputRequested = true;
    }

    @Override public void onEnable() {
        solver = new FireballInterceptSolver(this);
    }

    @Override public void onDisable() {
        solver = null;
    }

    @Override public String getDescription() {
        return "Detects and automatically knocks away any nearby fireballs.";
    }
}
