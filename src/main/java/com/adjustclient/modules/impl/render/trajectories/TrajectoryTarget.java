package com.adjustclient.modules.impl.render.trajectories;
import com.adjustclient.modules.impl.render.trajectories.predictor.ProjectilePredictor;
public final class TrajectoryTarget {
    public final ProjectilePredictor predictor;
    public final ProjectileType type;
    public TrajectoryTarget(ProjectilePredictor predictor, ProjectileType type) { this.predictor = predictor; this.type = type; }
}
