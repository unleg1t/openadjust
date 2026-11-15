package com.adjustclient.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;

public class Render2DEvent extends Event {
    public List<Runnable> postTasks = new ArrayList<>();
    public ScaledResolution scaledResolution;
    public float partialTicks;

    public Render2DEvent(ScaledResolution resolution, float partialTicks) {
        this.scaledResolution = resolution;
        this.partialTicks = partialTicks;
    }

    public void runPostTasks() {
        while (!postTasks.isEmpty()) postTasks.remove(0).run();
    }
}
