package com.adjustclient.module.impl.movement.flight;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveEvent;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.event.RewoundDisablerEvent;
import com.adjustclient.event.TeleportEvent;
import com.adjustclient.game.packet.PacketHoldHandle;
import com.adjustclient.game.player.SlotSpoofer;
import com.adjustclient.game.render.progress.ProgressBar;
import com.adjustclient.game.rewound.RewoundDisablerType;
import com.adjustclient.game.rewound.RewoundGame;
import com.adjustclient.game.rewound.RewoundGameState;
import com.adjustclient.module.util.MouseClickHelper;
import com.adjustclient.module.util.SwingMouseClick;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.WorldUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;

public final class RewoundFlightMode extends FlightMode {
    public static RewoundFlightMode INSTANCE;

    public final NumberSetting speed;
    public final ModeSetting<DamageMode> damage;
    public final BooleanSetting timerEnabled;
    public final NumberSetting timerSpeed;
    public final NumberSetting timerTime;
    public final BooleanSetting dropEnabled;
    public final NumberSetting dropSpeed;
    public final BooleanSetting disablerEnabled;
    public final BooleanSetting enderPearl;
    public final BooleanSetting fireball;
    public final BooleanSetting packet;
    public final NumberSetting horizontalSpeed;
    public final NumberSetting verticalSpeed;
    public final BooleanSetting progress;
    public final ModeSetting<BobbingMode> bobbing;

    private State state;
    private Timer timerBoostTimer;
    private final Timer xprossTimer = new Timer(0L);
    private final PacketHoldHandle blinker = new PacketHoldHandle(Adjust.outboundPacketQueue);
    private ProjectileStage projectileStage;
    private Timer hoverTimer;
    private double moveSpeed;
    private int stage;
    private int groundTicks;
    private int projectileSlot;
    private boolean waitingForDamage;

    public RewoundFlightMode(FlightModule module) {
        super(module, "Rewound");
        INSTANCE = this;

        BooleanSupplier hideIf = () -> module.mode.getValue() != this;

        speed = new NumberSetting(module, "Speed", 1.0D, 0.5D, 5.0D, 0.05D, hideIf);
        speed.setDisplayName("rewoundSpeed");

        damage = new ModeSetting<>(module, "Damage", DamageMode.NORMAL, hideIf, DamageMode.values());
        damage.setDisplayName("rewoundDamage1337");

        ExpandSetting timer = new ExpandSetting(module, "Timer", hideIf);
        timer.setDisplayName("rewoundTimer");
        timerEnabled = new BooleanSetting(timer, "Enabled", true, "Yes", "No");
        timer.setActiveSupplier(timerEnabled::isEnabled);
        timerSpeed = new NumberSetting(timer, "Speed", 2.0D, 0.5D, 10.0D, 0.05D).formatter(NumberSetting.MULTIPLIER);
        timerTime = new NumberSetting(timer, "Time", 500, 5, 2000, 5).formatter(NumberSetting.MILLISECONDS);

        ExpandSetting drop = new ExpandSetting(module, "Drop", hideIf);
        drop.setDisplayName("rewoundDrop");
        dropEnabled = new BooleanSetting(drop, "Enabled", true, "Yes", "No");
        drop.setActiveSupplier(dropEnabled::isEnabled);
        dropSpeed = new NumberSetting(drop, "Speed", 4, 0.5D, 10, 0.1D);

        ExpandSetting disabler = new ExpandSetting(module, "Disabler", hideIf);
        disabler.setDisplayName("rewoundDisabler");
        disablerEnabled = new BooleanSetting(disabler, "Enabled", true, "Yes", "No");
        disabler.setActiveSupplier(disablerEnabled::isEnabled);

        ExpandSetting methods = new ExpandSetting(disabler, "Methods");
        enderPearl = new BooleanSetting(methods, "Ender pearl", true);
        fireball = new BooleanSetting(methods, "Fireball", true);
        packet = new BooleanSetting(methods, "Packet", true);

        horizontalSpeed = new NumberSetting(disabler, "Horizontal speed", 4.0D, 0.5D, 10.0D, 0.1D);
        verticalSpeed = new NumberSetting(disabler, "Vertical speed", 2.0D, 0.5D, 10.0D, 0.1D);
        progress = new BooleanSetting(disabler, "Progress", true)
                .description("Renders the progress until you stop flying.");

        bobbing = new ModeSetting<>(module, "Bobbing", BobbingMode.NONE, hideIf, BobbingMode.values());
        bobbing.setDisplayName("rewoundBobbing");
    }

    @Override
    public void onEnable() {
        state = null;
        stage = 0;
        moveSpeed = 0.0D;
        timerBoostTimer = null;
        groundTicks = 0;
        hoverTimer = null;
        waitingForDamage = false;

        if (mc.thePlayer == null) {
            return;
        }
        if (!Features.FLIGHT_REWOUND.isAllowed()) {
            return;
        }
        if (disablerEnabled.isEnabled()) {
            if (Adjust.rewoundDisablerTracker.isActive()) {
                state = State.DISABLER;
                return;
            }
            if (tryStartDisabler()) {
                return;
            }
        }
        if (mc.thePlayer.onGround) {
            state = State.DAMAGE;
        } else if (dropEnabled.isEnabled() && mc.thePlayer.fallDistance > 4.0F) {
            state = State.XPROSS;
            xprossTimer.reset();
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            double baseSpeed = MovementUtil.getSpeedWithPotion(0.2873D, 0.2D);
            if (MovementUtil.getSpeed() > baseSpeed) {
                MovementUtil.stop();
                if (state == State.DISABLER) {
                    mc.thePlayer.motionY = 0.0D;
                }
            }
        }
        blinker.release();
    }

    private boolean tryStartDisabler() {
        if (!mc.thePlayer.onGround
                && packet.isEnabled()
                && !MovementUtil.isMoving()
                && (mc.thePlayer.fallDistance < 4.0F || !dropEnabled.isEnabled())) {
            state = State.DISABLER_HOVER;
            return true;
        }
        if (enderPearl.isEnabled()) {
            Integer slot = WorldUtil.findHotbarSlot(mc.thePlayer.inventory.currentItem, index -> {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(index);
                return stack != null && stack.getItem() instanceof ItemEnderPearl;
            });
            if (slot != null) {
                projectileSlot = slot;
                projectileStage = ProjectileStage.ROTATE;
                state = State.DISABLER_PEARL;
                return true;
            }
        }
        if (fireball.isEnabled() && Adjust.rewoundGameTracker.state.game == RewoundGame.BEDWARS) {
            Integer slot = WorldUtil.findHotbarSlot(mc.thePlayer.inventory.currentItem, index -> {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(index);
                return stack != null && stack.getItem() instanceof ItemFireball;
            });
            if (slot != null) {
                projectileSlot = slot;
                projectileStage = ProjectileStage.ROTATE;
                state = State.DISABLER_FIREBALL;
                return true;
            }
        }
        return false;
    }

    private boolean canSelfDamage() {
        if (damage.getValue() == DamageMode.NONE) {
            return false;
        }
        RewoundGameState gameState = Adjust.rewoundGameTracker.state;
        if (gameState.lobby == null || gameState.lobby) {
            return false;
        }
        if ((gameState.game.flags & 4) != 0) {
            return false;
        }
        if (gameState.game == RewoundGame.BEDWARS) {
            return gameState.isStarted();
        }
        return gameState.startTimer != null && !gameState.ended;
    }

    private boolean sendSelfDamagePackets() {
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;
        double fallDistance = 3.1D;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            fallDistance += (float) (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1);
        }
        double step = 0.06249D;
        List<Packet<?>> packets = new ArrayList<>();
        for (double distance = 0.0D; distance < fallDistance; distance += step) {
            packets.add(new C03PacketPlayer.C04PacketPlayerPosition(x, y + step, z, false));
            packets.add(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false));
        }
        packets.add(new C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true));

        if (795 - packets.size() > Adjust.rewoundDisablerTracker.sentPackets.size()) {
            for (Packet<?> pkt : packets) {
                mc.getNetHandler().getNetworkManager().sendPacketNoEvent(pkt);
            }
            float slowdown = packets.size() * 0.001F;
            mc.timer.setTimerSpeed(slowdown > 0.2F ? 0.8F : 1.0F - slowdown);
            mc.thePlayer.positionUpdateTicks = 0;
            return true;
        }
        return false;
    }

    private static double calculateFriction(double lastDistance, double baseSpeed) {
        return Math.max(
                Math.max(lastDistance - lastDistance / 160.0D, lastDistance - (lastDistance - baseSpeed) / 33.3D),
                lastDistance - (lastDistance - baseSpeed) * 0.02D
        );
    }

    private void spoofProjectileSlot() {
        SlotSpoofer spoofer = Adjust.slotSpoofer;
        spoofer.slot = projectileSlot;
        spoofer.active = true;
        spoofer.field8 = spoofer.field12;
        spoofer.field17 = false;
        spoofer.renderSpoofed = false;
    }

    @EventHandler
    public void onProjectileAction(IngameActionEvent event) {
        if (!isActive() || event.suppressActions) {
            return;
        }
        if (state != State.DISABLER_FIREBALL && state != State.DISABLER_PEARL) {
            return;
        }
        switch (projectileStage) {
            case ROTATE:
                Vec2f renderRot = RenderRotationTracker.INSTANCE.rotation;
                float yaw = renderRot != null ? renderRot.x : mc.thePlayer.rotationYaw;
                RotationHandler.INSTANCE.rotate(InstantRotationModel.INSTANCE, yaw, 90.0F);
                spoofProjectileSlot();
                projectileStage = ProjectileStage.THROW;
                break;
            case THROW:
                SwingMouseClick click = ((MouseClickHelper) Adjust.mouseClickHelper).right;
                click.clicks = 1;
                click.down = Boolean.TRUE;
                click.holdTicks = 0;
                click.swing = false;
                spoofProjectileSlot();
                projectileStage = ProjectileStage.WAITING;
                break;
            case WAITING:
            default:
                break;
        }
    }

    @EventHandler
    public void onXprossAction(IngameActionEvent event) {
        if (!isActive()) {
            return;
        }
        if (state == State.XPROSS && blinker.hold != null) {
            WorldUtil.clearMouseOver();
            mc.leftClickCounter = 0;
            MouseClickHelper clicks = (MouseClickHelper) Adjust.mouseClickHelper;
            clicks.right.disabled = true;
            clicks.left.disabled = true;
        }
    }

    @EventHandler
    public void onTeleport(TeleportEvent event) {
        if (!isActive()) {
            return;
        }
        if (event.pre && state == State.DAMAGE) {
            module.setEnabled(false, true);
            new NotificationBuilder(module.getName()).format("Automatically disabled due to lag back.").show();
        }
    }

    @EventHandler
    public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive()) {
            return;
        }
        if (state == null) {
            module.setEnabled(false, true);
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive() || !event.pre) {
            return;
        }
        if (state == State.DAMAGE) {
            if (stage == 0) {
                if (mc.thePlayer.onGround && mc.thePlayer.lastDistance > 0.0D) {
                    if (canSelfDamage() && sendSelfDamagePackets()) {
                        if (damage.getValue() == DamageMode.WAIT) {
                            waitingForDamage = true;
                        } else {
                            stage = 1;
                        }
                    } else {
                        moveSpeed = 0.0D;
                        stage = 4;
                    }
                    if (timerEnabled.isEnabled()) {
                        timerBoostTimer = new Timer();
                    }
                }
            } else {
                if ((groundTicks & 1) == 0) {
                    double offset = 1.0E-9D - RandomUtil.RANDOM.nextDouble() * 9.9999E-10D;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset, mc.thePlayer.posZ);
                }
                groundTicks++;
            }
            if (bobbing.getValue() == BobbingMode.NORMAL_ONLY) {
                mc.thePlayer.cameraYaw = 0.1F;
            }
        } else if (state == State.DISABLER_HOVER) {
            if (hoverTimer == null) {
                if (mc.thePlayer.posY - mc.thePlayer.lastGroundY < 0.1D) {
                    double x = mc.thePlayer.posX;
                    double y = mc.thePlayer.posY;
                    double z = mc.thePlayer.posZ;
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(
                            new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.2D, z, false));
                    mc.getNetHandler().getNetworkManager().sendPacketNoEvent(
                            new C03PacketPlayer.C04PacketPlayerPosition(x, y + 0.1D, z, false));
                    mc.thePlayer.setPosition(x, y + 0.1D, z);
                    event.y = mc.thePlayer.posY;
                }
                hoverTimer = new Timer();
                new NotificationBuilder(module.getName()).format("Getting ready to fly.").show();
            }
        }
        if (bobbing.getValue() == BobbingMode.ALWAYS) {
            mc.thePlayer.cameraYaw = 0.1F;
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!isActive()) {
            return;
        }
        if (state == State.DISABLER_HOVER) {
            if (hoverTimer != null) {
                float pct = Math.min(1.0F, (float) (System.currentTimeMillis() - hoverTimer.lastMs) / 4250.0F);
                Adjust.progressBarRenderer.bars.add(new ProgressBar(pct));
            }
        } else if (state == State.DISABLER) {
            if (progress.isEnabled()) {
                Adjust.rewoundDisablerTracker.renderProgress();
            }
        }
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (!isActive() || state == null) {
            return;
        }
        switch (state) {
            case DAMAGE: {
                double baseSpeed = MovementUtil.getSpeedWithPotion(0.2873D, 0.2D);
                if (stage == 0) {
                    moveSpeed = waitingForDamage ? 0.0D : baseSpeed;
                } else if (stage == 1) {
                    mc.thePlayer.motionY = 0.0D;
                    moveSpeed = speed.getDouble();
                } else if (stage == 2) {
                    mc.thePlayer.motionY = MovementUtil.getJumpMotion((double) 0.42F);
                    moveSpeed *= 2.145D;
                } else if (stage == 3) {
                    mc.thePlayer.motionY = 0.0D;
                    moveSpeed = mc.thePlayer.lastDistance - (mc.thePlayer.lastDistance - baseSpeed) * 0.66D;
                } else {
                    moveSpeed = calculateFriction(mc.thePlayer.lastDistance, baseSpeed) - 0.00001D;
                    mc.thePlayer.motionY = 0.0D;
                    if (mc.thePlayer.onGround && moveSpeed > baseSpeed) {
                        module.setEnabled(false, true);
                        return;
                    }
                }
                if (Adjust.movementPredictor.historyTicks < 10) {
                    moveSpeed = 0.0D;
                    if (!waitingForDamage) {
                        moveSpeed = Math.max(0.0D, baseSpeed);
                    }
                } else if (!waitingForDamage) {
                    moveSpeed = Math.max(moveSpeed, baseSpeed);
                }
                MovementUtil.setSpeed(moveSpeed);
                if (stage >= 1) {
                    if (timerBoostTimer != null) {
                        if (timerBoostTimer.hasElapsed(timerTime.getLong())) {
                            timerBoostTimer = null;
                        } else {
                            mc.timer.setTimerSpeed(timerSpeed.getFloat());
                        }
                    }
                    stage++;
                }
                break;
            }
            case DISABLER: {
                if (!Adjust.rewoundDisablerTracker.isActive()) {
                    module.setEnabled(false, true);
                } else {
                    double motionY = 0.0D;
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        motionY += verticalSpeed.getDouble();
                    }
                    if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        motionY -= verticalSpeed.getDouble();
                    }
                    mc.thePlayer.motionY = motionY;
                    MovementUtil.setSpeed(horizontalSpeed.getDouble());
                }
                break;
            }
            case XPROSS: {
                if (mc.thePlayer.onGround || xprossTimer.hasElapsed(3000L)) {
                    module.setEnabled(false, true);
                    return;
                }
                if (mc.thePlayer.getHealth() < 6.0F) {
                    module.setEnabled(false, true);
                    new NotificationBuilder(module.getName())
                            .format("Automatically disabled due to being on low health.")
                            .show();
                    return;
                }
                MovementUtil.setSpeed(dropSpeed.getDouble());
                mc.thePlayer.motionY = 0.0D;
                Vec3 nextPos = new Vec3(
                        mc.thePlayer.posX + mc.thePlayer.motionX,
                        mc.thePlayer.posY + mc.thePlayer.motionY,
                        mc.thePlayer.posZ + mc.thePlayer.motionZ
                );
                if (WorldUtil.isOverVoid() || WorldUtil.isFreeAt(nextPos, 0.0D)) {
                    blinker.acquire(blinker.field16);
                } else {
                    blinker.release();
                }
                break;
            }
            case DISABLER_PEARL: {
                if (projectileStage != ProjectileStage.WAITING) {
                    MovementUtil.stop();
                }
                break;
            }
            case DISABLER_FIREBALL: {
                MovementUtil.stop();
                break;
            }
            case DISABLER_HOVER: {
                MovementUtil.stop();
                mc.thePlayer.motionY = 0.0D;
                break;
            }
        }
        event.x = mc.thePlayer.motionX;
        event.y = mc.thePlayer.motionY;
        event.z = mc.thePlayer.motionZ;
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null || event.outgoing) {
            return;
        }
        Packet<?> pkt = event.packet;
        if (state == State.DISABLER_HOVER && hoverTimer != null && pkt instanceof S08PacketPlayerPosLook) {
            if (!hoverTimer.hasElapsed(3920L)) {
                module.setEnabled(false, true);
                new NotificationBuilder("Error").format("Failed to disable movement checks.").show();
            } else {
                Adjust.rewoundDisablerTracker.trigger(RewoundDisablerType.DISABLER);
                state = State.DISABLER;
                new NotificationBuilder(module.getName()).format("You can fly for 3 seconds.").show();
            }
        } else if (state == State.DAMAGE && stage == 0 && waitingForDamage && pkt instanceof S19PacketEntityStatus) {
            S19PacketEntityStatus status = (S19PacketEntityStatus) pkt;
            if (status.getOpCode() == 2 && status.entityId == mc.thePlayer.getEntityId()) {
                waitingForDamage = false;
                stage = 1;
            }
        }
    }

    @EventHandler
    public void onRewoundDisabler(RewoundDisablerEvent event) {
        if (!isActive()) {
            return;
        }
        if (state == State.DISABLER_PEARL
                && projectileStage == ProjectileStage.WAITING
                && event.type == RewoundDisablerType.ENDER_PEARL) {
            new NotificationBuilder(module.getName()).format("You an fly for 3 seconds.").show();
        }
        state = State.DISABLER;
    }

    public enum DamageMode implements ModeOption {
        NORMAL("Normal"),
        WAIT("Wait"),
        NONE("None");

        private final String name;

        DamageMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public enum BobbingMode implements ModeOption {
        NONE("None"),
        NORMAL_ONLY("Normal only"),
        ALWAYS("Always");

        private final String name;

        BobbingMode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public enum State {
        DAMAGE,
        XPROSS,
        DISABLER_PEARL,
        DISABLER_FIREBALL,
        DISABLER_HOVER,
        DISABLER
    }

    public enum ProjectileStage {
        ROTATE,
        THROW,
        WAITING
    }
}
