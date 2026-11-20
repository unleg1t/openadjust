package com.adjustclient.modules.impl.render.freecam.tool;

import java.util.ArrayList;
import net.minecraft.util.Vec3;

public final class PendingAction {
    public final FreecamTool tool;
    public final Vec3 destination;
    public final Vec3 start;
    public ArrayList<Vec3> path;
    public volatile boolean complete;
    public Object field24;

    public PendingAction(FreecamTool tool, Vec3 destination, Vec3 start) {
        this.tool = tool;
        this.destination = destination;
        this.start = start;
    }
}
