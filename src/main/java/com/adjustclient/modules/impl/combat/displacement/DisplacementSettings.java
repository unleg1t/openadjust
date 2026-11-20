package com.adjustclient.modules.impl.combat.displacement;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.settings.*;

public final class DisplacementSettings {
    public final BooleanSetting players;
    public final BooleanSetting teammates;
    public final BooleanSetting invisibles;
    public final BooleanSetting landEnabled;
    public final ModeSetting<DisplacementPreference> landPreference;
    public final BooleanSetting targetFacingAway;
    public final KeybindSetting landBind;
    public final BooleanSetting voidEnabled;
    public final KeybindSetting voidBind;
    public final BooleanSetting knockbackItem;
    public final NumberSetting cooldown;
    public final BooleanSetting autoSwap;
    public final BooleanSetting hideRotations;

    public DisplacementSettings(SettingContainer parent) {
        ExpandSetting targets = new ExpandSetting(parent, "Targets");
        players = new BooleanSetting(targets, "Players", true);
        teammates = new BooleanSetting(targets, "Teammates", false);
        invisibles = new BooleanSetting(targets, "Invisibles", true);
        ExpandSetting direction = new ExpandSetting(parent, "Direction");
        ExpandSetting land = new ExpandSetting(direction, "Land");
        land.enabledPredicate = new LandBindVisibility(this);
        landEnabled = new BooleanSetting(land, "Enabled", false, "Yes", "No");
        landPreference = new ModeSetting<>(land, "Preference", DisplacementPreference.LEFT, DisplacementPreference.values());
        targetFacingAway = new BooleanSetting(land, "Target facing away", true);
        targetFacingAway.setDescription("Only displaces when opponent is facing away from us.");
        Bind landKey = Adjust.INSTANCE.bindManager.register(
                "module.displacement.landbind", mode -> { }, "Land Bind");
        landBind = new KeybindSetting(land, "Bind", landKey);
        ExpandSetting voidGroup = new ExpandSetting(direction, "Void");
        voidGroup.enabledPredicate = new VoidBindVisibility(this);
        voidGroup.setDescription("Tries to hit entities into the void where possible.");
        voidEnabled = new BooleanSetting(voidGroup, "Enabled", true, "Yes", "No");
        Bind voidKey = Adjust.INSTANCE.bindManager.register(
                "module.displacement.voidbind", mode -> { }, "Void Bind");
        voidBind = new KeybindSetting(voidGroup, "Bind", voidKey);
        ExpandSetting requirements = new ExpandSetting(parent, "Requirements");
        knockbackItem = new BooleanSetting(requirements, "Knockback item", true);
        knockbackItem.setDescription("Requires you to be holding an item enchanted with knockback.");
        cooldown = new NumberSetting(parent, "Cooldown", 10, 2, 20, 1).formatter(NumberSetting.TICKS);
        cooldown.setDescription("Minimum ticks before displacing again.");
        autoSwap = new BooleanSetting(parent, "Auto swap", false);
        autoSwap.setDescription("Automatically swaps to an item with knockback.");
        hideRotations = new BooleanSetting(parent, "Hide rotations", false);
        hideRotations.setDescription("Hides your rotations from other players by simulating outbound network lag.");
    }

    public int getTargetMask() {
        boolean includePlayers = players.isEnabled();
        boolean includeTeammates = teammates.isEnabled();
        boolean includeInvisibles = invisibles.isEnabled();
        return 0x400 | (includePlayers ? 2 : 0) | (includeTeammates ? 0x20 : 0) | (includeInvisibles ? 0x40 : 0);
    }
    public int getCooldown() { return cooldown.getInt(); }
}
