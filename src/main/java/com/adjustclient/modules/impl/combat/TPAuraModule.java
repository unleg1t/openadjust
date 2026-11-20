package com.adjustclient.modules.impl.combat;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import com.adjustclient.core.rendering.RenderUtil;
import java.util.Comparator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.MathHelper;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.utilities.game.player.InventoryStateTracker;

public final class TPAuraModule extends Module implements MinecraftInstance {
    public static TPAuraModule INSTANCE;
    public final ModeSetting<TPAuraMode> mode;
    public final NumberSetting range;
    public final ExpandSetting targets;
    public final BooleanSetting players;
    public final BooleanSetting hostile;
    public final BooleanSetting teammates;
    public final BooleanSetting invisibles;
    public final BooleanSetting disableWhileMoving;
    public final BooleanSetting fakeBlock;
    private PacketHold hold;
    private TrackedLivingEntity target;
    private EntityLivingBase attackTarget;
    private int ticks;
    private boolean ready;

    public TPAuraModule() {
        super("TP Aura", Category.COMBAT);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", TPAuraMode.HOUSING, TPAuraMode.HOUSING);
        range = new NumberSetting(this, "Range", 20, 6, 20, 1);
        targets = new ExpandSetting(this, "Targets");
        players = new BooleanSetting(targets, "Players", true);
        hostile = new BooleanSetting(targets, "Hostile", false);
        teammates = new BooleanSetting(targets, "Teammates", false);
        invisibles = new BooleanSetting(targets, "Invisibles", true);
        disableWhileMoving = new BooleanSetting(this, "Disable While Moving", true);
        fakeBlock = new BooleanSetting(this, "Fake Block", true);
        fakeBlock.setDescription("Blocks your sword client side (visual effect only)");
    }
    public int getTargetMask() {
        int mask = 1024;
        if (players.isEnabled() && !players.isHidden()) mask |= 2;
        if (hostile.isEnabled() && !hostile.isHidden()) mask |= 4;
        if (teammates.isEnabled() && !teammates.isHidden()) mask |= 32;
        if (invisibles.isEnabled() && !invisibles.isHidden()) mask |= 64;
        return mask;
    }
    @EventHandler(priority = 48) public void onTick(IngameActionEvent event) {
        if (event.suppressActions || !isEnabled()) return;
        if (attackTarget != null) {
            RotationUtil.forceMouseOverEntity(attackTarget);
            Adjust.mouseClickHelper.left.clicks = 1;
            Adjust.mouseClickHelper.left.down = Boolean.TRUE;
            Adjust.mouseClickHelper.left.holdTicks = 0;
            attackTarget = null;
        }
        if (++ticks >= 2) releaseHold();
    }
    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        updateTarget();
        if (!ready) { ready = true; return; }
        if (target == null) return;
        if (hold != null) {
            EntityLivingBase entity = target.getEntity();
            event.x = entity.posX; event.y = entity.posY; event.z = entity.posZ;
            attackTarget = entity;
        } else { event.x = event.y = event.z = 0.0D; }
        mc.thePlayer.positionUpdateTicks = 20;
    }
    @EventHandler public void onMovement(UserInputVerificationEvent event) {
        if (!isEnabled()) return;
        if (mc.thePlayer != null && !InventoryStateTracker.isInServersideGui()) event.inputRequested = true;
    }
    @EventHandler public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || target == null) return;
        EntityLivingBase entity = target.getEntity();
        if (!entity.ignoreFrustumCheck && !RenderUtil.isInFrustum(EntityBoundsEvent.getRenderBoundingBox(entity))) return;
        var bounds = RenderUtil.projectEntityBounds(entity, event.scaledResolution, !target.isRemoved(), event.partialTicks);
        if (mc.entityRenderer.worldProjectionActive) mc.entityRenderer.setupOverlayRendering();
        if (bounds == null) return;
        java.awt.Color color = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        RenderUtil.glColor(color.getRGB());
        RenderUtil.drawLine2d((float) (event.scaledResolution.getScaledWidth_double() / 2.0D),
                (float) (event.scaledResolution.getScaledHeight_double() / 2.0D),
                ((float) bounds.z - (float) bounds.x) * 0.5F + (float) bounds.x,
                (float) bounds.y + ((float) bounds.w - (float) bounds.y) * 0.5F, 1.0F);
        net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, 1);
    }
    @EventHandler public void onItemInUse(ItemUseDurationEvent event) {
        if (!isEnabled() || !fakeBlock.isEnabled() || fakeBlock.isHidden()) return;
        var stack = mc.thePlayer.inventory.getCurrentItem();
        if (stack != null && stack.getItem() instanceof ItemSword && target != null) event.useDuration = 72000;
    }
    @Override public void onDisable() {
        if (hold != null) { Adjust.outboundPacketQueue.removeHold(hold); hold = null; ready = false; }
        target = null; attackTarget = null; ticks = 0;
    }
    @Override public String getSuffix() { return mode.getValue().getName(); }
    public void releaseHold() {
        if (hold == null) return;
        Adjust.outboundPacketQueue.removeHold(hold);
        hold = null; ready = false;
    }
    public void updateTarget() {
        if (disableWhileMoving.isEnabled() && !disableWhileMoving.isHidden() && isMoving()) { target = null; return; }
        var entities = ServerTracker.INSTANCE.entityTracker.getEntities(getTargetMask(), TrackedLivingEntity.class);
        int distance = range.getInt();
        entities.removeIf(entity -> entity.getEntity().getDistanceToEntity(mc.thePlayer) > (float) distance);
        if (entities.isEmpty()) { target = null; return; }
        entities.sort(Comparator.comparingDouble(entity -> {
            var playerRotation = com.adjustclient.core.rotation.handler.RenderRotationTracker.getRotation();
            float yaw = playerRotation == null ? mc.thePlayer.rotationYaw : playerRotation.x;
            float pitch = playerRotation == null ? mc.thePlayer.rotationPitch : playerRotation.y;
            var living = entity.getEntity();
            double best = Double.MAX_VALUE;
            for (double height : new double[] {0, living.getEyeHeight(), living.getEyeHeight() / 2.0D}) {
                Vec2f rotation = RotationUtil.getRotations(new net.minecraft.util.Vec3(living.posX, living.posY + height, living.posZ));
                double dy = MathHelper.wrapAngleTo180_float(yaw - rotation.x);
                double dp = pitch - rotation.y;
                best = Math.min(best, Math.sqrt(dy * dy + dp * dp));
            }
            return best;
        }));
        target = entities.get(0);
    }
    public static boolean isMoving() {
        var keys = mc.gameSettings;
        return keys.keyBindForward.isKeyDown() || keys.keyBindBack.isKeyDown() || keys.keyBindLeft.isKeyDown()
                || keys.keyBindRight.isKeyDown() || keys.keyBindJump.isKeyDown()
                || mc.thePlayer.onGround && keys.keyBindSneak.isKeyDown();
    }
    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (!event.outgoing && event.packet instanceof S08PacketPlayerPosLook && hold == null) {
            hold = Adjust.outboundPacketQueue.addHold(null, false);
            ticks = 0;
        }
    }
}
