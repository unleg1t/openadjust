package com.adjustclient.modules.impl.render.skeleton;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.render.ThirdPersonModule;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import com.adjustclient.core.commands.impl.debug.DebugEpsteinSubCommand;

public final class SkeletonModule extends Module {
    public static SkeletonModule INSTANCE;
    public Map<AbstractClientPlayer, SkeletonPose> poses;
    public SkeletonTargetSettings players;
    public SkeletonTargetSettings friendly;
    public SkeletonTargetSettings enemy;

    public SkeletonModule() {
        super("Skeleton", Category.RENDER);
        INSTANCE = this;
        players = new SkeletonTargetSettings(this, "Players");
        friendly = new SkeletonTargetSettings(this, "Friendly");
        enemy = new SkeletonTargetSettings(this, "Enemy");
    }

    public static void beginSkeletons() {
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableDepth();
        mc.entityRenderer.disableLightmap();
    }

    @EventHandler
    public void onUpdate(com.adjustclient.core.events.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled() || poses == null) return;
        updatePoses();
    }

    public void updatePoses() {
        poses.clear();
        if (DebugEpsteinSubCommand.enabled) return;
        int mask = players.enabled.isEnabled() ? 2 : 0;
        if (friendly.enabled.isEnabled()) mask |= 0x20;
        if (mc.gameSettings.thirdPersonView != 0 || mc.cameraOverride != null
                || ThirdPersonModule.INSTANCE.freeLooking) mask |= 0x80;
        if (enemy.enabled.isEnabled()) mask |= 0x800;
        for (TrackedEntity<?> tracked : com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.entityTracker.getEntities(mask, null)) {
            SkeletonTargetSettings target = tracked.matchesType(2048) ? enemy : tracked.matchesType(224) ? friendly : players;
            if (!target.enabled.isEnabled()) continue;
            AbstractClientPlayer player = (AbstractClientPlayer) tracked.getEntity();
            if (player.isPlayerSleeping() || player.isDead || player.isInvisible() || player.ridingEntity != null) continue;
            poses.put(player, new SkeletonPose((TrackedLivingEntity) tracked, target));
        }
    }

    public void drawSkeleton(SkeletonPose pose, RenderManager renderManager, ModelBiped model, float partialTicks) {
        AbstractClientPlayer player = (AbstractClientPlayer) pose.player.getEntity();
        boolean sneaking = model.isSneak;
        GL11.glPushMatrix();
        GL11.glLineWidth(1.25f);
        RenderUtil.glColor(pose.settings.getColor());
        var position = com.adjustclient.utilities.game.entity.tracker.ServerPositionTracker.INSTANCE.get(player.getEntityId());
        double x = player.posX, y = player.posY, z = player.posZ;
        double oldX = player.prevPosX, oldY = player.prevPosY, oldZ = player.prevPosZ;
        float previousYaw = player.prevRenderYawOffset, yaw = player.renderYawOffset;
        if (position != null) {
            x = position.x; y = position.y; z = position.z;
            oldX = position.previousX; oldY = position.previousY; oldZ = position.previousZ;
            previousYaw = position.previousRenderYawOffset; yaw = position.renderYawOffset;
        }
        if (player.ridingEntity instanceof EntityLivingBase) {
            EntityLivingBase mount = (EntityLivingBase) player.ridingEntity;
            previousYaw = mount.prevRenderYawOffset; yaw = mount.renderYawOffset;
        }
        GL11.glTranslated((x - oldX) * partialTicks + oldX - renderManager.viewerPosX,
                (y - oldY) * partialTicks + oldY - renderManager.viewerPosY + 0.0625,
                (z - oldZ) * partialTicks + oldZ - renderManager.viewerPosZ);
        float bodyYaw = previousYaw + (yaw - previousYaw) * partialTicks;
        GL11.glRotatef(-bodyYaw, 0, 1, 0);
        if (player.deathTime > 0) {
            float death = (float) Math.sqrt(((player.deathTime + partialTicks - 1) / 20.0f) * 1.6f);
            if (death > 1) death = 1;
            GL11.glRotatef(death * 90, 0, 0, -1);
        }
        boolean crouched = player.isSneaking();
        if (crouched) GL11.glTranslated(0, 0, -0.235);
        double hips = crouched ? 0.6000000238418579 : 0.75;
        double shoulders = crouched ? 1.1500000238418579 : 1.3;
        double legLength = crouched ? -0.550000011920929 : -0.699999988079071;
        ModelRenderer[] legs = {model.bipedRightLeg, model.bipedLeftLeg};
        for (int side = 0; side < legs.length; side++) {
            ModelRenderer part = legs[side];
            GL11.glPushMatrix();
            GL11.glTranslated(side == 0 ? -0.125 : 0.125, hips, 0);
            if (part.rotateAngleX != 0) GL11.glRotatef(part.rotateAngleX * 48.701412f, 1, 0, 0);
            if (part.rotateAngleY != 0) GL11.glRotatef(part.rotateAngleY * 48.701412f, 0, 1, -1);
            if (part.rotateAngleZ != 0) GL11.glRotatef(part.rotateAngleZ * 48.701412f, 0, 0, 1);
            GL11.glBegin(3); GL11.glVertex3d(0, 0, 0); GL11.glVertex3d(0, legLength, 0); GL11.glEnd();
            GL11.glPopMatrix();
        }
        double armX = sneaking ? 0.325 : 0.35;
        if (crouched) GL11.glTranslated(0, 0, 0.25);
        GL11.glPushMatrix();
        if (crouched) GL11.glTranslated(0, -0.05, -0.01725);
        ModelRenderer[] arms = {model.bipedRightArm, model.bipedLeftArm};
        for (int side = 0; side < 2; side++) {
            ModelRenderer part = arms[side];
            GL11.glPushMatrix(); GL11.glTranslated(side == 0 ? -armX : armX, shoulders, 0);
            if (part.rotateAngleX != 0) GL11.glRotatef(part.rotateAngleX * 48.701412f, 1, 0, 0);
            if (part.rotateAngleY != 0) GL11.glRotatef(part.rotateAngleY * 48.701412f, 0, 1, -1);
            if (part.rotateAngleZ != 0) GL11.glRotatef(part.rotateAngleZ * 48.701412f, 0, 0, -1);
            GL11.glBegin(3); GL11.glVertex3d(0, 0, 0); GL11.glVertex3d(0, -0.5, 0); GL11.glEnd();
            GL11.glPopMatrix();
        }
        GL11.glRotatef(bodyYaw - (player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks), 0, 1, 0);
        GL11.glPushMatrix(); GL11.glTranslated(0, shoulders, 0);
        if (model.bipedHead.rotateAngleX != 0) GL11.glRotatef(model.bipedHead.rotateAngleX * 48.701412f, 1, 0, 0);
        GL11.glBegin(3); GL11.glVertex3d(0, 0, 0); GL11.glVertex3d(0, 0.3, 0); GL11.glEnd(); GL11.glPopMatrix();
        GL11.glPopMatrix();
        if (crouched) { GL11.glRotatef(25, 1, 0, 0); GL11.glTranslated(0, -0.16175, -0.48025); }
        GL11.glPushMatrix(); GL11.glTranslated(0, hips, 0);
        GL11.glBegin(3); GL11.glVertex3d(-0.125, 0, 0); GL11.glVertex3d(0.125, 0, 0); GL11.glEnd(); GL11.glPopMatrix();
        GL11.glPushMatrix(); GL11.glTranslated(0, hips, 0);
        GL11.glBegin(3); GL11.glVertex3d(0, 0, 0); GL11.glVertex3d(0, 0.55, 0); GL11.glEnd(); GL11.glPopMatrix();
        GL11.glPushMatrix(); GL11.glTranslated(0, shoulders, 0);
        GL11.glBegin(3); GL11.glVertex3d(-armX, 0, 0); GL11.glVertex3d(armX, 0, 0); GL11.glEnd(); GL11.glPopMatrix();
        GlStateManager.color(1, 1, 1, 1); GL11.glPopMatrix();
    }

    @Override public void onEnable() { poses = new HashMap<>(); }
    @Override public void onDisable() { poses = null; }

    public static void endSkeletons() {
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        mc.entityRenderer.enableLightmap();
    }
}
