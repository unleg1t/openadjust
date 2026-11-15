package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.module.impl.movement.speed.SpeedModule;
import com.adjustclient.module.util.CpsSettings;
import com.adjustclient.rotation.RotationMode;
import com.adjustclient.rotation.RotationSetting;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import net.minecraft.client.Minecraft;

public final class ScaffoldSettings {

    public static ScaffoldMode NORMAL;
    public static ScaffoldMode HYPIXEL;
    public static ScaffoldMode NCP;

    public final ModeSetting<ScaffoldMode> mode;
    public final ScaffoldMode hypixel;
    public final CpsSettings cps;
    public final BooleanSetting rotationsEnabled;
    public final RotationSetting rotation;
    public final BooleanSetting snap;
    public final BooleanSetting stableYaw;
    public final BooleanSetting raycast;
    public final BooleanSetting clientsideSwing;
    public final BooleanSetting clientsideSwap;
    public final BooleanSetting changeSlot;
    public final BooleanSetting waitForClick;
    public final NumberSetting extend;
    public final BooleanSetting autoStasis;
    public final BooleanSetting safeWalkEnabled;
    public final BooleanSetting safeWalkSneak;
    public final NumberSetting unSneakDelay;
    public final NumberSetting earlySneak;
    public final BooleanSetting allowSprintEnabled;
    public final BooleanSetting noAscendEnabled;
    public final ModeSetting<NoAscendMode> noAscendMode;
    public final Bind noAscendBind;
    public final BooleanSetting autoJump;
    public final BooleanSetting ascendOnJump;
    public final BooleanSetting towerEnabled;
    public final Bind towerBind;
    public final ModeSetting<TowerMovement> towerMovement;
    public final BlockCountHud blockCountHud;
    public final BooleanSetting blockPlacements;

    public ScaffoldSettings(ScaffoldModule module) {
        NORMAL = new NormalScaffoldMode();
        NORMAL.module = module;
        NORMAL.name = "Normal";
        NORMAL.capabilities = 62;
        HYPIXEL = this.hypixel = new HypixelScaffoldMode();
        HYPIXEL.module = module;
        HYPIXEL.name = "Hypixel";
        HYPIXEL.capabilities = 14;
        NCP = new NcpScaffoldMode();
        NCP.module = module;
        NCP.name = "NCP";
        NCP.capabilities = 62;

        this.mode = new ModeSetting<>(module, "Mode", NORMAL, NORMAL, HYPIXEL, NCP);
        this.mode.addListener(() -> module.onModeChange());

        this.cps = new CpsSettings(module, 9, 13);

        // ---- Rotations
        ExpandSetting rotations = new ExpandSetting(module, "Rotations");
        rotations.setActiveSupplier(this::isRotationsEnabled);
        this.rotationsEnabled = new BooleanSetting(rotations, "Enabled", true, "Yes", "No");
        this.rotation = new RotationSetting(rotations, RotationMode.STEP, null, RotationMode.values());
        this.snap = new BooleanSetting(rotations, "Snap", false);
        this.stableYaw = new BooleanSetting(rotations, "Stable yaw", true)
                .description("Keeps your yaw stable when going flat.");
        this.raycast = new BooleanSetting(rotations, "Raycast", true)
                .description("Makes sure your final rotations still hit the block before placing.");

        // ---- Addons
        ExpandSetting addons = new ExpandSetting(module, "Addons");
        ExpandSetting clientside = new ExpandSetting(addons, "Clientside");
        clientside.setActiveSupplier(() -> this.isClientsideSwap() || this.isClientsideSwing());
        this.clientsideSwing = new BooleanSetting(clientside, "Swing", true)
                .description("Whether to show swings coming from Scaffold (other players will still see you swing).");
        this.clientsideSwap = new BooleanSetting(clientside, "Swap", false)
                .description("Whether to show your server held item clientside while using Scaffold.");
        this.changeSlot = new BooleanSetting(addons, "Change slot", true)
                .description("Automatically change your held item to a block.");
        this.waitForClick = new BooleanSetting(addons, "Wait for click", false)
                .description("Wait for a fake click on the existing block.");
        this.extend = new NumberSetting(addons, "Extend", 0, 0, 3, 1,
                () -> (((ScaffoldMode) this.mode.getValue()).capabilities & 0x20) == 0);
        this.autoStasis = new BooleanSetting(addons, "Auto stasis", false)
                .description("Automatically toggles Stasis if a block fails to place.");

        ExpandSetting safeWalk = new ExpandSetting(addons, "Safe walk");
        safeWalk.setActiveSupplier(this::isSafeWalk);
        this.safeWalkEnabled = new BooleanSetting(safeWalk, "Enabled", false, "Yes", "No");
        this.safeWalkSneak = new BooleanSetting(safeWalk, "Sneak", false);
        this.unSneakDelay = new NumberSetting(safeWalk, "Un-sneak delay", 0, 0, 5, 1,
                () -> !this.safeWalkSneak.isEnabled()).formatter(NumberSetting.TICKS);
        this.earlySneak = new NumberSetting(safeWalk, "Early sneak", 0, 0, 5, 1,
                () -> !this.safeWalkSneak.isEnabled()).formatter(NumberSetting.TICKS);

        ExpandSetting allowSprint = new ExpandSetting(addons, "Allow sprint",
                () -> (((ScaffoldMode) this.mode.getValue()).capabilities & 0x2) == 0);
        allowSprint.setActiveSupplier(this::isAllowSprint);
        this.allowSprintEnabled = new BooleanSetting(allowSprint, "Enabled", true, "Yes", "No");

        ExpandSetting noAscend = new ExpandSetting(addons, "No ascend",
                () -> (((ScaffoldMode) this.mode.getValue()).capabilities & 0x4) == 0);
        noAscend.setActiveSupplier(this::isNoAscend);
        this.noAscendEnabled = new BooleanSetting(noAscend, "Enabled", false, "Yes", "No");
        this.noAscendMode = new ModeSetting<>(noAscend, "Mode", NoAscendMode.FLAT, NoAscendMode.FLAT, NoAscendMode.BUFFER);
        this.noAscendBind = Adjust.INSTANCE.bindManager.register("module.scaffold.noascendbind", mode -> {
        }, "No Ascend Bind");
        new KeybindSetting(noAscend, "Bind", this.noAscendBind);
        this.autoJump = new BooleanSetting(noAscend, "Auto jump", true);
        this.ascendOnJump = new BooleanSetting(noAscend, "Ascend on jump", true,
                () -> !(this.mode.getValue() instanceof HypixelScaffoldMode));

        ExpandSetting tower = new ExpandSetting(addons, "Tower",
                () -> (((ScaffoldMode) this.mode.getValue()).capabilities & 0x8) == 0);
        tower.setActiveSupplier(this::isTower);
        this.towerEnabled = new BooleanSetting(tower, "Enabled", false, "Yes", "No");
        this.towerBind = Adjust.INSTANCE.bindManager.register("module.scaffold.towerbind", mode -> {
        }, "Tower Bind");
        new KeybindSetting(tower, "Bind", this.towerBind);
        this.towerMovement = new ModeSetting<>(tower, "Movement", TowerMovement.NORMAL,
                () -> (((ScaffoldMode) this.mode.getValue()).capabilities & 0x10) == 0,
                java.util.List.of(TowerMovement.NORMAL, TowerMovement.STOP, TowerMovement.CENTER));

        // ---- Render
        ExpandSetting render = new ExpandSetting(module, "Render");
        this.blockCountHud = new BlockCountHud(module, render);
        this.blockPlacements = new BooleanSetting(render, "Block placements", false);
    }

    /**
     * No ascend is currently active (keeps the player from going up). {@code force} is OR-ed in.
     * Without a bind, it is active while jump is not held; Speed being enabled also activates it.
     */
    public boolean isNoAscendActive(boolean force) {
        if (!this.isNoAscend()) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.keyBindJump.pressed) {
            return false;
        }
        return this.noAscendBind.isDown()
                || force
                || this.noAscendBind.getKeys().isEmpty() && !mc.gameSettings.keyBindJump.pressed
                || SpeedModule.INSTANCE.isEnabled();
    }

    public int getExtend() {
        if ((((ScaffoldMode) this.mode.getValue()).capabilities & 0x20) == 0) {
            return 0;
        }
        return this.extend.getValue().intValue();
    }

    public boolean isAllowSprint() {
        return this.allowSprintEnabled.isEnabled() && (((ScaffoldMode) this.mode.getValue()).capabilities & 0x2) != 0;
    }

    public boolean isNoAscend() {
        return this.noAscendEnabled.isEnabled() && (((ScaffoldMode) this.mode.getValue()).capabilities & 0x4) != 0;
    }

    public boolean isTower() {
        return this.towerEnabled.isEnabled() && (((ScaffoldMode) this.mode.getValue()).capabilities & 0x8) != 0;
    }

    /** Tower key: the Tower bind if one is set, otherwise the jump key. */
    public boolean isTowerKeyDown() {
        if (this.towerBind.getKeys().isEmpty()) {
            return Minecraft.getMinecraft().gameSettings.keyBindJump.pressed;
        }
        return this.towerBind.isDown();
    }

    public boolean isRotationsEnabled() {
        return this.rotationsEnabled.isEnabled();
    }

    public RotationSetting getRotation() {
        return this.rotation;
    }

    public boolean isSnap() {
        return this.snap.isEnabled();
    }

    public boolean isStableYaw() {
        return this.stableYaw.isEnabled();
    }

    public boolean isRaycast() {
        return this.raycast.isEnabled();
    }

    public boolean isClientsideSwing() {
        return this.clientsideSwing.isEnabled();
    }

    public boolean isClientsideSwap() {
        return this.clientsideSwap.isEnabled();
    }

    public boolean isChangeSlot() {
        return this.changeSlot.isEnabled();
    }

    public boolean isWaitForClick() {
        return this.waitForClick.isEnabled();
    }

    public boolean isAutoStasis() {
        return this.autoStasis.isEnabled();
    }

    public boolean isSafeWalk() {
        return this.safeWalkEnabled.isEnabled();
    }

    public boolean isSafeWalkSneak() {
        return this.safeWalkSneak.isEnabled();
    }

    public int getUnSneakDelay() {
        return this.unSneakDelay.getValue().intValue();
    }

    public int getEarlySneak() {
        return this.earlySneak.getValue().intValue();
    }

    public boolean isAutoJump() {
        return this.autoJump.isEnabled();
    }

    public boolean isAscendOnJump() {
        return this.ascendOnJump.isEnabled();
    }

    public boolean isBlockPlacements() {
        return this.blockPlacements.isEnabled();
    }
}
