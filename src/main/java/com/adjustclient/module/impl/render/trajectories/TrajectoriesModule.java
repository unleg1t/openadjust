package com.adjustclient.module.impl.render.trajectories;

import com.adjustclient.Adjust;
import com.adjustclient.event.DebugCoordinatesEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.render.trajectories.predictor.*;
import com.adjustclient.module.impl.render.trajectories.settings.*;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.setting.*;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.SimulationWorld;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import com.adjustclient.util.simulation.*;
import java.awt.Color;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import net.minecraft.init.Items;

public final class TrajectoriesModule extends Module implements MinecraftInstance {
    public static final Map<Item, TrajectoryTarget> PREDICTORS;
    public static final ThrowableProjectilePredictor SHARED_THROWABLE;
    public static final ThrowableProjectilePredictor SHARED_POTION;
    public static final HeldProjectilePredictor SHARED_ARROW;
    public static final FireballPredictor SHARED_FIREBALL;
    public static TrajectoriesModule INSTANCE;
    public final ProjectileSnapshot settings;
    public Map<Integer, TrajectoryRenderer> renderers;
    public final ThrowableProjectilePredictor throwablePredictor;
    public final ThrowableProjectilePredictor potionPredictor;

    public TrajectoriesModule() {
        super("Trajectories", Category.RENDER);
        INSTANCE = this;
        settings = new ProjectileSnapshot(this);
        renderers = new HashMap<>();
        throwablePredictor = new ThrowableProjectilePredictor(new ProjectileSimulation());
        potionPredictor = new ThrowableProjectilePredictor(new PotionSimulation());
    }

    static {
        PREDICTORS = new HashMap<>();
        SHARED_THROWABLE = new ThrowableProjectilePredictor(new ProjectileSimulation());
        SHARED_POTION = new ThrowableProjectilePredictor(new PotionSimulation());
        SHARED_ARROW = new HeldProjectilePredictor();
        SHARED_FIREBALL = new FireballPredictor();
        PREDICTORS.put(Items.snowball, new TrajectoryTarget(SHARED_THROWABLE, ProjectileType.SNOWBALL));
        PREDICTORS.put(Items.egg, new TrajectoryTarget(SHARED_THROWABLE, ProjectileType.EGG));
        PREDICTORS.put(Items.ender_pearl, new TrajectoryTarget(SHARED_THROWABLE, ProjectileType.ENDER_PEARL));
        PREDICTORS.put(Items.potionitem, new TrajectoryTarget(SHARED_POTION, ProjectileType.SPLASH_POTION));
        PREDICTORS.put(Items.bow, new TrajectoryTarget(SHARED_ARROW, ProjectileType.ARROW));
        PREDICTORS.put(Items.fire_charge, new TrajectoryTarget(SHARED_FIREBALL, ProjectileType.FIREBALL));
    }

    public void updateAll() {
        if (renderers == null || ServerTracker.INSTANCE.entityTracker == null) return;
        Map<Integer, TrajectoryRenderer> next = new HashMap<>();
        for (TrackedFakePlayer tracked : ServerTracker.INSTANCE.entityTracker.getEntities(162, TrackedFakePlayer.class)) {
            EntityPlayer entity = tracked.getEntity();
            TrajectoryOwnerSettings owner = settingsFor(tracked);
            if (!owner.enabled.isEnabled() || owner.enabled.isHidden()) continue;
            TrajectoryTarget target = typeOf(entity, owner);
            if (target == null || !heldEnabled(owner, target.type)) continue;
            TrajectoryRenderer renderer = renderers.get(entity.getEntityId());
            if (renderer == null || renderer.predictor != target.predictor || renderer.type != target.type)
                renderer = new TrajectoryRenderer(entity, target.predictor, target.type, owner, false);
            else renderer.update();
            next.put(entity.getEntityId(), renderer);
        }
        for (TrackedFireball tracked : ServerTracker.INSTANCE.entityTracker.getEntities(256, TrackedFireball.class))
            updateEntity(tracked, next);
        for (TrackedOtherEntity tracked : ServerTracker.INSTANCE.entityTracker.getEntities(256, TrackedOtherEntity.class))
            shouldRender(tracked, next);
        for (TrackedThrowable tracked : ServerTracker.INSTANCE.entityTracker.getEntities(256, TrackedThrowable.class)) {
            EntityThrowable entity = tracked.getEntity();
            if (entity.isDead) continue;
            TrajectoryOwnerSettings owner = settingsFor(tracked.shooter);
            ProjectileType type = entity instanceof EntityEnderPearl ? ProjectileType.ENDER_PEARL
                    : entity instanceof EntitySnowball ? ProjectileType.SNOWBALL
                    : entity instanceof EntityEgg ? ProjectileType.EGG
                    : entity instanceof EntityPotion && ItemPotion.isSplash(((EntityPotion) entity).getPotionDamage())
                    ? ProjectileType.SPLASH_POTION : null;
            if (type == null) continue;
            ThrowableProjectilePredictor predictor = type == ProjectileType.SPLASH_POTION ? SHARED_POTION : SHARED_THROWABLE;
            TrajectoryRenderer renderer = rendererFor(entity, predictor, type, owner);
            if (renderer != null) next.put(entity.getEntityId(), renderer);
        }
        renderers = next;
    }

    @EventHandler public void renderAll(com.adjustclient.event.Render3DEvent event) {
        if (!isEnabled() || renderers == null || renderers.isEmpty()) return;
        for (TrajectoryRenderer renderer : renderers.values())
            renderEntity(renderer, mc.getRenderManager(), event.partialTicks);
    }

    public TrajectoryOwnerSettings settingsFor(TrackedEntity<?> tracked) {
        EntityLivingBase entity = (EntityLivingBase) tracked.getEntity();
        if (entity == mc.thePlayer) return settings.local;
        entity.isInvisible();
        return settingsFor(entity);
    }

    public TrajectoryOwnerSettings settingsFor(Entity entity) {
        if (entity == null) return settings.players;
        if (entity == mc.thePlayer) return settings.local;
        if (WorldUtil.isTeammate(entity)
                || Adjust.INSTANCE.tagManager.getTag(entity.getName()) == com.adjustclient.tag.PlayerTag.WHITELIST)
            return settings.friendly;
        return settings.players;
    }

    public void shouldRender(TrackedOtherEntity tracked, Map<Integer, TrajectoryRenderer> next) {
        EntityArrow arrow = (EntityArrow) tracked.getEntity();
        if (arrow.isDead) return;
        net.minecraft.nbt.NBTTagCompound state = new net.minecraft.nbt.NBTTagCompound();
        arrow.writeEntityToNBT(state);
        if (state.getByte("inGround") != 0) return;
        Entity shooter = arrow.shootingEntity instanceof EntityLivingBase ? arrow.shootingEntity : null;
        TrajectoryRenderer renderer = rendererFor(arrow, SHARED_ARROW, ProjectileType.ARROW, settingsFor(shooter));
        if (renderer != null) next.put(arrow.getEntityId(), renderer);
    }

    public void updateEntity(TrackedFireball tracked, Map<Integer, TrajectoryRenderer> next) {
        TrajectoryOwnerSettings owner = settingsFor(tracked.shooter);
        if (!owner.enabled.isEnabled() || owner.enabled.isHidden()
                || !owner.fireballsEnabled.isEnabled() || owner.fireballsEnabled.isHidden()) return;
        EntityFireball entity = tracked.getEntity();
        if (entity.isDead) return;
        TrajectoryRenderer renderer = renderers.get(entity.getEntityId());
        if (renderer == null || renderer.predictor != SHARED_FIREBALL || renderer.entity != entity
                || renderer.type != ProjectileType.FIREBALL)
            renderer = new TrajectoryRenderer(entity, SHARED_FIREBALL, ProjectileType.FIREBALL, owner, true);
        next.put(entity.getEntityId(), renderer);
    }

    public TrajectoryTarget typeOf(EntityPlayer entity, TrajectoryOwnerSettings owner) {
        ItemStack stack;
        if (entity == mc.thePlayer) stack = Adjust.slotSpoofer.getRenderItemStack();
        else {
            int slot = entity.inventory.currentItem;
            if (slot >= 9) return null;
            stack = slot < 0 ? null : entity.inventory.mainInventory[slot];
        }
        if (stack == null) return null;
        TrajectoryTarget target = PREDICTORS.get(stack.getItem());
        if (target == null) return null;
        if (target.type == ProjectileType.SNOWBALL) {
            var server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL
                    && Adjust.gameStateTracker.state.game == com.adjustclient.hypixel.HypixelGame.BED_WARS) return null;
        }
        if (stack.getItem() instanceof ItemBow && !entity.isUsingItem()) return null;
        if (stack.getItem() instanceof ItemPotion && !ItemPotion.isSplash(stack.getMetadata())) return null;
        if (stack.getItem() == Items.fire_charge
                && (!owner.fireballsEnabled.isEnabled() || owner.fireballsEnabled.isHidden())) return null;
        return target;
    }

    public void renderEntity(TrajectoryRenderer renderer, net.minecraft.client.renderer.entity.RenderManager manager, float partialTicks) {
        Entity entity = renderer.entity;
        if (entity.isDead) return;
        if (entity instanceof EntityArrow || entity instanceof EntityThrowable || entity instanceof EntityFireball) {
            net.minecraft.nbt.NBTTagCompound state = new net.minecraft.nbt.NBTTagCompound();
            if (entity instanceof EntityArrow arrow) arrow.writeEntityToNBT(state);
            else if (entity instanceof EntityThrowable throwable) throwable.writeEntityToNBT(state);
            else ((EntityFireball) entity).writeEntityToNBT(state);
            if (state.getByte("inGround") != 0) return;
        }
        if (renderer.projectile) renderer.alpha();
        if (entity instanceof EntityPlayer player) {
            float yaw = entity.prevRotationYaw + MathHelper.wrapAngleTo180_float(entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            if (renderer.predictor == SHARED_ARROW && player.isUsingItem() && player.getItemInUseDuration() < 21
                    || entity.posX != entity.lastTickPosX || entity.posY != entity.lastTickPosY
                    || entity.posZ != entity.lastTickPosZ || renderer.hasMoved() || renderer.rotation == null
                    || Float.floatToIntBits(renderer.rotation.x) != Float.floatToIntBits(yaw)
                    || Float.floatToIntBits(renderer.rotation.y) != Float.floatToIntBits(pitch))
                renderer.update(new Vec2f(yaw, pitch), partialTicks);
        }
        TrajectoryPath path = renderer.path;
        boolean translated = path.points != null && path.points.size() >= 2;
        TrajectoryOwnerSettings owner = renderer.settings;
        Integer override = settings.projectileColors.enabled.isEnabled() && !settings.projectileColors.enabled.isHidden()
                ? settings.projectileColors.getColor(renderer.type) : null;
        int alpha = 255;
        if (entity instanceof EntityPlayer && owner instanceof PlayerTrajectorySettings players
                && players.fovAlpha.isEnabled() && !players.fovAlpha.isHidden()) {
            Vec2f towards = RotationUtil.getYawBetween(mc.thePlayer.getPositionEyes(1.0F), entity.getPositionEyes(1.0F));
            float difference = Math.abs(MathHelper.wrapAngleTo180_float(towards.x - renderer.rotation.x))
                    + Math.abs(towards.y - renderer.rotation.y);
            alpha = (int) (Math.max(0.0, Math.min(1.0, 1.0 - difference / 75.0F)) * 255.0);
        }
        boolean fresh = renderer.animation == null;
        if (fresh) renderer.animation = new com.adjustclient.render.animation.Animation(
                com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 180L);
        boolean entityHit = path.hit != null && path.hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
        if (fresh && entityHit) renderer.animation.value = 1.0F;
        else renderer.animation.update(entityHit ? 1.0F : 0.0F);
        if (translated) {
            RenderUtil.i();
            RenderUtil.setupOverlayRendering(partialTicks);
            GL11.glTranslated(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
            Color blended = ColorUtil.blend(owner.hitColor.getColor(), owner.color.getColor(), renderer.animation.value);
            int color = override == null ? blended.getRGB() : override;
            if (alpha >= 25) {
                RenderUtil.glColor(alpha << 24 | color & 0xffffff);
                GL11.glLineWidth(2.0F);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                if (!renderer.projectile && renderer.type == ProjectileType.FIREBALL && entity == mc.thePlayer) {
                    float yaw = renderer.rotation.x / 180.0F * (float) Math.PI;
                    Vec3 first = path.points.get(0).addVector(-MathHelper.cos(yaw) * .16F, -.1000000014901161,
                            -MathHelper.sin(yaw) * .16F);
                    Vec3 last = path.points.get(path.points.size() - 1);
                    GL11.glVertex3d(first.xCoord, first.yCoord, first.zCoord);
                    GL11.glVertex3d(last.xCoord, last.yCoord, last.zCoord);
                } else for (int i = 0; i < path.points.size(); i++) {
                    Vec3 vertex = renderer.projectile && i == 0 ? renderer.interpolate(partialTicks) : path.points.get(i);
                    GL11.glVertex3d(vertex.xCoord, vertex.yCoord, vertex.zCoord);
                }
                GL11.glEnd();
            }
        }
        if (path.hit != null) renderer.predictor.render(path, translated, owner, override, alpha, partialTicks);
        if (translated) RenderUtil.h();
    }

    public Vec3[] getHitBoxVertices(AxisAlignedBB box, Vec3 hit, EnumFacing side) {
        EnumFacing.Axis axis;
        if (side != null) axis = side.getAxis();
        else {
            double x = Math.min(Math.abs(hit.xCoord - box.minX), Math.abs(hit.xCoord - box.maxX));
            double y = Math.min(Math.abs(hit.yCoord - box.minY), Math.abs(hit.yCoord - box.maxY));
            double z = Math.min(Math.abs(hit.zCoord - box.minZ), Math.abs(hit.zCoord - box.maxZ));
            axis = y < x ? (z < y ? EnumFacing.Axis.Z : EnumFacing.Axis.Y)
                    : z < x ? (z < y ? EnumFacing.Axis.Z : EnumFacing.Axis.Y) : EnumFacing.Axis.X;
        }
        double x = hit.xCoord, y = hit.yCoord, z = hit.zCoord;
        if (axis == EnumFacing.Axis.X) return new Vec3[] {
                new Vec3(x, y - .25, z - .25), new Vec3(x, y + .25, z + .25),
                new Vec3(x, y - .25, z + .25), new Vec3(x, y + .25, z - .25)};
        if (axis == EnumFacing.Axis.Y) return new Vec3[] {
                new Vec3(x - .25, y, z - .25), new Vec3(x + .25, y, z + .25),
                new Vec3(x - .25, y, z + .25), new Vec3(x + .25, y, z - .25)};
        return new Vec3[] {new Vec3(x - .25, y - .25, z), new Vec3(x + .25, y + .25, z),
                new Vec3(x - .25, y + .25, z), new Vec3(x + .25, y - .25, z)};
    }

    public TrajectoryRenderer rendererFor(Entity entity, ProjectilePredictor predictor, ProjectileType type, TrajectoryOwnerSettings owner) {
        if (!owner.enabled.isEnabled() || owner.enabled.isHidden()) return null;
        if (type == ProjectileType.SNOWBALL) {
            var server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL
                    && Adjust.gameStateTracker.state.game == com.adjustclient.hypixel.HypixelGame.BED_WARS) return null;
        }
        TrajectoryRenderer renderer = renderers.get(entity.getEntityId());
        if (renderer == null || renderer.predictor != predictor || renderer.entity != entity || renderer.type != type)
            renderer = new TrajectoryRenderer(entity, predictor, type, owner, true);
        return renderer;
    }

    public void drawEntityHit(Entity entity, Vec3 hit, TrajectoryOwnerSettings owner, Integer colorOverride, Integer alpha, float partialTicks) {
        if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) return;
        int argb = colorOverride == null ? owner.hitColor.getColor().getRGB() : colorOverride;
        if (settings.entityHitBox.enabled.isEnabled() && !settings.entityHitBox.enabled.isHidden()) {
            AxisAlignedBB bounds = entity.getEntityBoundingBox();
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
            double expand = settings.entityHitBox.getExpand();
            double halfX = (bounds.maxX - bounds.minX) * .5;
            double halfZ = (bounds.maxZ - bounds.minZ) * .5;
            double height = bounds.maxY - bounds.minY;
            AxisAlignedBB box = new AxisAlignedBB(x - halfX - expand, y, z - halfZ - expand,
                    x + halfX + expand, y + height + (entity.isSneaking() ? -.25 : 0.0) + expand, z + halfZ + expand);
            RenderUtil.glColor(argb);
            RenderUtil.drawFilledBox(box);
            if (settings.entityHitBox.outline.isEnabled() && !settings.entityHitBox.outline.isHidden()) {
                GL11.glLineWidth(1.0F);
                RenderUtil.glColor(Math.min(255, (argb >>> 24) + 30) << 24 | argb & 0xffffff);
                RenderUtil.drawOutlinedBox(box);
            }
        } else if (hit != null) {
            if (alpha != null) {
                if (alpha < 25) return;
                argb = alpha << 24 | argb & 0xffffff;
            }
            GL11.glBegin(GL11.GL_LINES);
            RenderUtil.glColor(argb);
            for (Vec3 vertex : getHitBoxVertices(entity.getEntityBoundingBox(), hit, null))
                GL11.glVertex3d(vertex.xCoord, vertex.yCoord, vertex.zCoord);
            GL11.glEnd();
        }
    }

    @Override public void onEnable() { renderers.clear(); }
    @Override public void onDisable() { renderers.clear(); }

    private ProjectilePredictor predictor(ProjectileType type) {
        switch (type) {
            case FIREBALL: return new FireballPredictor();
            case ARROW: return new HeldProjectilePredictor();
            case SPLASH_POTION: return potionPredictor;
            default: return throwablePredictor;
        }
    }
    private boolean heldEnabled(TrajectoryOwnerSettings owner, ProjectileType type) {
        if (!(owner instanceof PlayerTrajectorySettings)) return true;
        PlayerTrajectorySettings player = (PlayerTrajectorySettings) owner;
        BooleanSetting enabled = player.heldProjectiles.get(type);
        return enabled.isEnabled() && !enabled.isHidden();
    }
}
