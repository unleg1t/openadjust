package com.adjustclient.modules.impl.movement.antivoid;

import com.adjustclient.Adjust;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.movement.flight.FlightModule;
import com.adjustclient.modules.impl.movement.highjump.HighJumpModule;
import com.adjustclient.modules.impl.movement.longjump.LongJumpModule;
import com.adjustclient.modules.impl.movement.speed.SpeedModule;
import com.adjustclient.core.settings.KeybindSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.Cooldown;
import com.adjustclient.utilities.WorldUtil;
import java.util.Arrays;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public final class AntiVoidModule extends Module {
    public static AntiVoidModule INSTANCE;
    public final NumberSetting fallDistance;
    public final NumberSetting voidDistance;
    public final KeybindSetting voidDistOverrideKey;
    public final NumberSetting voidDistOverride;
    public final NumberSetting fallSimulations;
    public final KeybindSetting disableBind;
    public BlinkAntiVoidMode blink;
    public final LegitAntiVoidMode legit;
    public final PacketAntiVoidMode packet;
    public ModeSetting<AntiVoidMode> mode;
    public final Cooldown liquidCheckCooldown;
    public int field76;

    public AntiVoidModule() {
        super("Anti Void", Category.MOVEMENT);
        INSTANCE = this;
        fallDistance = new NumberSetting(this, "Fall Distance", 0.0D, 0.0D, 10.0D, 0.5D);
        fallDistance.setDescription("Distance to fall until you get saved.");
        voidDistance = new NumberSetting(this, "Void Distance", 41.0D, 2.0D, 41.0D, 1.0D);
        voidDistOverrideKey = new KeybindSetting(this, "Void Dist. Override Key",
                Adjust.INSTANCE.bindManager.register("antivoid.voiddistoverride",
                        mode -> {
                        }, "Void Distance Override Key"));
        voidDistOverride = new NumberSetting(this, "Void Dist. Override", 41.0D, 2.0D, 41.0D, 1.0D,
                () -> {
                    return voidDistOverrideKey.getValue().keys.isEmpty();
                });
        fallSimulations = new NumberSetting(this, "Fall Simulations", 1.0D, 0.0D, 7.0D, 1.0D);
        fallSimulations.setDescription("Simulates movement and predicts if you will actually fall before starting to blink.");
        disableBind = new KeybindSetting(this, "Disable Bind",
                Adjust.INSTANCE.bindManager.register("module.antivoid.disablebind",
                        mode -> {
                        }, "Anti Void Disable Bind"));
        disableBind.setDescription("Anti Void will temporarily stop working when this key is held.");
        blink = new BlinkAntiVoidMode(this);
        legit = new LegitAntiVoidMode(this);
        packet = new PacketAntiVoidMode(this);
        mode = new ModeSetting<>(this, "Mode", legit, Arrays.asList(legit, blink, packet));
        liquidCheckCooldown = new Cooldown(0L);
        fallDistance.setHideIf(() -> {
            return mode.getValue() != blink && mode.getValue() != packet;
        });
        fallSimulations.setHideIf(() -> {
            return mode.getValue() != blink;
        });
        NumberSetting.Formatter distanceFormatter = setting -> {
            return setting.getInt() == setting.getMax().intValue() ? "Void" : NumberSetting.formatMeters(setting);
        };
        voidDistance.formatter(distanceFormatter);
        voidDistOverride.formatter(distanceFormatter);
        fallSimulations.formatter(setting -> {
            return setting.getInt() == setting.getMin().intValue() ? "None" : NumberSetting.FORMAT.format(setting.getInt());
        });
        fallDistance.formatter(NumberSetting.METERS);
        mode.addListener((oldMode, newMode) -> {
            if (oldMode != null) ((AntiVoidMode) oldMode).onDisable();
            if (newMode != null) ((AntiVoidMode) newMode).onEnable();
        });
        settings.remove(mode);
        settings.add(0, mode);
    }

    public static boolean isWorldReady() {
        EntityPlayerSP player = mc.thePlayer;
        return mc.theWorld.isBlockLoaded(new BlockPos(player.posX + 0.5D, player.posY + 0.5D,
                player.posZ + 0.5D)) && player.ticksExisted >= 11;
    }

    public boolean shouldIgnore() {
        if (!disableBind.getValue().keys.isEmpty() && disableBind.isDown()) return true;
        if (!isWorldReady()) return true;
        EntityPlayerSP player = mc.thePlayer;
        return player.isDead || player.getHealth() <= 0.0F || player.capabilities.isFlying || player.noClip
                || Adjust.gameStateTracker.state.isSpecialState() != 0
                || Boolean.TRUE.equals(Adjust.rewoundGameTracker.state.lobby)
                || Adjust.rewoundGameTracker.state.allowFlying
                || HighJumpModule.INSTANCE.isEnabled()
                || LongJumpModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled()
                || mode.getValue() instanceof LegitAntiVoidMode && SpeedModule.INSTANCE.isEnabled();
    }

    public boolean isFullVoidDistance() {
        NumberSetting setting = !voidDistOverrideKey.getValue().keys.isEmpty() && voidDistOverrideKey.isDown()
                ? voidDistOverride : voidDistance;
        return setting.getInt() == setting.getMax().intValue();
    }

    public double getCheckBottom(double y) {
        NumberSetting setting = !voidDistOverrideKey.getValue().keys.isEmpty() && voidDistOverrideKey.isDown()
                ? voidDistOverride : voidDistance;
        return setting.getInt() == setting.getMax().intValue() ? 0.0D : Math.max(0.0D, y - (setting.getDouble() - 0.6D));
    }

    public boolean isOverVoid(Vec3 feet) {
        double bottom = getCheckBottom(feet.yCoord);
        float halfWidth = mc.thePlayer.width * mode.getValue().collisionWidth;
        return liquidCheckCooldown.isExpired()
                ? WorldUtil.isColumnCollisionFree(feet, bottom, halfWidth)
                : WorldUtil.isColumnFreeOfLiquidAndClimbables(feet, bottom, halfWidth);
    }

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
    }

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
    }

    @Override
    public String getSuffix() {
        return mode.getValue().getName();
    }
}
