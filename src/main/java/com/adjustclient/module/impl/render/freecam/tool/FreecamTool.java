package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.util.MinecraftInstance;
import java.util.List;
import net.minecraft.network.Packet;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public abstract class FreecamTool implements MinecraftInstance {
    public FreecamController controller;
    public MovingObjectPosition hit;
    public boolean field12;

    protected FreecamTool(FreecamController controller, MovingObjectPosition hit) {
        this.controller = controller;
        this.hit = hit;
    }

    public Object getResult() { return null; }

    public abstract void execute(List<Packet<?>> packets, Vec3 position);
}
