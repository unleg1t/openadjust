package com.adjustclient.modules.impl.combat.velocity.mode;

import com.adjustclient.utilities.anticheat.feature.ModuleFeature;
import com.adjustclient.core.events.*;
import com.adjustclient.modules.impl.combat.velocity.*;
import com.adjustclient.core.rotation.InstantRotationModel;
import com.adjustclient.core.rotation.handler.RenderRotationTracker;
import com.adjustclient.core.rotation.handler.RotationHandler;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import com.adjustclient.modules.impl.movement.MovementFixModule;

public final class NormalVelocityMode extends VelocityMode {
    public static NormalVelocityMode INSTANCE;
    public final NumberSetting horizontal;
    public final NumberSetting vertical;
    public final NumberSetting explosionHorizontal;
    public final NumberSetting explosionVertical;
    public final BooleanSetting jumpResetEnabled;
    public final BooleanSetting jumpResetRotate;
    public final BooleanSetting validateDamage;
    private float velocityYaw;
    private boolean damage;
    private boolean receivedVelocity;
    private boolean resetJump;
    private boolean processed;

    public NormalVelocityMode(VelocityModule module) {
        super(module, "Normal");
        INSTANCE = this;
        ExpandSetting normal = new ExpandSetting(module, "Normal",
                () -> module.mode.getValue() != this);
        horizontal = new NumberSetting(normal, "Horizontal", 0, 0, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        vertical = new NumberSetting(normal, "Vertical", 0, 0, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        ExpandSetting explosion = new ExpandSetting(module, "Explosion",
                () -> module.mode.getValue() != this);
        explosionHorizontal = new NumberSetting(explosion, "Horizontal", 0, 0, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        explosionVertical = new NumberSetting(explosion, "Vertical", 0, 0, 100, 1).formatter(NumberSetting.INTEGER_SUFFIX_B);
        ExpandSetting addons = new ExpandSetting(module, "Addons",
                () -> module.mode.getValue() != this);
        ExpandSetting jumpReset = new ExpandSetting(addons, "Jump reset");
        jumpResetEnabled = new BooleanSetting(jumpReset, "Enabled", false, "Yes", "No");
        addons.enabledPredicate = () -> this.jumpResetEnabled.isEnabled() && !this.jumpResetEnabled.isHidden();
        jumpReset.enabledPredicate = () -> this.jumpResetEnabled.isEnabled() && !this.jumpResetEnabled.isHidden();
        jumpResetRotate = new BooleanSetting(jumpReset, "Rotate", true, "On", "Off");
        validateDamage = new BooleanSetting(addons, "Validate damage", true, "On", "Off");
        validateDamage.setDescription("Checks if the velocity you receive is linked to player damage.");
    }
    @Override public void reset() { receivedVelocity = damage = resetJump = processed = false; }
    @Override public String getSuffix() { return horizontal.getInt() + "% " + vertical.getInt() + "%"; }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive()) return;
        processed = false;
        if (receivedVelocity) {
            damage = false;
            receivedVelocity = false;
        }
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        if (!isActive()) return;
        receivedVelocity = false;
        damage = false;
    }

    @EventHandler(priority = 20) public void onRotate(IngameActionEvent event) {
        if (!isActive() || !resetJump || !jumpResetRotate.isEnabled() || jumpResetRotate.isHidden()) return;
        float yaw = velocityYaw - 180.0F;
        Vec2f camera = RenderRotationTracker.getRotation();
        float inputYaw = camera == null ? mc.thePlayer.rotationYaw : camera.x;
        float forward = (mc.gameSettings.keyBindForward.isKeyDown() ? 1.0F : 0.0F) - (mc.gameSettings.keyBindBack.isKeyDown() ? 1.0F : 0.0F);
        float strafe = (mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0F : 0.0F) - (mc.gameSettings.keyBindRight.isKeyDown() ? 1.0F : 0.0F);
        float factor = 1.0F;
        if (forward < 0.0F) { inputYaw += 180.0F; factor = -0.5F; }
        else if (forward > 0.0F) factor = 0.5F;
        if (strafe > 0.0F) inputYaw -= factor * 90.0F;
        if (strafe < 0.0F) inputYaw += factor * 90.0F;
        if (Math.abs(MathHelper.wrapAngleTo180_float(yaw - inputYaw)) >= 90.0F) return;
        RotationHandler state = RotationHandler.INSTANCE;
        float pitch = state.target == null ? mc.thePlayer.rotationPitch : state.target.y;
        state.target = new Vec2f(yaw, pitch);
        state.model = InstantRotationModel.INSTANCE;
        state.requested = state.active = true;
        RotationHandler.release();
    }
    @EventHandler public void onJumpKey(SprintEligibilityEvent event) {
        if (!isActive()) return;
        if (resetJump) mc.gameSettings.keyBindJump.pressed = true;
    }
    @EventHandler public void onPacket(IncomingVelocityEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null) return;
        if (event.packet instanceof S27PacketExplosion) {
            S27PacketExplosion packet = (S27PacketExplosion) event.packet;
            if (module.isSelfVelocity() && packet.func_149144_d() > 0.0F
                    && Math.hypot(packet.func_149149_c(), packet.func_149147_e()) > 0.0D) {
                module.selfVelocityTimer.lastMs = 0;
                module.explosionTimer.reset();
            }
            if (!module.explosionTimer.hasElapsed(300L)) {
                mc.thePlayer.motionX += packet.func_149149_c();
                mc.thePlayer.motionY += packet.func_149144_d();
                mc.thePlayer.motionZ += packet.func_149147_e();
            } else if (isDamageValid()) {
                double h = explosionHorizontal.getDouble() / 100.0D;
                double v = explosionVertical.getDouble() / 100.0D;
                if (v != 1.0D && !com.adjustclient.utilities.anticheat.feature.Features.VELOCITY_VERTICAL.isAllowed()) v = 1.0D;
                mc.thePlayer.motionX += packet.func_149149_c() * h;
                mc.thePlayer.motionY += packet.func_149144_d() * v;
                mc.thePlayer.motionZ += packet.func_149147_e() * h;
                processJumpReset();
            }
            receivedVelocity = true;
            packet.suppressPlayerMotion = true;
            return;
        }
        if (!(event.packet instanceof S12PacketEntityVelocity) || event.suppressApplication) return;
        S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.packet;
        if (packet.getEntityID() != mc.thePlayer.getEntityId() || !isDamageValid()) return;
        receivedVelocity = true;
        double x = packet.getMotionX() / 8000.0D, y = packet.getMotionY() / 8000.0D, z = packet.getMotionZ() / 8000.0D;
        if (!module.explosionTimer.hasElapsed(300L)) {
            mc.thePlayer.motionY = y;
            mc.thePlayer.motionX = x;
            mc.thePlayer.motionZ = z;
            event.suppressApplication = true;
            return;
        }
        double h = (module.explosion ? explosionHorizontal : horizontal).getDouble() / 100.0D;
        double v = (module.explosion ? explosionVertical : vertical).getDouble() / 100.0D;
        if (v != 1.0D && !com.adjustclient.utilities.anticheat.feature.Features.VELOCITY_VERTICAL.isAllowed()) v = 1.0D;
        if (h != 0.0D) { mc.thePlayer.motionX = x * h; mc.thePlayer.motionZ = z * h; }
        if (v != 0.0D) mc.thePlayer.motionY = y * v;
        processJumpReset();
        event.suppressApplication = true;
        module.explosion = false;
    }
    @EventHandler(priority = -5) public void onMovementInput(MovementInputEvent event) {
        if (!isActive() || !resetJump) return;
        if (jumpResetRotate.isEnabled() && !jumpResetRotate.isHidden() && MovementFixModule.INSTANCE.isEnabled()) {
            event.back = false;
            event.forward = true;
            event.left = false;
            event.right = false;
        }
        event.jump = true;
        resetJump = false;
    }
    public boolean isDamageValid() { return !validateDamage.isEnabled() || validateDamage.isHidden() || damage; }
    public static boolean canJumpReset() {
        return !mc.thePlayer.isCollidedHorizontally && mc.thePlayer.motionY >= 0.0D && !mc.thePlayer.isPotionActive(Potion.jump);
    }
    public void processJumpReset() {
        if (processed) return;
        processed = true;
        if (jumpResetEnabled.isEnabled() && !jumpResetEnabled.isHidden() && mc.thePlayer.onGround && canJumpReset()) resetJump = true;
        velocityYaw = (float) (Math.atan2(-mc.thePlayer.motionX, mc.thePlayer.motionZ) * 57.29577951308232D);
    }
    @EventHandler public void onEntityStatus(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null || event.outgoing || !(event.packet instanceof S19PacketEntityStatus)) return;
        S19PacketEntityStatus packet = (S19PacketEntityStatus) event.packet;
        if (packet.getEntity(mc.theWorld) == mc.thePlayer && packet.getOpCode() == 2) {
            receivedVelocity = false;
            damage = true;
        }
    }
}
