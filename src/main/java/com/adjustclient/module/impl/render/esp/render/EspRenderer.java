package com.adjustclient.module.impl.render.esp.render;

import com.adjustclient.util.MinecraftInstance;
import net.minecraft.client.gui.ScaledResolution;
import org.joml.Vector4d;

public abstract class EspRenderer implements MinecraftInstance {
    public boolean initialized;
    public abstract void update();
    public abstract Vector4d project(ScaledResolution resolution, float partialTicks);
}
