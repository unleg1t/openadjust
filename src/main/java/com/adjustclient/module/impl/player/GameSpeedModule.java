package com.adjustclient.module.impl.player;

import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldSettings;
import java.text.DecimalFormat;

public final class GameSpeedModule extends Module {
    public static GameSpeedModule INSTANCE;
    public final ModeSetting<GameSpeedMode> mode;
    public final NumberSetting multiplier;
    private float credit;
    private boolean wasMoving;
    private static final DecimalFormat FORMAT;

    static { FORMAT = new DecimalFormat("#.##"); }

    public GameSpeedModule() {
        super("Game Speed", Category.PLAYER);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", GameSpeedMode.NORMAL, GameSpeedMode.values());
        multiplier = new NumberSetting(this, "Multiplier", 2.0, 0.01, 10.0, 0.01,
                () -> mode.getValue() == GameSpeedMode.HYPIXEL);
    }

    public void updateNormal() {
        float speed = multiplier.getFloat();
        if (speed > 1.0F && mode.getValue() == GameSpeedMode.NORMAL
                && !com.adjustclient.anticheat.feature.Features.GAME_SPEED.isAllowed()) speed = 1.0F;
        if (!(multiplier.getMin().floatValue() < speed)) {
            speed = multiplier.getMin().floatValue();
            multiplier.setValue(speed);
        }
        if (speed > 1.0F) {
        }
        if (speed != 1.0F && mc.timer.timerSpeed == 1.0F) {
            mc.timer.unscaledElapsedPartialTicks = mc.timer.elapsedPartialTicks;
            mc.timer.unscaledElapsedTicks = mc.timer.elapsedTicks;
        }
        mc.timer.timerSpeed = speed;
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass(float speed) {
        if (speed > 1.0F) {
            com.adjustclient.service.util.ServiceUtil.reportFeatureUse(
                    com.adjustclient.service.telemetry.BypassFeature.GAME_SPEED_NORMAL);
        }
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        switch (mode.getValue()) {
            case NORMAL: updateNormal(); break;
            case HYPIXEL: updateHypixel(); break;
        }
    }

    @Override public void onEnable() {
        credit = 0.0F;
        wasMoving = com.adjustclient.util.MovementUtil.isMoving();
    }

    @Override public void onDisable() { mc.timer.timerSpeed = 1.0F; }

    @Override public String getSuffix() {
        return mode.getValue() == GameSpeedMode.NORMAL ? FORMAT.format(multiplier.getDouble()) + "x" : mode.getValue().getName();
    }

    @Override public String getDescription() { return "Adjust the speed your game runs at."; }

    public void updateHypixel() {
        boolean moving = com.adjustclient.util.MovementUtil.isMoving()
                && (!mc.thePlayer.movementInput.sneak || mc.thePlayer.isPlayerSleeping());
        boolean usingBlock = false;
        if (mc.thePlayer != null && mc.gameSettings.keyBindUseItem.isKeyDown()
                && mc.playerController.getCurrentGameType() != WorldSettings.GameType.SPECTATOR) {
            if (mc.playerController.getIsHittingBlock()) usingBlock = true;
            else {
                Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
                MovingObjectPosition hit = WorldUtil.pickFromPlayer(false,
                        rotation == null ? mc.thePlayer.rotationYaw : rotation.x,
                        rotation == null ? mc.thePlayer.rotationPitch : rotation.y,
                        mc.playerController.getCurrentGameType() == WorldSettings.GameType.CREATIVE ? 5.0 : 4.5);
                usingBlock = hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
            }
        }
        float speed = 1.0F;
        if (mc.thePlayer.onGround && !moving && credit != 2.6F && !usingBlock) {
            speed = wasMoving ? 0.45F : 0.75F;
            wasMoving = false;
            credit += 1.0F - speed;
        } else if (moving && !usingBlock && credit > 0.0F
                && (!mc.thePlayer.movementInput.sneak || mc.thePlayer.isPlayerSleeping())) {
            float spent = Math.min(credit, 0.3F);
            credit = Math.max(credit - spent, 0.0F);
            speed = spent * 3.0F + 1.0F;
        }
        if (moving) wasMoving = true;
        credit = Math.min(credit, 2.6F);
        var state = ServerTracker.INSTANCE.serverState;
        if (state == null || state.antiCheatModel == null
                || state.antiCheatModel.serverType != ServerType.HYPIXEL
                || !state.antiCheatModel.isAllChecksPassed()) return;
        if (speed != 1.0F && mc.timer.timerSpeed == 1.0F) {
            mc.timer.unscaledElapsedPartialTicks = mc.timer.elapsedPartialTicks;
            mc.timer.unscaledElapsedTicks = mc.timer.elapsedTicks;
        }
        mc.timer.timerSpeed = speed;
    }

    public enum GameSpeedMode implements com.adjustclient.setting.ModeOption {
        NORMAL("Normal"), HYPIXEL("Hypixel");
        private final String displayName;
        GameSpeedMode(String displayName) { this.displayName = displayName; }
        public String getName() { return displayName; }
    }
}
