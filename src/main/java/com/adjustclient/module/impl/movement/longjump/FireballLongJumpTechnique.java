package com.adjustclient.module.impl.movement.longjump;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindAction;
import com.adjustclient.event.*;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import com.adjustclient.util.simulation.FireballSimulation;
import java.util.LinkedList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.*;
import com.adjustclient.module.impl.render.hud.HUDModule;

public final class FireballLongJumpTechnique extends HypixelLongJumpTechnique {
    public static FireballLongJumpTechnique INSTANCE;
    public final Bind moreFireballs;
    public final BooleanSetting maximizeOnEdge;
    public final BooleanSetting hideExplosion;
    public final BooleanSetting hud;
    private FireballPhase phase;

    public boolean isHidingWaitingExplosion() {
        return isActive() && phase == FireballPhase.WAITING && hideExplosion.isEnabled();
    }
    private final Timer timer = new Timer(0L);
    private Vec2f rotation;
    private Integer progressLength;
    private Integer progressTicks;
    private final LinkedList<FireballThrow> throwsInFlight = new LinkedList<>();
    private int field52;
    private int fireballsRemaining;
    private int ticks;
    private int startY;
    private boolean prepared;
    private boolean field69;
    private boolean field70;
    private boolean field71;

    public FireballLongJumpTechnique(HypixelLongJumpMode hypixel) {
        super(hypixel);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(module, "Fireball");
        moreFireballs = Adjust.INSTANCE.bindManager.register("module.longjump.fbmoreexplosions",
                mode -> { }, "Fireball LongJump More Explosions");
        KeybindSetting more = new KeybindSetting(group, "More Fireballs", moreFireballs);
        more.setDescription("Holding down this bind makes you throw more Fireballs to go further.");
        maximizeOnEdge = new BooleanSetting(group, "Maximize on edge", true);
        maximizeOnEdge.setDescription("Walks until you hit the closest edge to maximize your distance jumped.");
        hideExplosion = new BooleanSetting(group, "Hide explosion", true);
        hideExplosion.setDescription("Hide the Fireball explosion effect.");
        hud = new BooleanSetting(group, "HUD", true);
    }

    @Override public void onDisable() {
        while (!throwsInFlight.isEmpty()) Adjust.inboundPacketQueue.removeHold(throwsInFlight.removeFirst().hold);
        field70 = false;
        progressLength = null;
        progressTicks = null;
        rotation = null;
        field71 = false;
        prepared = false;
        field69 = false;
    }

    @EventHandler
    public void onPreTick(PreClientTickEvent event) {
        if (!isActive()) return;
        updateLaunch();
    }

    public void updateLaunch() {
        if (!prepared) {
            checkEdge();
            if (!prepared) return;
        }
        switch (phase.ordinal()) {
            case 0:
                prepareThrow();
                phase = FireballPhase.THROW;
                break;
            case 1:
                boolean more = moreFireballs.isDown();
                if (ticks >= 2 && more) prepareThrow();
                if (timer.hasElapsed(520L)) {
                    var use = Adjust.mouseClickHelper.right;
                    use.clicks = 1;
                    use.down = Boolean.TRUE;
                    use.holdTicks = 0;
                    use.previousDown = false;
                    if (fireballsRemaining-- == 1 || !more) phase = FireballPhase.WAITING;
                    timer.reset();
                }
                break;
            case 2:
                if (!throwsInFlight.isEmpty()) {
                    var watcher = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
                    int average = watcher == null ? 0 : watcher.packetWatcher.delayCount == 0 ? 0
                            : watcher.packetWatcher.totalDelay / watcher.packetWatcher.delayCount;
                    if (timer.hasElapsed((long) (int) (average * 1.25) + 50L)) phase = FireballPhase.DONE;
                }
                break;
        }
    }

    @EventHandler public void onGameLoop(MoveFlyingEvent event) {
        if (!isActive() || event.pre || phase != FireballPhase.DONE) return;
        double motionY = mc.thePlayer.motionY;
        if (mc.thePlayer.onGround) {
            if (field69) module.setEnabled(false, true);
            else if (field71 || !BlockUtil.isColliding(mc.thePlayer.getEntityBoundingBox().offset(
                    mc.thePlayer.motionX, motionY, mc.thePlayer.motionZ))) {
                mc.thePlayer.jump();
                startY = (int) mc.thePlayer.posY;
                field69 = true;
                if (hud.isEnabled()) {
                    progressLength = 10;
                    progressTicks = 0;
                }
            }
        } else {
            boolean release = false;
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                if (!field70) { field70 = true; release = true; }
            } else field70 = false;
            if (startY >= (motionY - 0.08) * 0.9800000190734863 + motionY + mc.thePlayer.posY) release = true;
            if (release && !throwsInFlight.isEmpty()) {
                FireballThrow next = throwsInFlight.removeFirst();
                if (hud.isEnabled() && !throwsInFlight.isEmpty()) {
                    progressLength = ticksToStartHeight(next.motionY) - 2;
                    progressTicks = 0;
                } else { progressLength = null; progressTicks = null; }
                Adjust.inboundPacketQueue.processNow(() -> Adjust.inboundPacketQueue.removeHold(next.hold));
            }
        }
        if (progressTicks != null) ++progressTicks;
    }

    @EventHandler public void onWorldChange(UserInputVerificationEvent event) {
        if (isActive() && !event.inputRequested
                && phase != FireballPhase.DONE && phase != FireballPhase.WAITING)
            module.setEnabled(false, true);
    }

    @EventHandler public void onRender2D(Render2DEvent event) {
        if (!isActive() || progressLength == null || progressTicks == null || progressTicks > progressLength) return;
        float progress = MathHelper.clamp_float((float) progressTicks / progressLength, 0.0F, 1.0F);
        Adjust.progressBarRenderer.bars.add(new com.adjustclient.game.render.progress.ProgressBar(progress));
    }

    public Vec2f findThrow(net.minecraft.entity.EntityLivingBase player) {
        float forward = mc.gameSettings.keyBindForward.isKeyDown() ? 1.0F : 0.0F;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward = forward == 1.0F ? 0.0F : -1.0F;
        float strafe = mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0F : 0.0F;
        if (mc.gameSettings.keyBindRight.isKeyDown()) strafe = strafe == 1.0F ? 0.0F : -1.0F;
        float yaw = MovementUtil.getDirection(mc.thePlayer.rotationYaw, forward, strafe);
        float pitchStep = RandomUtil.jitter(0.21F, 0.005F);
        float yawStep = RandomUtil.jitter(0.8F, 0.1F);
        float firstPitch = RandomUtil.jitter(58.5F, 0.005F);
        float yawRange = RandomUtil.nextFloat(179.89999F, 180.0F);
        for (float pitch = firstPitch; pitch < 80.0F; pitch += pitchStep) {
            for (float sign = -1.0F; sign <= 1.0F; sign += 2.0F) {
                for (float angle = yaw + sign * yawRange; Math.abs(angle - yaw) < 180.0F; angle += yawStep * sign) {
                    FireballSimulation simulation = new FireballSimulation();
                    simulation.initialize(player, player.posX, player.posY, player.posZ, angle, pitch);
                    for (int tick = 0; tick < 3; ++tick) {
                        simulation.onUpdate();
                        if (simulation.collision != null
                                && simulation.collision.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                            return new Vec2f(angle, pitch);
                    }
                }
            }
        }
        return null;
    }

    @Override public void onEnable() {
        int slot = -1;
        net.minecraft.item.ItemStack stack = null;
        for (int i = 0; i < 9; ++i) {
            net.minecraft.item.ItemStack candidate = mc.thePlayer.inventory.mainInventory[i];
            if (candidate != null && candidate.getItem() == net.minecraft.init.Items.fire_charge) {
                slot = i;
                stack = candidate;
                break;
            }
        }
        if (slot == -1) {
            var notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = module.getName();
            notification.key = "longjump.nofireball";
            notification.duration = 30000L;
            notification.addText("You don't have a Fireball in your hotbar.", (int) 4290690237L);
            notification.show();
            module.setEnabled(false, true);
            return;
        }
        phase = FireballPhase.ROTATE;
        ticks = fireballsRemaining = Math.min(2, stack.stackSize);
        field52 = slot;
        if (maximizeOnEdge.isEnabled() && mc.thePlayer.onGround) {
            Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
            for (int tick = 0; tick < 14; ++tick) {
                simulation.player.tick();
                if (mc.thePlayer.posY > simulation.player.posY) return;
            }
        }
        field71 = true;
        prepared = true;
    }

    @Override public String getName() { return "Fireball"; }

    public void prepareThrow() {
        if (rotation == null) rotation = findThrow(mc.thePlayer);
        if (rotation == null) {
            var notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = module.getName();
            notification.addText("There are no possible throws at your location, find a place with blocks beneath you.", (int) 4290690237L);
            com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.notifications.add(notification.build());
            module.setEnabled(false, true);
            return;
        }
        var handler = com.adjustclient.rotation.handler.RotationHandler.INSTANCE;
        handler.target = new Vec2f(rotation.x, rotation.y);
        handler.model = com.adjustclient.rotation.InstantRotationModel.INSTANCE;
        handler.active = true;
        handler.requested = true;
        com.adjustclient.rotation.handler.RotationHandler.release();
        Adjust.slotSpoofer.slot = field52;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = false;
        Adjust.slotSpoofer.renderSpoofed = false;
    }

    public void checkEdge() {
        if (prepared) return;
        Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
        for (int tick = 0; tick < 4; ++tick) simulation.player.tick();
        if (mc.thePlayer.posY > simulation.player.posY) prepared = true;
    }

    public void releaseThrows() {
        while (!throwsInFlight.isEmpty()) Adjust.inboundPacketQueue.removeHold(throwsInFlight.removeFirst().hold);
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isActive() || event.outgoing) return;
        if (event.packet instanceof S08PacketPlayerPosLook) {
            module.setEnabled(false, true);
            return;
        }
        if (phase != FireballPhase.DONE && hideExplosion.isEnabled() && mc.thePlayer != null) {
            double x = 0, y = 0, z = 0;
            boolean explosion = false;
            if (event.packet instanceof S29PacketSoundEffect) {
                S29PacketSoundEffect sound = (S29PacketSoundEffect) event.packet;
                explosion = sound.getSoundName().equals("random.explode") || sound.getSoundName().equals("random.bowhit");
                x = sound.getX(); y = sound.getY(); z = sound.getZ();
            } else if (event.packet instanceof S2APacketParticles) {
                S2APacketParticles particles = (S2APacketParticles) event.packet;
                explosion = particles.getParticleType() == EnumParticleTypes.EXPLOSION_HUGE
                        || particles.getParticleType() == EnumParticleTypes.EXPLOSION_LARGE;
                x = particles.getXCoordinate(); y = particles.getYCoordinate(); z = particles.getZCoordinate();
            }
            if (explosion && mc.thePlayer.getDistanceSq(x, y - mc.thePlayer.getEyeHeight(), z) <= 4.0) event.cancelled = true;
        }
        if (event.packet instanceof S12PacketEntityVelocity && mc.thePlayer != null) {
            S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) event.packet;
            if (velocity.getEntityID() == mc.thePlayer.getEntityId()) {
                throwsInFlight.add(new FireballThrow(Adjust.inboundPacketQueue.addHold(null, false),
                        velocity.getMotionY() / 8000.0, null));
                if (phase == FireballPhase.WAITING) phase = FireballPhase.DONE;
            }
        }
    }

    @EventHandler public void onPacketReceived(com.adjustclient.event.PacketAppliedEvent event) {
        if (!isActive() || phase != FireballPhase.WAITING || !hideExplosion.isEnabled()) return;
        if (!(event.packet instanceof S0EPacketSpawnObject)) return;
        S0EPacketSpawnObject spawn = (S0EPacketSpawnObject) event.packet;
        if (spawn.getType() != 63 && spawn.getType() != 60) return;
        if (mc.thePlayer.getDistanceSq(spawn.getX() / 32.0,
                spawn.getY() / 32.0 - mc.thePlayer.getEyeHeight(), spawn.getZ() / 32.0) > 4.0) return;
        Entity entity = mc.theWorld.getEntityByID(spawn.getEntityID());
        if (entity != null) entity.forceInvisible = true;
    }

    public int ticksToStartHeight(double motionY) {
        double y = mc.thePlayer.posY;
        int count = 0;
        while (y > startY) {
            ++count;
            y += motionY;
            motionY = (motionY - 0.08) * 0.9800000190734863;
        }
        return count;
    }
}
