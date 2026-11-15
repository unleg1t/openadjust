package com.adjustclient.module.impl.render.trajectories;
import java.util.List;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
public class TrajectoryPath {
    public final List<Vec3> points;
    public final MovingObjectPosition hit;
    public TrajectoryPath(List<Vec3> points, MovingObjectPosition hit) { this.points = points; this.hit = hit; }
}
