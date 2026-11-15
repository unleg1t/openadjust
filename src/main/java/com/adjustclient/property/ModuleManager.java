package com.adjustclient.property;

import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.util.HashUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ModuleManager extends Manager<Module> {
    public final Map<Class<? extends Module>, Module> byClass;
    public Object field20;

    public ModuleManager() {
        byClass = new HashMap<>();
        add(new com.adjustclient.module.impl.combat.killaura.KillAuraModule());
        add(new com.adjustclient.module.impl.combat.velocity.VelocityModule());
        add(new com.adjustclient.module.impl.combat.antibot.AntiBotModule());
        add(new com.adjustclient.module.impl.combat.autoblock.AutoBlockModule());
        add(new com.adjustclient.module.impl.combat.criticals.CriticalsModule());
        add(new com.adjustclient.module.impl.combat.MoreKnockbackModule());
        add(new com.adjustclient.module.impl.combat.backtrack.LagBreakModule());
        add(new com.adjustclient.module.impl.combat.backtrack.BackTrackModule());
        add(new com.adjustclient.module.impl.combat.HitSelectModule());
        add(new com.adjustclient.module.impl.combat.displacement.DisplacementModule());
        add(new com.adjustclient.module.impl.combat.autoheal.AutoHealModule());
        add(new com.adjustclient.module.impl.combat.NoClickDelayModule());
        add(new com.adjustclient.module.impl.combat.antifireball.AntiFireballModule());
        add(new com.adjustclient.module.impl.combat.builder.BuilderModule());
        add(new com.adjustclient.module.impl.combat.TeamsDetectionModule());
        add(new com.adjustclient.module.impl.combat.ReachModule());
        add(new com.adjustclient.module.impl.combat.HitboxesModule());
        add(new com.adjustclient.module.impl.combat.RegenModule());
        add(new com.adjustclient.module.impl.player.AntiCheatModule());
        add(new com.adjustclient.module.impl.player.inventory.InventoryModule());
        add(new com.adjustclient.module.impl.player.AntiDebuffModule());
        add(new com.adjustclient.module.impl.player.GameSpeedModule());
        add(new com.adjustclient.module.impl.player.NoRotateModule());
        add(new com.adjustclient.module.impl.player.BlinkModule());
        add(new com.adjustclient.module.impl.player.BlockSwapModule());
        add(new com.adjustclient.module.impl.player.nofall.NoFallModule());
        add(new com.adjustclient.module.impl.player.fastuse.FastUseModule());
        add(new com.adjustclient.module.impl.player.FastPlaceModule());
        add(new com.adjustclient.module.impl.player.DropConfirmModule());
        add(new com.adjustclient.module.impl.player.breaker.BreakerModule());
        add(new com.adjustclient.module.impl.player.autopotion.AutoPotionModule());
        add(new com.adjustclient.module.impl.player.AntiAimModule());
        add(new com.adjustclient.module.impl.player.spammer.SpammerModule());
        add(new com.adjustclient.module.impl.player.killevent.KillEventModule());
        add(new com.adjustclient.module.impl.player.ghosthand.GhostHandModule());
        add(new com.adjustclient.module.impl.player.SeasonalSimulatorModule());
        add(new com.adjustclient.module.impl.player.PixelPartyModule());
        add(new com.adjustclient.module.impl.player.guessthebuild.GuessTheBuildModule());
        add(new com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule());
        add(new com.adjustclient.module.impl.movement.PearlAssistModule());
        add(new com.adjustclient.module.impl.movement.noslowdown.NoSlowdownModule());
        add(new com.adjustclient.module.impl.movement.scaffold.ScaffoldModule());
        add(new com.adjustclient.module.impl.movement.flight.FlightModule());
        add(new com.adjustclient.module.impl.movement.speed.SpeedModule());
        add(new com.adjustclient.module.impl.movement.sprint.SprintModule());
        add(new com.adjustclient.module.impl.movement.step.StepModule());
        add(new com.adjustclient.module.impl.movement.TerrainModule());
        add(new com.adjustclient.module.impl.movement.longjump.LongJumpModule());
        add(new com.adjustclient.module.impl.movement.jesus.JesusModule());
        add(new com.adjustclient.module.impl.movement.SafeWalkModule());
        add(new com.adjustclient.module.impl.player.AntiAFKModule());
        add(new com.adjustclient.module.impl.movement.MovementFixModule());
        add(new com.adjustclient.module.impl.movement.DoubleJumpModule());
        add(new com.adjustclient.module.impl.movement.StasisModule());
        add(new com.adjustclient.module.impl.render.blockoverlay.BlockOverlayModule());
        add(new com.adjustclient.module.impl.render.AtmosphereModule());
        add(new com.adjustclient.module.impl.render.indicators.IndicatorsModule());
        add(new com.adjustclient.module.impl.render.CrosshairModule());
        add(new com.adjustclient.module.impl.render.HurtcamModule());
        add(new com.adjustclient.module.impl.render.streamer.StreamerModule());
        add(new com.adjustclient.module.impl.render.ItemAnimationsModule());
        add(new com.adjustclient.module.impl.render.freecam.FreecamModule());
        add(new com.adjustclient.module.impl.render.esp.ESPModule());
        add(new com.adjustclient.module.impl.render.hud.HUDModule());
        add(new com.adjustclient.module.impl.render.XRayModule());
        add(new com.adjustclient.module.impl.render.DamageParticlesModule());
        add(new com.adjustclient.module.impl.render.skeleton.SkeletonModule());
        add(new com.adjustclient.module.impl.render.BlockPlacesModule());
        add(new com.adjustclient.module.impl.render.chams.ChamsModule());
        add(new com.adjustclient.module.impl.render.ThirdPersonModule());
        add(new com.adjustclient.module.impl.render.motionblur.MotionBlurModule());
        add(new com.adjustclient.module.impl.render.TrailsModule());
        add(new com.adjustclient.module.impl.render.tweaks.TweaksModule());
        add(new com.adjustclient.module.impl.render.bedplates.BedPlatesModule());
        add(new com.adjustclient.module.impl.render.ItemPhysicsModule());
        add(new com.adjustclient.module.impl.render.MoreParticlesModule());
        add(new com.adjustclient.module.impl.render.trajectories.TrajectoriesModule());
        add(new com.adjustclient.module.impl.render.TrueSightModule());
        add(new com.adjustclient.module.impl.exploit.playerdetect.PlayerDetectModule());
        add(new com.adjustclient.module.impl.exploit.ChatFilterModule());
        add(new com.adjustclient.module.impl.exploit.phase.PhaseModule());
        add(new com.adjustclient.module.impl.exploit.JoinClaimModule());
        add(new com.adjustclient.module.impl.exploit.disabler.DisablerModule());
        add(new com.adjustclient.module.impl.exploit.murdermystery.MurderMysteryModule());
        add(new com.adjustclient.module.impl.exploit.AntiSpleefModule());
        add(new com.adjustclient.module.impl.exploit.ClientSpooferModule());
        add(new com.adjustclient.module.impl.exploit.PluginScannerModule());
        add(new com.adjustclient.module.impl.player.NoChatCloseModule());
        add(new com.adjustclient.module.impl.exploit.CreativeTabsModule());
        add(new com.adjustclient.module.impl.render.BarrierRevealModule());
        add(new com.adjustclient.module.impl.movement.NoJumpDelayModule());
        add(new com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule());
        add(new com.adjustclient.module.impl.exploit.IncognitoModule());
        add(new com.adjustclient.module.impl.combat.KnockbackDelayModule());
        add(new com.adjustclient.module.impl.combat.AutoClickerModule());
        add(new com.adjustclient.module.impl.combat.targetstrafe.TargetStrafeModule());
        add(new com.adjustclient.module.impl.movement.antivoid.AntiVoidModule());
        add(new com.adjustclient.module.impl.player.zombies.ZombiesModule());
        add(new com.adjustclient.module.impl.player.voicechat.VoiceChatModule());
        add(new com.adjustclient.module.impl.combat.TPAuraModule());
        for (Module module : view) byClass.put(module.getClass(), module);
        Arrays.stream(Category.values()).forEach(category -> {
            populateCategory(category);
        });
    }

    public void populateCategory(Category category) {
        List<Module> modules = view.stream().filter(module -> {
            return module.category.equals(category);
        }).collect(Collectors.toList());
        if (category.modules == null) category.modules = modules;
    }

    @Override
    public boolean matches(Module module, Object key) {
        if (key instanceof String) {
            return ((String) key).equalsIgnoreCase(module.name)
                || ((String) key).equalsIgnoreCase(module.name.replaceAll(" ", ""));
        }
        if (key instanceof Class<?>) return module.getClass() == key;
        return key instanceof Number && ((Number) key).intValue() == HashUtil.hash(module.name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> type) {
        return (T) byClass.get(type);
    }
}
