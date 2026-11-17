package com.adjustclient.module.impl.movement.speed;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.bind.Bind;
import com.adjustclient.module.impl.combat.criticals.mode.RewoundCriticalsMode;
import com.adjustclient.module.impl.movement.scaffold.RewoundScaffoldMode;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.module.impl.movement.step.StepModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.SimulationPlayer;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.PacketWatcher;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;

public final class RewoundSpeedMode extends SpeedMode<RewoundSpeedState> {
    public final NumberSetting timer;
    public final NumberSetting damageBoost;
    public final BooleanSetting lowhopEnabled;
    public final BooleanSetting collisionPrediction;
    public final Bind disableBind;
    public final BooleanSetting disableViewBobbing;
    public final BooleanSetting disablerEnabled;
    public final NumberSetting disablerSpeed;

    public RewoundSpeedMode(SpeedModule module) {
        super(module, Features.SPEED_REWOUND);
        BooleanSupplier hideIf = () -> module.mode.getValue() != this;

        timer = new NumberSetting(module, "Timer", 1.1D, 1.0D, 2.0D, 0.05D, hideIf)
                .formatter(NumberSetting.MULTIPLIER);
        timer.setDisplayName("rewoundTimer");

        damageBoost = new NumberSetting(module, "Damage boost", 1.5D, 1.0D, 3.0D, 0.05D, hideIf)
                .formatter(NumberSetting.MULTIPLIER);
        damageBoost.setDisplayName("rewoundDamageBoost2");

        ExpandSetting lowhop = new ExpandSetting(module, "Lowhop", hideIf);
        lowhop.setDisplayName("rewoundLohop2");
        lowhopEnabled = new BooleanSetting(lowhop, "Enabled", false, "Yes", "No");
        lowhop.setActiveSupplier(lowhopEnabled::isEnabled);
        collisionPrediction = new BooleanSetting(lowhop, "Collision prediction", true)
                .description("Predicts when your about to hit a wall or fall into the void and does a full jump.");
        disableBind = Adjust.INSTANCE.bindManager.register("speed.rewound.lowhopfulljump", mode -> {
        }, "Rewound Lowhop Disable Bind");
        new KeybindSetting(lowhop, "Disable bind", disableBind);
        disableViewBobbing = new BooleanSetting(lowhop, "Disable view bobbing", true);

        ExpandSetting disabler = new ExpandSetting(module, "Disabler", hideIf);
        disabler.setDisplayName("rewoundDisabler");
        disabler.setDescription("Uses faster speed when the disabler from the Flight module is active.");
        disablerEnabled = new BooleanSetting(disabler, "Enabled", false, "Yes", "No");
        disabler.setActiveSupplier(disablerEnabled::isEnabled);
        disablerSpeed = new NumberSetting(disabler, "Speed", 1.0D, 0.5D, 10.0D, 0.1D);
    }

    @Override
    public String getName() {
        return "Rewound";
    }

    @Override
    public RewoundSpeedState createState(Entity entity) {
        return new RewoundSpeedState(entity);
    }

    public void reset(RewoundSpeedState state) {
        state.speed = 0.0D;
        state.started = false;
    }

    public boolean isLowHopActive() {
        return lowhopEnabled.isEnabled() && !disableBind.isDown();
    }

    public void onViewBobbing() {
        if (isLowHopActive() && disableViewBobbing.isEnabled() && mc.thePlayer != null) {
            mc.thePlayer.cameraYaw = 0.0F;
            mc.thePlayer.cameraPitch = 0.0F;
        }
    }

    @Override
    public void update(RewoundSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (MovementUtil.isMovementRestricted(entity) || StepModule.INSTANCE.isStepping()) {
            reset(state);
            return;
        }
        ScaffoldModule scaffold = ScaffoldModule.INSTANCE;
        if (scaffold != null && scaffold.placeReady) {
            Object scaffoldMode = scaffold.settings.mode.getValue();
            if (scaffoldMode instanceof RewoundScaffoldMode && ((RewoundScaffoldMode) scaffoldMode).shouldTower()) {
                reset(state);
                return;
            }
        }
        if (!MovementUtil.isMoving()) {
            reset(state);
            if (!WorldUtil.isOverVoid()) {
                if (isOverVoidAhead(entity)) {
                    entity.motionX = 0.0D;
                    entity.motionY = 0.0D;
                } else {
                    entity.motionX *= 0.82D;
                    entity.motionZ *= 0.82D;
                }
            }
            return;
        }
        if (Adjust.movementPredictor.historyTicks < 7) {
            state.started = false;
            return;
        }
        if (!(entity instanceof SimulationPlayer)) {
            mc.timer.setTimerSpeed(timer.getFloat());
        }
        updateHop(state);
        applyLowHop(state);
    }

    private static double calculateFriction(double lastDistance, double baseSpeed) {
        return Math.max(
                Math.max(lastDistance - lastDistance / 160.0D, lastDistance - (lastDistance - baseSpeed) / 33.3D),
                lastDistance - (lastDistance - baseSpeed) * 0.02D
        );
    }

    public void updateHop(RewoundSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        double base = 0.2873D;
        double initial = 0.377855D;
        if (mc.thePlayer != null && entity.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            base = ((amplifier + 1) * 0.2D + 1.0D) * 0.2873D;
            initial = base * 1.35D - 0.01D;
        }
        if (entity.offGroundTicks == 0) {
            if (MovementUtil.isAutoJumping()) {
                state.speed = 0.0D;
                state.started = false;
            } else if (MovementUtil.isMoving()) {
                if (!isLowHopActive() || state.started) {
                    if (entity.motionY <= 0.0D) {
                        entity.jump();
                    }
                    state.speed = MovementUtil.getSpeedWithPotion(0.2873D, 0.2D) * 2.149D;
                } else {
                    state.speed = initial;
                    state.started = true;
                }
                RewoundCriticalsMode.INSTANCE.jumped = true;
            }
        } else if (entity.offGroundTicks == 1) {
            state.speed -= (state.speed - base) * 0.66D;
            if (!state.damageTimer.hasElapsed(500L)) {
                state.speed *= damageBoost.getDouble();
            }
        } else {
            state.speed = calculateFriction(entity.lastDistance, base);
            state.started = false;
        }

        double minSpeed = base;
        if (disablerEnabled.isEnabled()) {
            int avgDelay = 0;
            if (ServerTracker.INSTANCE.serverState != null) {
                PacketWatcher watcher = ServerTracker.INSTANCE.serverState.packetWatcher;
                if (watcher.delayCount != 0) {
                    avgDelay = (int) (watcher.totalDelay / watcher.delayCount);
                }
            }
            long window = avgDelay < 2931 ? 2930L - avgDelay : 0L;
            if (Adjust.rewoundDisablerTracker.activeTimer != null
                    && !Adjust.rewoundDisablerTracker.activeTimer.hasElapsed(window)
                    && entity.offGroundTicks < 2) {
                minSpeed = Math.max(base, disablerSpeed.getDouble());
            }
        }
        state.speed = Math.max(state.speed, minSpeed);
        strafe(entity, state.speed);
    }

    public void applyLowHop(RewoundSpeedState state) {
        if (Adjust.movementPredictor.historyTicks < 13) {
            return;
        }
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (entity.isPotionActive(Potion.jump)) {
            return;
        }
        if (isLowHopActive() && state.started && !shouldFullJump(entity)) {
            if (entity.offGroundTicks == 1) {
                entity.motionY = (double) -0.0784F;
                state.started = false;
            }
        } else if (entity.offGroundTicks == 4) {
            entity.motionY -= 0.045D;
        } else if (entity.offGroundTicks == 5) {
            entity.motionY -= 0.175D;
        }
    }

    public boolean shouldFullJump(EntityPlayer entity) {
        if (!collisionPrediction.isEnabled()) {
            return false;
        }
        if (entity.isCollidedHorizontally) {
            return true;
        }
        Simulation simulation = new Simulation(entity);
        if (!(entity instanceof SimulationPlayer)) {
            simulation.includeSpeedModule();
        }
        int steps = entity instanceof SimulationPlayer ? 6 : 4;
        for (int i = 0; i < steps; i++) {
            simulation.player.tick();
            if (simulation.player.isCollidedHorizontally) {
                return true;
            }
            if (WorldUtil.isFreeAt(new Vec3(simulation.player.posX, simulation.player.posY, simulation.player.posZ), 0.0D)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOverVoidAhead(Entity entity) {
        double distance = Math.max(0.5D, Math.hypot(entity.motionX, entity.motionZ));
        float degrees = (float) (Math.atan2(-entity.motionX, entity.motionZ) * 57.29577951308232D);
        double radians = degrees * 0.0174532925199433D;
        double x = -Math.sin(radians) * distance + entity.posX;
        double z = Math.cos(radians) * distance + entity.posZ;
        return WorldUtil.isFreeAt(new Vec3(x, entity.posY, z), 0.0D);
    }
}
