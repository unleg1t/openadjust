package com.adjustclient.modules.impl.render.freecam.tool;

import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class EmptyTool extends FreecamTool {
    public Object field16;

    public EmptyTool(FreecamController controller, MovingObjectPosition hit) { super(controller, hit); }

    @Override public void execute(List<Packet<?>> packets, Vec3 position) { }
}
