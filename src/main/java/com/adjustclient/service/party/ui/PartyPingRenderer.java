package com.adjustclient.service.party.ui;

import com.adjustclient.event.Render3DEvent;
import com.adjustclient.user.ClientUser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.joml.Vector4d;

public final class PartyPingRenderer {
    public List<PartyPing> pings = new ArrayList<>();
    public PartyPing preview;

    public static final Consumer<PartyPing> updatePing = ping -> ping.update();
    public static final Predicate<PartyPing> expired = ping -> System.currentTimeMillis() - ping.lifetime.lastMs >= 5000L;

    public void renderWorld(Render3DEvent event) {
        if (pings.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        Entity player = mc.thePlayer;
        float partialTicks = event.partialTicks;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        for (PartyPing ping : pings) {
            float opacity;
            if (ping.removed) opacity = 0.5F;
            else {
                long age = System.currentTimeMillis() - ping.lifetime.lastMs;
                if (age >= 4000L) opacity = 5000L - age >= 30L ? (5000L - age) / 1000.0F : 0.0F;
                else opacity = ping.visible || age >= 140L ? 1.0F : age / 140.0F;
            }
            ping.drawWorld(mc.getRenderManager(), x, z, opacity, partialTicks);
        }
    }

    public void setPreview(PartyPing ping) {
        if (preview != null) pings.remove(preview);
        preview = ping;
        if (ping != null) pings.add(ping);
    }

    public void renderLabels(Vector4d[] positions, float partialTicks) {
        for (int i = 0; i < pings.size(); i++) {
            Vector4d position = positions[i];
            if (position == null) continue;
            PartyPing ping = pings.get(i);
            if (ping.removed) { ping.drawLabel(100, (float)position.x, (float)position.y); continue; }
            long age = System.currentTimeMillis() - ping.lifetime.lastMs;
            int alpha;
            if (age >= 4000L) {
                if (5000L - age < 30L) continue;
                alpha = (int)((5000L - age) / 1000.0F * 200.0F);
            } else alpha = ping.visible || age >= 140L ? 200 : (int)(age / 140.0F * 200.0F);
            if (alpha >= 25) ping.drawLabel(alpha, (float)position.x, (float)position.y);
        }
    }

    public void previewBlock(double x, double y, double z) {
        BlockPing ping = new BlockPing(new Vec3(x, y, z), null);
        ping.removed = true;
        ping.visible = true;
        setPreview(ping);
    }

    public boolean projectPings(Vector4d[] positions, ScaledResolution resolution, float partialTicks) {
        boolean projected = false;
        for (int i = 0; i < pings.size(); i++) {
            PartyPing ping = pings.get(i);
            if (!ping.removed) {
                positions[i] = ping.project(resolution, partialTicks);
                if (positions[i] != null) projected = true;
            }
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.entityRenderer.worldProjectionActive) {
            mc.entityRenderer.worldProjectionActive = false;
            org.lwjgl.opengl.GL11.glClear(256);
            org.lwjgl.opengl.GL11.glMatrixMode(5889);
            org.lwjgl.opengl.GL11.glLoadIdentity();
            org.lwjgl.opengl.GL11.glOrtho(0.0, resolution.getScaledWidth_double(),
                    resolution.getScaledHeight_double(), 0.0, 1000.0, 3000.0);
            org.lwjgl.opengl.GL11.glMatrixMode(5888);
            org.lwjgl.opengl.GL11.glLoadIdentity();
            org.lwjgl.opengl.GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        }
        return projected;
    }

    public void receiveBlock(ClientUser user, double x, double z, double y) {
        for (PartyPing existing : pings) {
            BlockPing ping = (BlockPing)existing;
            if (ping.user != null && ping.user == user && ping.position.xCoord == x && ping.position.zCoord == z) {
                ping.position = new Vec3(x, y, z);
                ping.visible = true;
                ping.lifetime.reset();
                return;
            }
        }
        pings.add(new BlockPing(new Vec3(x, y, z), user));
    }

    public void receiveEntity(ClientUser user, int entityId, double x, double y, double z) {
        for (PartyPing existing : pings) {
            if (existing.user != null && existing.user == user && existing instanceof EntityPing
                    && ((EntityPing)existing).entityId == entityId) {
                existing.visible = true;
                existing.lifetime.reset();
                return;
            }
        }
        pings.add(new EntityPing(entityId, new Vec3(x, y, z), user));
    }

}
