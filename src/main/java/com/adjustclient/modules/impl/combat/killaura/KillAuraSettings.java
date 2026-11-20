package com.adjustclient.modules.impl.combat.killaura;

import com.adjustclient.core.settings.*;
import com.adjustclient.modules.util.CpsSettings;
import com.adjustclient.core.rotation.RotationSetting;
import com.adjustclient.core.rotation.RotationMode;
import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.modules.impl.combat.killaura.target.priority.TargetPriority;
import com.adjustclient.modules.impl.combat.killaura.target.priority.impl.*;
import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class KillAuraSettings {
    public ModeSetting<KillAuraMode> mode;
    public ModeSetting<TargetPriority> priority;
    public NumberSetting rangeFieldOfView;
    public NumberSetting rangeAttackPlayers;
    public NumberSetting rangeAttackOthers;
    public NumberSetting rangeSwingRange;
    public NumberSetting rangeRotate;
    public BooleanSetting rangeSwingHideSwings;
    public BooleanSetting rangeThroughWalls;
    public BooleanSetting rangeThroughEntities;
    public BooleanSetting targetsPlayers;
    public BooleanSetting targetsPassive;
    public BooleanSetting targetsHostile;
    public BooleanSetting targetsInvisibles;
    public BooleanSetting targetsTeammates;
    public RotationSetting rotationModel;
    public BooleanSetting rotationsEnabled;
    public ModeSetting<RotationVisibility> rotationsVisibility;
    public NumberSetting rotationsHorizontalSpeed;
    public NumberSetting rotationsVerticalSpeed;
    public ModeSetting<LookForMode> rotationsLookFor;
    public BooleanSetting rotationsIncreaseFov;
    public NumberSetting rotationsMultipointHorizontal;
    public NumberSetting rotationsMultipointVertical;
    public BooleanSetting rotationsMultipointPreferCenterMass;
    public CpsSettings cps;
    public BooleanSetting renderTargetEnabled;
    public ColorSetting renderTargetColor;
    public ColorSetting renderTargetHurtColor;
    public NumberSetting renderTargetExpand;
    public BooleanSetting renderTargetOutline;
    public BooleanSetting renderRangeEnabled;
    public ColorSetting renderRangeColor;
    public ColorSetting renderRangeSecondaryColor;
    public BooleanSetting disableOnWorldJoin;
    public BooleanSetting disableOnNoWeapon;
    public BooleanSetting disableOnBreakingBlock;
    public BooleanSetting holdAttack;
    public NumberSetting switchDelay;

    public KillAuraSettings(KillAuraModule parent) {
        mode = new ModeSetting<>(parent, "Mode", KillAuraMode.SINGLE, KillAuraMode.values());
        TargetPriority smart = new SmartPriority();
        priority = new ModeSetting<>(parent, "Priority", smart, smart, new DamagePriority(), new HealthPriority(), new DistancePriority());
        cps = new CpsSettings(parent, 8, 16);
        ExpandSetting range = new ExpandSetting(parent, "Range");
        rangeFieldOfView = new NumberSetting(range, "Field of view", 180, 5, 180, 1).formatter(com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_A);
        ExpandSetting attack = new ExpandSetting(range, "Attack");
        rangeAttackPlayers = new NumberSetting(attack, "Players", 3.0, 3.0, 6.0, 0.01).formatter(com.adjustclient.core.settings.NumberSetting.METERS);
        rangeAttackOthers = new NumberSetting(attack, "Others", 3.0, 3.0, 6.0, 0.01).formatter(com.adjustclient.core.settings.NumberSetting.METERS);
        rangeAttackOthers.setDescription("Range for entities other than players (mobs)");
        ExpandSetting swing = new ExpandSetting(range, "Swing");
        rangeSwingRange = new NumberSetting(swing, "Range", 3.0, 3.0, 10.0, 0.01).formatter(com.adjustclient.core.settings.NumberSetting.METERS);
        rangeSwingHideSwings = new BooleanSetting(swing, "Hide swings", true);
        rangeSwingHideSwings.setDescription("Hides swings clientside (visual effect only).");
        rangeRotate = new NumberSetting(range, "Rotate", 3.0, 3.0, 10.0, 0.01).formatter(com.adjustclient.core.settings.NumberSetting.METERS);
        rangeThroughWalls = new BooleanSetting(range, "Through walls", true);
        rangeThroughWalls.setDescription("Enabling allows targets to be hit through solid blocks.");
        rangeThroughEntities = new BooleanSetting(range, "Through entities", true);
        rangeThroughEntities.setDescription("Enabling allows targets to be hit through other players/entities.");
        ExpandSetting targets = new ExpandSetting(parent, "Targets");
        targetsPlayers = new BooleanSetting(targets, "Players", true);
        targetsPassive = new BooleanSetting(targets, "Passive", false);
        targetsHostile = new BooleanSetting(targets, "Hostile", false);
        targetsTeammates = new BooleanSetting(targets, "Teammates", false);
        targetsInvisibles = new BooleanSetting(targets, "Invisibles", true);
        ExpandSetting rotations = new ExpandSetting(parent, "Rotations");
        rotations.enabledPredicate = () -> rotationsEnabled.isEnabled();
        rotationsEnabled = new BooleanSetting(rotations, "Enabled", true, "Yes", "No");
        rotationsVisibility = new ModeSetting<>(rotations, "Visibility", RotationVisibility.SILENT, RotationVisibility.values());
        rotationModel = new RotationSetting(rotations, RotationMode.INSTANT,
                () -> rotationsVisibility.getValue() != RotationVisibility.SILENT, RotationMode.values());
        rotationsHorizontalSpeed = new NumberSetting(rotations, "Horizontal speed", 50, 1, 100, 1,
                () -> rotationsVisibility.getValue() != RotationVisibility.LINEAR).formatter(com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_B);
        rotationsVerticalSpeed = new NumberSetting(rotations, "Vertical speed", 50, 0, 100, 1,
                () -> rotationsVisibility.getValue() != RotationVisibility.LINEAR).formatter(com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_B);
        rotationsLookFor = new ModeSetting<>(rotations, "Look For", LookForMode.INSTANT,
                () -> !rotationsEnabled.isEnabled(), LookForMode.values());
        rotationsIncreaseFov = new BooleanSetting(rotations, "Increase FOV", true,
                () -> rotationsVisibility.getValue() == RotationVisibility.SILENT);
        rotationsIncreaseFov.setDescription("Increases field of view when locked.");
        ExpandSetting multipoint = new ExpandSetting(rotations, "Multipoint",
                () -> rotationsVisibility.getValue() == RotationVisibility.SILENT);
        rotationsMultipointHorizontal = new NumberSetting(multipoint, "Horizontal", 50, 0, 100, 1).formatter(com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_B);
        rotationsMultipointVertical = new NumberSetting(multipoint, "Vertical", 50, 0, 100, 1).formatter(com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_B);
        rotationsMultipointPreferCenterMass = new BooleanSetting(multipoint, "Prefer center mass", false);
        ExpandSetting disableOn = new ExpandSetting(parent, "Disable on");
        disableOn.enabledPredicate = () -> disableOnWorldJoin.isEnabled() || disableOnBreakingBlock.isEnabled() || disableOnNoWeapon.isEnabled();
        disableOnWorldJoin = new BooleanSetting(disableOn, "World join", true);
        disableOnBreakingBlock = new BooleanSetting(disableOn, "Breaking block", true);
        disableOnNoWeapon = new BooleanSetting(disableOn, "No weapon", false);
        ExpandSetting render = new ExpandSetting(parent, "Render");
        render.enabledPredicate = () -> renderTargetEnabled.isEnabled() || renderRangeEnabled.isEnabled();
        com.adjustclient.modules.ModuleSerializer.RESTRICTED_SETTINGS.add(render);
        ExpandSetting target = new ExpandSetting(render, "Target");
        target.enabledPredicate = () -> renderTargetEnabled.isEnabled();
        renderTargetEnabled = new BooleanSetting(target, "Enabled", false, "Yes", "No");
        renderTargetColor = new ColorSetting(target, "Color", new Color(0x64435ad1, true));
        renderTargetHurtColor = new ColorSetting(target, "Hurt color", new Color(0x64a83b44, true));
        renderTargetExpand = new NumberSetting(target, "Expand", 0.1F, 0.0F, 0.2F, 0.05F);
        renderTargetOutline = new BooleanSetting(target, "Outline", true);
        ExpandSetting renderRange = new ExpandSetting(render, "Range");
        renderRange.enabledPredicate = () -> renderRangeEnabled.isEnabled();
        renderRangeEnabled = new BooleanSetting(renderRange, "Enabled", false, "Yes", "No");
        renderRangeColor = new ColorSetting(renderRange, "Color", new Color(0xd2dc4747, true));
        renderRangeSecondaryColor = new ColorSetting(renderRange, "Secondary Color", new Color(0x3c7941a2, true));
        switchDelay = new NumberSetting(parent, "Switch delay", 350L, 50L, 1000L, 50L,
                () -> mode.getValue() != KillAuraMode.SWITCH).formatter(com.adjustclient.core.settings.NumberSetting.MILLISECONDS);
        switchDelay.setDescription("Time to stay on the current target before switching to the next one.");
        holdAttack = new BooleanSetting(parent, "Hold attack", false);
    }

    public float getVerticalSpeed() { return rotationsVerticalSpeed.getValue().floatValue(); }
    public float getTargetExpand() { return renderTargetExpand.getValue().floatValue(); }
    public long getSwitchDelay() { return switchDelay.getValue().longValue(); }
    public double getMultipointHorizontal() { return rotationsMultipointHorizontal.getValue().doubleValue(); }
    public double getMultipointVertical() { return rotationsMultipointVertical.getValue().doubleValue(); }
    public double getAttackRange(Entity entity) {
        return entity instanceof EntityPlayer ? getPlayerAttackRange() : rangeAttackOthers.getValue().doubleValue();
    }
    public int getRangeColor() { return renderRangeColor.getColor().getRGB(); }
    public double getPlayerAttackRange() {
        double range = rangeAttackPlayers.getValue().doubleValue();
        if (range <= 3.0D) return 3.0D;
        return com.adjustclient.utilities.anticheat.feature.Features.REACH.isAllowed() ? range : 3.0D;
    }
    public double getSwingRange() { return rangeSwingRange.getValue().doubleValue(); }
    public double getRotateRange() { return rangeRotate.getValue().doubleValue(); }
    public int getTargetMask() {
        int mask = 0;
        if (targetsPlayers.isEnabled()) mask |= 2;
        if (targetsHostile.isEnabled()) mask |= 4;
        if (targetsPassive.isEnabled()) mask |= 8;
        if (targetsTeammates.isEnabled()) mask |= 0x20;
        if (targetsInvisibles.isEnabled()) mask |= 0x40;
        return mask;
    }
    public int getTargetColor() { return renderTargetColor.getColor().getRGB(); }
    public int getTargetHurtColor() { return renderTargetHurtColor.getColor().getRGB(); }
    public float getHorizontalSpeed() { return rotationsHorizontalSpeed.getValue().floatValue(); }
}
