package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.Adjust;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.util.*;
import java.util.Arrays;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import static com.adjustclient.util.MinecraftInstance.mc;
import com.adjustclient.module.impl.movement.flight.FlightModule;
import com.adjustclient.module.impl.movement.speed.SpeedModule;
import com.adjustclient.module.impl.movement.longjump.LongJumpModule;

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
    public ModeSetting<AntiVoidMode> mode;
    public final Cooldown liquidCheckCooldown;
    public int field76;

    public AntiVoidModule() {
        super("Anti Void", Category.MOVEMENT);
        INSTANCE = this;
        fallDistance = new NumberSetting(this, "Fall Distance", 0.0D, 0.0D, 5.0D, 0.5D);
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
        mode = new ModeSetting<>(this, "Mode", legit, Arrays.asList(legit, blink));
        liquidCheckCooldown = new Cooldown(0L);
        fallDistance.setHideIf(() -> {
            return mode.getValue() != blink;
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
            if (oldMode instanceof LegitAntiVoidMode) ((LegitAntiVoidMode) oldMode).timer.lastMs = 0L;
            else if (oldMode instanceof BlinkAntiVoidMode) {
                BlinkAntiVoidMode blink = (BlinkAntiVoidMode) oldMode;
                blink.safeState = null;
                blink.releasePackets();
                blink.flagTimer = null;
                blink.pendingMotion = null;
                blink.freezeTicks = 0;
                blink.flagCooldownTicks = 0;
            }
            if (newMode instanceof BlinkAntiVoidMode) {
                BlinkAntiVoidMode blink = (BlinkAntiVoidMode) newMode;
                blink.safeState = null;
                blink.releasePackets();
            } else ((LegitAntiVoidMode) newMode).safeSpot = null;
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

    @Override public void onEnable() {
        AntiVoidMode selected = mode.getValue();
        if (selected instanceof BlinkAntiVoidMode) {
            BlinkAntiVoidMode blink = (BlinkAntiVoidMode) selected;
            blink.safeState = null;
            blink.releasePackets();
        } else ((LegitAntiVoidMode) selected).safeSpot = null;
    }

    @Override public void onDisable() {
        AntiVoidMode selected = mode.getValue();
        if (selected instanceof BlinkAntiVoidMode) {
            BlinkAntiVoidMode blink = (BlinkAntiVoidMode) selected;
            blink.safeState = null;
            blink.releasePackets();
            blink.flagTimer = null;
            blink.pendingMotion = null;
            blink.freezeTicks = 0;
            blink.flagCooldownTicks = 0;
        } else ((LegitAntiVoidMode) selected).timer.lastMs = 0L;
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }
}
