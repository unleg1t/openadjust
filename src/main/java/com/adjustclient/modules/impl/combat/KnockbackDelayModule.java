package com.adjustclient.modules.impl.combat;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.bind.BindMode;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;

public final class KnockbackDelayModule extends Module {
    public static KnockbackDelayModule INSTANCE;
    public final BooleanSetting groundEnabled;
    public final BooleanSetting groundOnlyOnJump;
    public final BooleanSetting closeDirection;
    public final BooleanSetting explosions;
    public final NumberSetting delay;
    public final BooleanSetting validateDamage;
    public Bind bufferBind;
    public BooleanSetting bufferHold;
    private final Timer fireballTimer = new Timer(0L);
    public final Object lock = new Object();
    private DelayedVelocity pending;
    private int groundTicks = Integer.MAX_VALUE;
    private boolean closeEnough;
    private boolean buffering;
    public boolean damageValidated;
    public boolean damageConsumed;

    public KnockbackDelayModule() {
        super("Knockback Delay", Category.COMBAT);
        INSTANCE = this;
        delay = new NumberSetting(this, "Delay", 350, 50, 5000, 50).formatter(NumberSetting.MILLISECONDS);
        explosions = new BooleanSetting(this, "Explosions", false);
        validateDamage = new BooleanSetting(this, "Validate damage", true);
        validateDamage.setDescription("Checks if the velocity you receive is linked to player damage.");
        ExpandSetting dispatchOn = new ExpandSetting(this, "Dispatch on");
        ExpandSetting ground = new ExpandSetting(dispatchOn, "Ground");
        ground.enabledPredicate = new KnockbackDelayHoldVisibility(this);
        groundEnabled = new BooleanSetting(ground, "Enabled", true, "Yes", "No");
        groundOnlyOnJump = new BooleanSetting(ground, "Only on jump", false);
        closeDirection = new BooleanSetting(dispatchOn, "Close direction", false);
        bufferBind = Adjust.INSTANCE.bindManager.register("module.knockbackdelay.bind", mode -> {
            synchronized (lock) {
                if (bufferHold.isEnabled()) buffering = mode == BindMode.PRESSED || bufferBind.isDown();
                else if (mode == BindMode.PRESSED) buffering = !buffering;
                if (buffering && pending != null && !pending.forced())
                    pending = new DelayedVelocity(pending.blockRule(), pending.stopwatch(), pending.motion(), true);
            }
        }, null);
        ExpandSetting buffer = new ExpandSetting(this, "Buffer");
        buffer.enabledPredicate = () -> !bufferBind.keys.isEmpty();
        KeybindSetting bind = new KeybindSetting(buffer, "Bind", bufferBind);
        bind.setDescription("Buffers your knockback when held.");
        bufferHold = new BooleanSetting(buffer, "Hold", true);
        bufferHold.addListener(() -> { synchronized (lock) { buffering = false; } });
        bufferBind.listeners.add(() -> { synchronized (lock) { buffering = false; } });
    }

    @EventHandler public void onClientTick(GameLoopEvent event) {
        if (!isEnabled()) return;
        synchronized (lock) {
            if (buffering && bufferHold.isEnabled() && !bufferBind.isDown()) buffering = false;
        }
        dispatch(false);
    }

    @EventHandler public void onFallDistance(JumpMotionEvent event) {
        if (!isEnabled() || !event.pre || !groundEnabled.isEnabled() || !groundOnlyOnJump.isEnabled()) return;
        synchronized (lock) { if (buffering) return; }
        dispatch(true);
    }

    @EventHandler public void onSlowdown(ItemUseNotificationEvent event) {
        if (!isEnabled()) return;
        if (event.stack != null && event.stack.getItem() != null
                && event.stack.getItem().getClass() == net.minecraft.item.ItemFireball.class) {
            synchronized (lock) { fireballTimer.reset(); }
        }
    }

    @EventHandler public void onTick(PreClientTickEvent event) {
        if (!isEnabled() || !canDelay()) return;
        DelayedVelocity velocity;
        synchronized (lock) { if (buffering) return; velocity = pending; }
        if (velocity != null) {
            if (isTargetOutOfReach()) { dispatch(true); return; }
            if (closeDirection.isEnabled()) {
                float yaw = (float) (Math.atan2(-velocity.motion().xCoord, velocity.motion().zCoord) * 57.29577951308232);
                boolean safe = Math.abs(MathHelper.wrapAngleTo180_float(yaw - MovementUtil.getDirection())) <= 70.0F;
                if (safe) {
                    Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
                    var player = simulation.player;
                    player.motionX = velocity.motion().xCoord;
                    player.motionY = velocity.motion().yCoord;
                    player.motionZ = velocity.motion().zCoord;
                    for (int i = 0; i < 14; ++i) {
                        player.tick();
                        if (player.fallDistance >= mc.thePlayer.fallDistance + 2.0F) { safe = false; break; }
                    }
                }
                synchronized (lock) { if (velocity == pending) closeEnough = safe; }
            }
        } else {
            int ticks = 0;
            if (!mc.thePlayer.onGround) {
                ticks = Integer.MAX_VALUE;
                Simulation simulation = new Simulation(mc.thePlayer);
                for (int i = 1; i <= delay.getLong() / 50L; ++i) {
                    simulation.player.tick();
                    if (simulation.player.onGround) { ticks = i; break; }
                }
            }
            synchronized (lock) { groundTicks = ticks; }
        }
    }

    @Override public void onEnable() {
        synchronized (lock) { buffering = false; fireballTimer.reset(); }
    }
    @Override public void onDisable() {
        synchronized (lock) { buffering = false; damageValidated = false; damageConsumed = false; groundTicks = Integer.MAX_VALUE; }
        PacketHold hold;
        synchronized (lock) {
            if (!canDelay()) groundTicks = Integer.MAX_VALUE;
            if (pending == null) return;
            hold = pending.blockRule();
            pending = null;
            closeEnough = false;
            groundTicks = Integer.MAX_VALUE;
        }
        Adjust.inboundPacketQueue.removeHold(hold);
    }
    @Override public String getSuffix() { return delay.getLong() + "ms"; }
    public boolean isDamageValid() { return !validateDamage.isEnabled() || damageValidated; }

    private static boolean isTargetOutOfReach() {
        var target = ServerTracker.INSTANCE.attackedLivingEntity;
        if (target == null || mc.theWorld == null || mc.theWorld.getEntityByID(target.getEntityId()) != target) return false;
        var snapshot = Adjust.entityTrackerService.tracker.get(target.getEntityId());
        if (snapshot == null) return false;
        double x = snapshot.serverX / 32.0, y = snapshot.serverY / 32.0, z = snapshot.serverZ / 32.0;
        float border = target.getCollisionBorderSize();
        AxisAlignedBB box = new AxisAlignedBB(x - 0.300000011920929, y, z - 0.300000011920929,
                x + 0.300000011920929, y + 1.7999999523162842, z + 0.300000011920929).expand(border, border, border);
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        return eyes.squareDistanceTo(DistanceUtil.closestPoint(eyes, box)) > 9.0;
    }
    private void dispatch(boolean force) {
        PacketHold hold;
        synchronized (lock) {
            boolean valid = canDelay();
            if (!valid) groundTicks = Integer.MAX_VALUE;
            if (pending == null) return;
            if (!force && canDelay()) {
                if (buffering) return;
                if (!pending.forced() && !pending.stopwatch().isExpired()
                        && !(groundEnabled.isEnabled() && !groundOnlyOnJump.isEnabled() && mc.thePlayer.onGround)
                        && !(closeDirection.isEnabled() && closeEnough)) return;
            }
            hold = pending.blockRule();
            pending = null;
            closeEnough = false;
            groundTicks = Integer.MAX_VALUE;
        }
        Adjust.inboundPacketQueue.removeHold(hold);
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || event.outgoing || !isEnabled()) return;
        if (!canDelay() || event.packet instanceof S08PacketPlayerPosLook) {
            synchronized (lock) { groundTicks = Integer.MAX_VALUE; }
            dispatch(true);
            return;
        }
        if (event.packet instanceof S19PacketEntityStatus status) {
            if (status.getEntity(mc.theWorld) == mc.thePlayer && status.getOpCode() == 2) {
                synchronized (lock) { if (isEnabled()) { damageValidated = true; damageConsumed = false; } }
            }
        } else if (event.packet instanceof S12PacketEntityVelocity velocity) {
            if (velocity.getEntityID() == mc.thePlayer.getEntityId())
                receiveVelocity(new Vec3(velocity.getMotionX() / 8000.0, velocity.getMotionY() / 8000.0, velocity.getMotionZ() / 8000.0), false);
        } else if (event.packet instanceof S27PacketExplosion explosion) {
            float x = explosion.func_149149_c(), y = explosion.func_149144_d(), z = explosion.func_149147_e();
            if (x == 0.0F && y == 0.0F && z == 0.0F) return;
            boolean buffer;
            synchronized (lock) {
                buffer = explosions.isEnabled() && !fireballTimer.hasElapsed(com.adjustclient.managers.util.ServiceUtil.addServerDelay(350));
            }
            if (buffer) receiveVelocity(new Vec3(x, y, z), true);
            else dispatch(true);
        }
    }

    private void receiveVelocity(Vec3 motion, boolean additive) {
        boolean flush = false;
        synchronized (lock) {
            if (!isEnabled() || !canDelay()) return;
            if (!buffering && groundEnabled.isEnabled() && !groundOnlyOnJump.isEnabled() && mc.thePlayer.onGround) {
                flush = true;
            } else if (pending != null) {
                Vec3 next = additive ? pending.motion().add(motion) : motion;
                pending = new DelayedVelocity(pending.blockRule(), pending.stopwatch(), next, pending.forced());
                closeEnough = false;
                return;
            } else {
                if (!isDamageValid()) return;
                if (!buffering) {
                    if (groundEnabled.isEnabled() && mc.thePlayer.onGround && mc.thePlayer.movementInput.jump) return;
                    if (!mc.thePlayer.onGround && groundTicks > delay.getLong() / 50L) return;
                }
                Vec3 next = additive ? motion.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ) : motion;
                PacketHold hold = Adjust.inboundPacketQueue.addHold(null, false);
                pending = new DelayedVelocity(hold, new Cooldown(delay.getLong()), next, buffering);
                closeEnough = false;
            }
        }
        if (flush) dispatch(true);
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        synchronized (lock) { buffering = false; damageValidated = false; damageConsumed = false; groundTicks = Integer.MAX_VALUE; }
        PacketHold hold;
        synchronized (lock) {
            if (!canDelay()) groundTicks = Integer.MAX_VALUE;
            if (pending == null) return;
            hold = pending.blockRule();
            pending = null;
            closeEnough = false;
            groundTicks = Integer.MAX_VALUE;
        }
        Adjust.inboundPacketQueue.removeHold(hold);
    }

    private static boolean canDelay() {
        return mc.thePlayer != null && !mc.thePlayer.isDead && mc.thePlayer.getHealth() > 0.0F
                && !MovementUtil.isMovementRestricted(mc.thePlayer);
    }
}
