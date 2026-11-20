package com.adjustclient.modules.impl.render;

import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.LowLevelPacketReceiveEvent;
import com.adjustclient.core.events.Render3DEvent;
import com.adjustclient.core.events.PlayerUpdateTailEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.player.BlinkModule;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.BoundedList;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

public final class TrailsModule extends Module {
    public static TrailsModule INSTANCE;
    public final BooleanSetting blinkOnly;
    private BoundedList<Vec3> trail;
    public final NumberSetting moves;
    public final NumberSetting width;
    public final ColorSetting color;

    public TrailsModule() {
        super("Trails", Category.RENDER);
        INSTANCE = this;
        blinkOnly = new BooleanSetting(this, "Blink only", false);
        blinkOnly.setDescription("Only renders when the Blink module is turned on");
        moves = new NumberSetting(this, "Moves", 10, 3, 60, 1);
        moves.addListener((Runnable) () -> {
            if (isEnabled()) trail = new BoundedList<>(moves.getInt());
        });
        width = new NumberSetting(this, "Width", 2.0, 1.0, 5.0, 0.5);
        color = new ColorSetting(this, "Color", new Color(0xdc9090ff, true));
    }
    @EventHandler public void onTick(PlayerUpdateTailEvent event) {
        if (!isEnabled()) return;
        if (blinkOnly.isEnabled() && !BlinkModule.INSTANCE.isEnabled()) {
            trail.clear();
            return;
        }
        trail.add(new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + 0.00625,
                mc.thePlayer.lastTickPosZ));
    }
    @EventHandler public void onRender(Render3DEvent event) {
        if (!isEnabled()) return;
        List<Vec3> points = new ArrayList<>(trail);
        if (points.isEmpty()) return;
        Vec3 last = points.get(points.size() - 1);
        if (last == null) return;
        float partialTicks = event.partialTicks;
        points.add(new Vec3(mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks,
                last.yCoord + (mc.thePlayer.posY + 0.00625 - last.yCoord) * partialTicks,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks));
        GlStateManager.disableDepth();
        RenderUtil.drawTrail(points, mc.getRenderManager(), color.getColor(), width.getFloat());
        GlStateManager.enableDepth();
    }
    @Override public void onEnable() { trail = new BoundedList<>(moves.getInt()); }
    @Override public void onDisable() { trail = null; }
    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        if (!isEnabled()) return;
        if (event.packet instanceof S08PacketPlayerPosLook position) {
            if (mc.thePlayer == null || mc.thePlayer.getDistanceSq(position.getX(), position.getY(), position.getZ()) > 9.0)
                trail.clear();
        } else if (event.packet instanceof S01PacketJoinGame || event.packet instanceof S07PacketRespawn) {
            trail.clear();
        }
    }
}
