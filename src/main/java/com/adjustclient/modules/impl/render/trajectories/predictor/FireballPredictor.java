package com.adjustclient.modules.impl.render.trajectories.predictor;

import com.adjustclient.modules.impl.render.trajectories.TrajectoriesModule;
import com.adjustclient.modules.impl.render.trajectories.TrajectoryPath;
import com.adjustclient.modules.impl.render.trajectories.settings.TrajectoryOwnerSettings;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.SimulationWorld;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.utilities.simulation.FireballSimulation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public final class FireballPredictor implements ProjectilePredictor, MinecraftInstance {
    public final FireballSimulation simulation = new FireballSimulation();

    public FireballPath predict(EntityFireball fireball) {
        simulation.initializeFromFireball(fireball);
        simulation.partialTicks = 1.0F;
        List<Vec3> points = new ArrayList<>();
        points.add(new Vec3(simulation.posX, simulation.posY, simulation.posZ));
        for (int i = 0; i < 200; ++i) {
            if (simulation.onUpdate()) {
                MovingObjectPosition hit = simulation.collision;
                points.add(hit.hitVec);
                SimulationWorld world = new SimulationWorld();
                world.world = fireball.worldObj;
                world.exploder = fireball;
                world.strength = 1.0F;
                world.posX = simulation.posX;
                world.posY = simulation.posY;
                world.posZ = simulation.posZ;
                var blocks = world.getAffectedBlocks();
                var entities = world.getAffectedEntities();
                return new FireballPath(points, simulation.collision, blocks, entities);
            }
            points.add(new Vec3(simulation.posX, simulation.posY, simulation.posZ));
        }
        return new FireballPath(points, null, null, null);
    }

    @Override public void render(TrajectoryPath rawPath, boolean translated, TrajectoryOwnerSettings settings,
                                 Integer override, int alpha, float partialTicks) {
        FireballPath path = (FireballPath) rawPath;
        MovingObjectPosition hit = path.hit;
        if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (alpha >= 25) {
                GL11.glBegin(GL11.GL_LINES);
                RenderUtil.glColor(alpha << 24 | (override == null ? settings.hitColor.getColor().getRGB() : override) & 0xffffff);
                for (Vec3 point : ThrowableProjectilePredictor.getImpactVertices(hit))
                    GL11.glVertex3d(point.xCoord, point.yCoord, point.zCoord);
                GL11.glEnd();
            }
            if (!settings.explosionEnabled.isEnabled()) return;
        }
        List<Entity> entities = path.affectedEntities;
        if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            if (entities == null) entities = new ArrayList<>(List.of(hit.entityHit));
            else if (!entities.contains(hit.entityHit)) entities.add(hit.entityHit);
        }
        if (!settings.explosionEnabled.isEnabled())
            entities = hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY ? List.of(hit.entityHit) : null;
        var manager = mc.getRenderManager();
        if (entities != null && !entities.isEmpty()) {
            if (translated) { GL11.glPopMatrix(); GL11.glPushMatrix(); }
            else GL11.glPushMatrix();
            GL11.glTranslated(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
            boolean boxes = TrajectoriesModule.INSTANCE.settings.entityHitBox.enabled.isEnabled();
            if (boxes) RenderUtil.begin3d(); else RenderUtil.i();
            for (Entity entity : entities) {
                Vec3 hitPosition = hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit == entity ? hit.hitVec : null;
                TrajectoriesModule.INSTANCE.drawEntityHit(entity, hitPosition, settings, override,
                        settings.explosionEnabled.isEnabled() ? null : alpha, partialTicks);
            }
            if (boxes) RenderUtil.end3d(); else RenderUtil.h();
            if (!translated) GL11.glPopMatrix();
        }
        if (!settings.explosionEnabled.isEnabled() || path.affectedBlocks == null || path.affectedBlocks.isEmpty()) return;
        if (translated) { GL11.glPopMatrix(); GL11.glPushMatrix(); }
        int fill = override == null ? settings.getExplosionColor()
                : (int) ((override >>> 24) * 0.45F) << 24 | override & 0xffffff;
        int outline = override == null ? settings.getExplosionOutlineColor()
                : (int) ((override >>> 24) * 0.45F) << 24 | override & 0xffffff;
        RenderUtil.begin3d();
        for (var entry : path.affectedBlocks.entrySet()) {
            AxisAlignedBB box = entry.getValue().getBlock().getSelectedBoundingBox(mc.theWorld, entry.getKey())
                    .offset(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
            RenderUtil.glColor(fill);
            RenderUtil.drawFilledBox(box);
            if (settings.explosionOutline.isEnabled()) {
                GL11.glLineWidth(1.0F);
                RenderUtil.glColor(outline);
                RenderUtil.drawOutlinedBox(box);
            }
        }
        RenderUtil.end3d();
    }

    @Override public FireballPath predict(EntityPlayer owner, Vec3 position, Vec2f rotation, float partialTicks) {
        return predictHeld(owner, position, rotation, partialTicks);
    }

    public FireballPath predictHeld(EntityPlayer owner, Vec3 position, Vec2f rotation, float partialTicks) {
        simulation.initialize(owner, position.xCoord, position.yCoord, position.zCoord, rotation.x, rotation.y);
        simulation.partialTicks = partialTicks;
        List<Vec3> points = new ArrayList<>();
        points.add(new Vec3(simulation.posX, simulation.posY, simulation.posZ));
        for (int i = 0; i < 200; ++i) {
            if (simulation.onUpdate()) {
                MovingObjectPosition hit = simulation.collision;
                points.add(hit.hitVec);
                SimulationWorld world = new SimulationWorld();
                world.world = owner.worldObj;
                world.exploder = owner;
                world.strength = 1.0F;
                world.posX = simulation.posX;
                world.posY = simulation.posY;
                world.posZ = simulation.posZ;
                var blocks = world.getAffectedBlocks();
                var entities = world.getAffectedEntities();
                return new FireballPath(points, simulation.collision, blocks, entities);
            }
            points.add(new Vec3(simulation.posX, simulation.posY, simulation.posZ));
        }
        return new FireballPath(points, null, null, null);
    }
}
