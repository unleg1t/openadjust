package com.adjustclient.core.settings.property;

import com.adjustclient.modules.Module;
import com.adjustclient.modules.Category;
import com.adjustclient.utilities.HashUtil;
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
        add(new com.adjustclient.modules.impl.combat.killaura.KillAuraModule());
        add(new com.adjustclient.modules.impl.combat.velocity.VelocityModule());
        add(new com.adjustclient.modules.impl.combat.antibot.AntiBotModule());
        add(new com.adjustclient.modules.impl.combat.autoblock.AutoBlockModule());
        add(new com.adjustclient.modules.impl.combat.criticals.CriticalsModule());
        add(new com.adjustclient.modules.impl.combat.MoreKnockbackModule());
        add(new com.adjustclient.modules.impl.combat.backtrack.LagBreakModule());
        add(new com.adjustclient.modules.impl.combat.backtrack.BackTrackModule());
        add(new com.adjustclient.modules.impl.combat.HitSelectModule());
        add(new com.adjustclient.modules.impl.combat.displacement.DisplacementModule());
        add(new com.adjustclient.modules.impl.combat.autoheal.AutoHealModule());
        add(new com.adjustclient.modules.impl.combat.NoClickDelayModule());
        add(new com.adjustclient.modules.impl.combat.antifireball.AntiFireballModule());
        add(new com.adjustclient.modules.impl.combat.builder.BuilderModule());
        add(new com.adjustclient.modules.impl.combat.TeamsDetectionModule());
        add(new com.adjustclient.modules.impl.combat.ReachModule());
        add(new com.adjustclient.modules.impl.combat.HitboxesModule());
        add(new com.adjustclient.modules.impl.combat.RegenModule());
        add(new com.adjustclient.modules.impl.player.AntiCheatModule());
        add(new com.adjustclient.modules.impl.player.inventory.InventoryModule());
        add(new com.adjustclient.modules.impl.player.AntiDebuffModule());
        add(new com.adjustclient.modules.impl.player.AutoRespawnModule());
        add(new com.adjustclient.modules.impl.player.GameSpeedModule());
        add(new com.adjustclient.modules.impl.player.NoRotateModule());
        add(new com.adjustclient.modules.impl.player.BlinkModule());
        add(new com.adjustclient.modules.impl.player.BlockSwapModule());
        add(new com.adjustclient.modules.impl.player.nofall.NoFallModule());
        add(new com.adjustclient.modules.impl.player.fastuse.FastUseModule());
        add(new com.adjustclient.modules.impl.player.FastPlaceModule());
        add(new com.adjustclient.modules.impl.player.DropConfirmModule());
        add(new com.adjustclient.modules.impl.player.breaker.BreakerModule());
        add(new com.adjustclient.modules.impl.player.autopotion.AutoPotionModule());
        add(new com.adjustclient.modules.impl.player.AntiAimModule());
        add(new com.adjustclient.modules.impl.player.spammer.SpammerModule());
        add(new com.adjustclient.modules.impl.player.killevent.KillEventModule());
        add(new com.adjustclient.modules.impl.player.ghosthand.GhostHandModule());
        add(new com.adjustclient.modules.impl.player.SeasonalSimulatorModule());
        add(new com.adjustclient.modules.impl.player.PixelPartyModule());
        add(new com.adjustclient.modules.impl.player.guessthebuild.GuessTheBuildModule());
        add(new com.adjustclient.modules.impl.movement.inventorymove.InventoryMoveModule());
        add(new com.adjustclient.modules.impl.movement.PearlAssistModule());
        add(new com.adjustclient.modules.impl.movement.noslowdown.NoSlowdownModule());
        add(new com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule());
        add(new com.adjustclient.modules.impl.movement.flight.FlightModule());
        add(new com.adjustclient.modules.impl.movement.speed.SpeedModule());
        add(new com.adjustclient.modules.impl.movement.sprint.SprintModule());
        add(new com.adjustclient.modules.impl.movement.step.StepModule());
        add(new com.adjustclient.modules.impl.movement.TerrainModule());
        add(new com.adjustclient.modules.impl.movement.longjump.LongJumpModule());
        add(new com.adjustclient.modules.impl.movement.jesus.JesusModule());
        add(new com.adjustclient.modules.impl.movement.SafeWalkModule());
        add(new com.adjustclient.modules.impl.player.AntiAFKModule());
        add(new com.adjustclient.modules.impl.movement.MovementFixModule());
        add(new com.adjustclient.modules.impl.movement.highjump.HighJumpModule());
        add(new com.adjustclient.modules.impl.movement.DoubleJumpModule());
        add(new com.adjustclient.modules.impl.movement.StasisModule());
        add(new com.adjustclient.modules.impl.render.blockoverlay.BlockOverlayModule());
        add(new com.adjustclient.modules.impl.render.AtmosphereModule());
        add(new com.adjustclient.modules.impl.render.indicators.IndicatorsModule());
        add(new com.adjustclient.modules.impl.render.CrosshairModule());
        add(new com.adjustclient.modules.impl.render.HurtcamModule());
        add(new com.adjustclient.modules.impl.render.streamer.StreamerModule());
        add(new com.adjustclient.modules.impl.render.ItemAnimationsModule());
        add(new com.adjustclient.modules.impl.render.freecam.FreecamModule());
        add(new com.adjustclient.modules.impl.render.esp.ESPModule());
        add(new com.adjustclient.modules.impl.render.hud.HUDModule());
        add(new com.adjustclient.modules.impl.render.XRayModule());
        add(new com.adjustclient.modules.impl.render.DamageParticlesModule());
        add(new com.adjustclient.modules.impl.render.skeleton.SkeletonModule());
        add(new com.adjustclient.modules.impl.render.BlockPlacesModule());
        add(new com.adjustclient.modules.impl.render.chams.ChamsModule());
        add(new com.adjustclient.modules.impl.render.ThirdPersonModule());
        add(new com.adjustclient.modules.impl.render.motionblur.MotionBlurModule());
        add(new com.adjustclient.modules.impl.render.TrailsModule());
        add(new com.adjustclient.modules.impl.render.tweaks.TweaksModule());
        add(new com.adjustclient.modules.impl.render.bedplates.BedPlatesModule());
        add(new com.adjustclient.modules.impl.render.ItemPhysicsModule());
        add(new com.adjustclient.modules.impl.render.MoreParticlesModule());
        add(new com.adjustclient.modules.impl.render.trajectories.TrajectoriesModule());
        add(new com.adjustclient.modules.impl.render.TrueSightModule());
        add(new com.adjustclient.modules.impl.exploit.playerdetect.PlayerDetectModule());
        add(new com.adjustclient.modules.impl.exploit.ChatFilterModule());
        add(new com.adjustclient.modules.impl.exploit.phase.PhaseModule());
        add(new com.adjustclient.modules.impl.exploit.JoinClaimModule());
        add(new com.adjustclient.modules.impl.exploit.disabler.DisablerModule());
        add(new com.adjustclient.modules.impl.exploit.murdermystery.MurderMysteryModule());
        add(new com.adjustclient.modules.impl.exploit.AntiSpleefModule());
        add(new com.adjustclient.modules.impl.exploit.ClientSpooferModule());
        add(new com.adjustclient.modules.impl.exploit.PluginScannerModule());
        add(new com.adjustclient.modules.impl.player.NoChatCloseModule());
        add(new com.adjustclient.modules.impl.exploit.CreativeTabsModule());
        add(new com.adjustclient.modules.impl.render.BarrierRevealModule());
        add(new com.adjustclient.modules.impl.movement.NoJumpDelayModule());
        add(new com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule());
        add(new com.adjustclient.modules.impl.player.autorewound.AutoRewoundModule());
        add(new com.adjustclient.modules.impl.exploit.IncognitoModule());
        add(new com.adjustclient.modules.impl.combat.KnockbackDelayModule());
        add(new com.adjustclient.modules.impl.combat.AutoClickerModule());
        add(new com.adjustclient.modules.impl.combat.targetstrafe.TargetStrafeModule());
        add(new com.adjustclient.modules.impl.movement.antivoid.AntiVoidModule());
        add(new com.adjustclient.modules.impl.player.voicechat.VoiceChatModule());
        add(new com.adjustclient.modules.impl.combat.TPAuraModule());
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
