package com.adjustclient.managers.party.ui;

import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.Timer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import org.joml.Vector4d;

abstract class PartyPing {
    public ClientUser user;
    public Timer lifetime = new Timer();
    public boolean removed;
    public boolean visible;
    protected PartyPing(ClientUser user) { this.user = user; }
    public abstract Vector4d project(ScaledResolution resolution, float partialTicks);
    public abstract void drawLabel(int alpha, float x, float y);
    public abstract void drawWorld(RenderManager manager, double x, double z, float opacity, float partialTicks);
    public abstract void update();
}
