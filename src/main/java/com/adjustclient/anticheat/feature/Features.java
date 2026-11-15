package com.adjustclient.anticheat.feature;

public final class Features {
    public static final Feature HITBOXES = new Feature("Hitboxes", false);
    public static final Feature REACH = new Feature("Reach", false);
    public static final Feature FLIGHT = new Feature("Flight", false);
    public static final Feature REGEN = new Feature("Regen", false);
    public static final Feature GAME_SPEED = new Feature("Timer / Game Speed", false);
    public static final Feature NO_MOVEMENT_FIX = new Feature("No Movement Fix", false);
    public static final ModuleFeature ANTI_AIM = new ModuleFeature(com.adjustclient.module.impl.player.AntiAimModule.class, null, true);
    public static final ModuleFeature VELOCITY_VERTICAL = new ModuleFeature(com.adjustclient.module.impl.combat.velocity.VelocityModule.class, "Vertical", false);
    public static final ModuleFeature VELOCITY_HORIZONTAL = new ModuleFeature(com.adjustclient.module.impl.combat.velocity.VelocityModule.class, "Horizontal", false);
    public static final ModuleFeature CRITICALS_PACKET = new ModuleFeature(com.adjustclient.module.impl.combat.criticals.CriticalsModule.class, "Packet", false);
    public static final ModuleFeature ANTI_VOID_BLINK = new ModuleFeature(com.adjustclient.module.impl.movement.antivoid.AntiVoidModule.class, "Blink", false);
    public static final ModuleFeature SCAFFOLD_NO_ASCEND = new ModuleFeature(com.adjustclient.module.impl.movement.scaffold.ScaffoldModule.class, "No Ascend", false);
    public static final ModuleFeature SPRINT_STRAFE = new ModuleFeature(com.adjustclient.module.impl.movement.sprint.SprintModule.class, "Strafe", false);
    public static final ModuleFeature DOUBLE_JUMP_MMC = new ModuleFeature(com.adjustclient.module.impl.movement.DoubleJumpModule.class, "MMC", false);
    public static final ModuleFeature DOUBLE_JUMP_VANILLA = new ModuleFeature(com.adjustclient.module.impl.movement.DoubleJumpModule.class, "Vanilla", false);
    public static final ModuleFeature STEP_VANILLA = new ModuleFeature(com.adjustclient.module.impl.movement.step.StepModule.class, "Vanilla", false);
    public static final ModuleFeature STEP_NCP = new ModuleFeature(com.adjustclient.module.impl.movement.step.StepModule.class, "NCP", false);
    public static final ModuleFeature STEP_MOTION = new ModuleFeature(com.adjustclient.module.impl.movement.step.StepModule.class, "Motion", false);
    public static final ModuleFeature NO_FALL_PACKET = new ModuleFeature(com.adjustclient.module.impl.player.nofall.NoFallModule.class, "Packet", false);
    public static final ModuleFeature NO_FALL_SPOOF = new ModuleFeature(com.adjustclient.module.impl.player.nofall.NoFallModule.class, "Spoof", false);
    public static final ModuleFeature TERRAIN_VANILLA = new ModuleFeature(com.adjustclient.module.impl.movement.TerrainModule.class, "Vanilla", false);
    public static final ModuleFeature TERRAIN_VULCAN = new ModuleFeature(com.adjustclient.module.impl.movement.TerrainModule.class, "Vulcan", false);
    public static final ModuleFeature NO_SLOW_SWAP = new ModuleFeature(com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule.class, "Swap", false);
    public static final ModuleFeature NO_SLOW_HYPIXEL = new ModuleFeature(com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule.class, "Hypixel", false);
    public static final ModuleFeature NO_SLOW_VANILLA = new ModuleFeature(com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule.class, "Vanilla", false);
    public static final ModuleFeature NO_SLOW_LEGIT = new ModuleFeature(com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule.class, "Legit", true);
    public static final ModuleFeature NO_SLOW_NCP = new ModuleFeature(com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule.class, "NCP", false);
    public static final ModuleFeature NO_SLOW_CUSTOM = new ModuleFeature(com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule.class, "Custom", true);
    public static final ModuleFeature SPEED_VANILLA = new ModuleFeature(com.adjustclient.module.impl.movement.speed.SpeedModule.class, "Vanilla", false);
    public static final ModuleFeature SPEED_NCP = new ModuleFeature(com.adjustclient.module.impl.movement.speed.SpeedModule.class, "NCP", false);
    public static final ModuleFeature GAME_SPEED_HYPIXEL = new ModuleFeature(com.adjustclient.module.impl.player.GameSpeedModule.class, "Hypixel", false);
}
