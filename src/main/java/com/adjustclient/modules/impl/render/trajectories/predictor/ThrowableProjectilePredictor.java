package com.adjustclient.modules.impl.render.trajectories.predictor;

import com.adjustclient.modules.impl.render.trajectories.TrajectoriesModule;
import com.adjustclient.modules.impl.render.trajectories.TrajectoryPath;
import com.adjustclient.modules.impl.render.trajectories.settings.TrajectoryOwnerSettings;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.utilities.simulation.ProjectileSimulation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class ThrowableProjectilePredictor implements ProjectilePredictor, MinecraftInstance {
    public final ProjectileSimulation simulation;
    public ThrowableProjectilePredictor() { this(new ProjectileSimulation()); }
    public ThrowableProjectilePredictor(ProjectileSimulation simulation) { this.simulation = simulation; }

    public static Vec3[] getImpactVertices(MovingObjectPosition hit) {
        return Arrays.stream(new Vec3[] {new Vec3(-0.25, 0.0, -0.25), new Vec3(0.25, 0.0, 0.25),
                new Vec3(-0.25, 0.0, 0.25), new Vec3(0.25, 0.0, -0.25)})
                .map(point -> transformImpactVertex(hit, point))
                .toArray(Vec3[]::new);
    }

    public static Vec3 transformImpactVertex(MovingObjectPosition hit, Vec3 point) {
        switch (hit.sideHit.getAxis()) {
            case X: point = point.rotatePitch(1.5707999F).rotateYaw(1.5707999F); break;
            case Y: break;
            case Z: point = point.rotatePitch(1.5707999F); break;
            default: throw new IllegalStateException();
        }
        return hit.hitVec.add(point);
    }

    protected ProjectileSimulation initialize(EntityPlayer player, Vec3 position, Vec2f rotation, float partialTicks) {
        simulation.setThrowableHeading(player.worldObj, player.getEyeHeight(), position.xCoord, position.yCoord,
                position.zCoord, rotation.x, rotation.y);
        simulation.source = player;
        return simulation;
    }

    @Override public void render(TrajectoryPath path, boolean translated, TrajectoryOwnerSettings settings,
                                 Integer override, int alpha, float partialTicks) {
        MovingObjectPosition hit = path.hit;
        switch (hit.typeOfHit) {
            case BLOCK:
                if (alpha < 25) return;
                if (!translated) RenderUtil.i();
                GL11.glBegin(GL11.GL_LINES);
                RenderUtil.glColor(alpha << 24 | (override == null ? settings.hitColor.getColor().getRGB() : override) & 0xffffff);
                for (Vec3 point : getImpactVertices(hit)) GL11.glVertex3d(point.xCoord, point.yCoord, point.zCoord);
                GL11.glEnd();
                if (!translated) RenderUtil.h();
                break;
            case ENTITY:
                if (translated) { GL11.glPopMatrix(); GL11.glPushMatrix(); }
                else {
                    var manager = mc.getRenderManager();
                    GL11.glTranslated(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
                }
                if (!TrajectoriesModule.INSTANCE.settings.entityHitBox.enabled.isEnabled()) {
                    RenderUtil.i();
                    TrajectoriesModule.INSTANCE.drawEntityHit(hit.entityHit, hit.hitVec, settings, override, alpha, partialTicks);
                    RenderUtil.h();
                } else {
                    RenderUtil.begin3d();
                    TrajectoriesModule.INSTANCE.drawEntityHit(hit.entityHit, hit.hitVec, settings, override, alpha, partialTicks);
                    RenderUtil.end3d();
                }
                break;
            default: break;
        }
    }

    @Override public TrajectoryPath predict(EntityPlayer player, Vec3 position, Vec2f rotation, float partialTicks) {
        initialize(player, position, rotation, partialTicks);
        simulation.partialTicks = partialTicks;
        return simulate();
    }

    public TrajectoryPath simulate() {
        List<Vec3> points = new ArrayList<>();
        points.add(new Vec3(simulation.posX, simulation.posY, simulation.posZ));
        for (int tick = 0; tick < 200; ++tick) {
            if (simulation.onUpdate()) {
                MovingObjectPosition hit = simulation.collision;
                points.add(hit.hitVec);
                return new TrajectoryPath(points, hit);
            }
            points.add(new Vec3(simulation.posX, simulation.posY, simulation.posZ));
        }
        return new TrajectoryPath(points, null);
    }

    public TrajectoryPath predict(EntityThrowable entity) {
        simulation.initialize(entity);
        simulation.partialTicks = 1.0F;
        return simulate();
    }
}
