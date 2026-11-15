package com.adjustclient.module.impl.render;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.LowLevelPacketReceiveEvent;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.render.ColorUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.awt.Color;
import java.util.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import org.lwjgl.opengl.GL11;
import com.adjustclient.ui.hud.watermark.WatermarkStandalone;

public final class DamageParticlesModule extends Module implements MinecraftInstance {
    public static DamageParticlesModule INSTANCE;
    public final ModeSetting<DamageParticlesDisplay> display;
    public final ExpandSetting customColor;
    public final BooleanSetting customColorEnabled;
    public final ColorSetting customColorColor;
    public final NumberSetting timeout;
    private final Int2ObjectOpenHashMap<UnusedRenderData> pending = new Int2ObjectOpenHashMap<>();
    private final List<DamageParticle> particles = new ArrayList<>();

    public DamageParticlesModule() {
        super("Damage Particles", Category.RENDER);
        INSTANCE = this;
        display = new ModeSetting<>(this, "Display", DamageParticlesDisplay.DAMAGE_ONLY, DamageParticlesDisplay.values());
        customColor = new ExpandSetting(this, "Custom color");
        customColorEnabled = new BooleanSetting(customColor, "Enabled", true);
        customColorColor = new ColorSetting(customColor, "Color", new Color(0xff435ad1, true), () -> !customColorEnabled.isEnabled());
        timeout = new NumberSetting(this, "Timeout", 3.5, 1.0, 6.0, 0.5);
    }

    @EventHandler public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        synchronized (particles) {
            particles.removeIf(particle -> isExpired(particle));
        }
        synchronized (pending) {
            pending.values().removeIf(update -> update.timer.hasElapsed(100L));
        }
    }

    @EventHandler public void onRender(Render3DEvent event) {
        if (!isEnabled()) return;
        synchronized (particles) {
            if (particles.isEmpty()) return;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            for (DamageParticle particle : particles) renderParticle(particle, mc.getRenderManager());
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
        }
    }

    private boolean isExpired(DamageParticle particle) {
        if (particle.timer.hasElapsed((int) (timeout.getValue().floatValue() * 1000.0F) + 470)) return true;
        Entity camera = mc.getRenderViewEntity();
        double x = camera.posX - particle.position.xCoord;
        double y = camera.posY - particle.position.yCoord;
        double z = camera.posZ - particle.position.zCoord;
        return x * x + y * y + z * z >= 576.0;
    }

    private void renderParticle(DamageParticle particle, RenderManager manager) {
        int alpha = particle.color >>> 24;
        long elapsed = System.currentTimeMillis() - particle.timer.lastMs;
        int duration = (int) (timeout.getValue().floatValue() * 1000.0F);
        if (elapsed >= duration) {
            long remaining = duration + 500L - elapsed;
            if (remaining < 30) return;
            alpha = (int) (remaining / 500.0F * alpha);
        }
        if (alpha < 25) return;
        GL11.glPushMatrix();
        GL11.glTranslated(particle.position.xCoord - manager.viewerPosX, particle.position.yCoord - manager.viewerPosY,
                particle.position.zCoord - manager.viewerPosZ);
        GL11.glRotatef(-manager.playerViewY, 0, 1, 0);
        GL11.glRotatef(manager.playerViewX, mc.gameSettings.thirdPersonView == 2 && !ThirdPersonModule.INSTANCE.freeLooking ? -1 : 1, 0, 0);
        GL11.glScalef(-0.03F, -0.03F, -0.03F);
        mc.fontRendererObj.drawString(particle.text, particle.textX, 0.0F, (alpha << 24) | (particle.color & 0xffffff), true);
        GL11.glPopMatrix();
    }

    @Override public void onDisable() {
        synchronized (particles) { particles.clear(); }
        synchronized (pending) { pending.clear(); }
    }

    @Override public String getDescription() { return "Shows 3D floating text displaying health lost or gained."; }

    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        Packet<?> packet = event.packet;
        if (packet instanceof S01PacketJoinGame) { clear(); return; }
        if (packet instanceof S19PacketEntityStatus) {
            S19PacketEntityStatus status = (S19PacketEntityStatus) packet;
            if (status.getOpCode() != 2 || display.getValue() == DamageParticlesDisplay.ALWAYS) return;
            Entity entity = status.getEntity(mc.theWorld);
            if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer || entity.getDistanceSqToEntity(mc.getRenderViewEntity()) > 576.0) return;
            synchronized (pending) { pending.put(entity.getEntityId(), new UnusedRenderData((EntityPlayer) entity)); }
            return;
        }
        if (!(packet instanceof S1CPacketEntityMetadata)) return;
        S1CPacketEntityMetadata metadata = (S1CPacketEntityMetadata) packet;
        int entityId = metadata.getEntityId();
        boolean always = display.getValue() == DamageParticlesDisplay.ALWAYS;
        boolean hasPending;
        synchronized (pending) { hasPending = pending.containsKey(entityId); }
        if (!hasPending && !always) return;
        List<net.minecraft.entity.DataWatcher.WatchableObject> watchables = metadata.func_149376_c();
        if (watchables == null) return;
        for (net.minecraft.entity.DataWatcher.WatchableObject value : watchables) {
            if (value.getObjectType() != 3 || value.getDataValueId() != 6) continue;
            EntityPlayer player;
            if (always) {
                Entity entity = mc.theWorld.getEntityByID(entityId);
                if (!(entity instanceof EntityPlayer)) return;
                player = (EntityPlayer) entity;
            } else {
                synchronized (pending) { player = pending.get(entityId).entity; }
            }
            float oldHealth = player.getHealth();
            addParticle(player, always, oldHealth, (Float) value.getObject());
            return;
        }
    }

    private void addParticle(EntityLivingBase entity, boolean signed, float oldHealth, float newHealth) {
        String text;
        float fraction;
        if (signed) {
            float delta = RandomUtil.round(newHealth - oldHealth, 1);
            if (delta > 0) { text = "+" + delta; fraction = 0; }
            else if (delta < 0) { text = Float.toString(delta); fraction = Math.min(-delta / 6.0F, 1.0F); }
            else return;
        } else {
            if (oldHealth <= newHealth) return;
            float delta = RandomUtil.round(oldHealth - newHealth, 1);
            text = Float.toString(delta);
            fraction = Math.min(delta / 6.0F, 1.0F);
        }
        int color;
        if (customColorEnabled.isEnabled()) {
            Color selected = customColorColor.getColor();
            Color dark = new Color((int) (selected.getRed() * 0.63F), (int) (selected.getGreen() * 0.63F),
                    (int) (selected.getBlue() * 0.63F), selected.getAlpha());
            color = ColorUtil.blend(dark, selected.brighter(), fraction).getRGB();
        } else color = Color.HSBtoRGB((1.0F - fraction) * 0.33333334F, 0.60000002F, 0.60000002F) | 0xff000000;
        synchronized (particles) { particles.add(new DamageParticle(this, text, color, entity)); }
        synchronized (pending) { pending.remove(entity.getEntityId()); }
    }

    private void clear() {
        synchronized (particles) { particles.clear(); }
        synchronized (pending) { pending.clear(); }
    }
}
