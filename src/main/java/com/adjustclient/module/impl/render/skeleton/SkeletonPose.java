package com.adjustclient.module.impl.render.skeleton;

import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import net.minecraft.client.model.ModelBiped;

public final class SkeletonPose {
    public TrackedLivingEntity player;
    public SkeletonTargetSettings settings;
    public ModelBiped model;
    public float partialTicks;

    public SkeletonPose(TrackedLivingEntity player, SkeletonTargetSettings settings) {
        this.player = player;
        this.settings = settings;
    }
    public SkeletonPose(TrackedLivingEntity player, Object context, float partialTicks) {
        this.player = player;
        this.partialTicks = partialTicks;
        if (context instanceof SkeletonTargetSettings) this.settings = (SkeletonTargetSettings) context;
        if (context instanceof ModelBiped) this.model = (ModelBiped) context;
    }
}
