package com.adjustclient.module.impl.render.indicators;

import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.render.ThirdPersonModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

public final class IndicatorsModule extends Module {
    public static IndicatorsModule INSTANCE;
    public final PlayerIndicatorSettings players;
    public final PlayerIndicatorSettings friendly;
    public final PlayerIndicatorSettings enemy;
    public final LivingIndicatorSettings hostile;
    public final LivingIndicatorSettings passive;
    public final IndicatorSettings projectiles;
    public final NumberSetting radius;
    public List<IndicatorEntry> entries = new ArrayList<>();

    public IndicatorsModule() {
        super("Indicators", Category.RENDER);
        INSTANCE = this;
        players = new PlayerIndicatorSettings(this, "Players", true, new Color(0xe84949));
        friendly = new PlayerIndicatorSettings(this, "Friendly", false, new Color(0x5b79b2));
        enemy = new PlayerIndicatorSettings(this, "Enemy", true, new Color(0xb78a28));
        hostile = new LivingIndicatorSettings(this, "Hostile", false, new Color(0x7a7a7a));
        passive = new LivingIndicatorSettings(this, "Passive", false, new Color(0xc0bbbb));
        projectiles = new IndicatorSettings(this, "Projectiles", true);
        radius = new NumberSetting(this, "Radius", 50.0F, 20.0F, 130.0F, 1.0F);
    }

    private static void drawTriangle(float size) {
        float half = size * 0.5F;
        GL11.glVertex2f(0.0F, -half);
        GL11.glVertex2f(-half, half);
        GL11.glVertex2f(half, half);
    }

    @EventHandler(priority = -20)
    public void collectIndicators(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        entries.clear();
        EntityTracker tracker = ServerTracker.INSTANCE.entityTracker;
        if (tracker == null) return;
        int mask = EntityFilter.of(false, players.enabled.isEnabled(), hostile.enabled.isEnabled(),
                passive.enabled.isEnabled(), false, friendly.enabled.isEnabled(), true);
        if (projectiles.enabled.isEnabled()) mask |= 0x100;
        if (enemy.enabled.isEnabled()) mask |= 0x800;
        for (TrackedEntity<?> tracked : tracker.getEntities(mask, (Class<TrackedEntity<?>>) null)) {
            IndicatorSettings selected;
            if (tracked.matchesType(0x100)) {
                if (tracked instanceof TrackedOtherEntity && ((TrackedOtherEntity) tracked).getEntity().inGround) continue;
                selected = projectiles;
            } else if (tracked.matchesType(0x800)) selected = enemy;
            else if (tracked.matchesType(2)) selected = players;
            else if (tracked.matchesType(32)) selected = friendly;
            else if (tracked.matchesType(4)) selected = hostile;
            else if (tracked.matchesType(8)) selected = passive;
            else continue;
            if (selected.enabled.isEnabled()) entries.add(new IndicatorEntry(tracked, selected));
        }
    }

    @EventHandler public void renderIndicators(Render2DEvent event) {
        if (!isEnabled()) return;
        float centerX = (float) (event.scaledResolution.getScaledWidth_double() * 0.5);
        float centerY = (float) (event.scaledResolution.getScaledHeight_double() * 0.5);
        GL11.glTranslatef(centerX, centerY, 0.0F);
        NanoVG.nvgTranslate(NvgContext.vg, centerX, centerY);
        Entity camera = mc.getRenderViewEntity();
        try {
            var renderingFrame = mc.getClass().getDeclaredField("renderingFrame");
            var cameraOverride = mc.getClass().getDeclaredField("cameraOverride");
            renderingFrame.setAccessible(true);
            cameraOverride.setAccessible(true);
            Entity override = (Entity) cameraOverride.get(mc);
            if (renderingFrame.getBoolean(mc) && override != null) camera = override;
        } catch (NoSuchFieldException absentInVanilla) {
        } catch (IllegalAccessException inaccessibleForkField) {
            throw new IllegalStateException(inaccessibleForkField);
        }
        float yaw = ThirdPersonModule.INSTANCE.getYaw();
        float x = (float) (camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.partialTicks);
        float z = (float) (camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.partialTicks);
        float markerRadius = radius.getFloat();
        float aspect = (float) (event.scaledResolution.getScaledWidth_double() / event.scaledResolution.getScaledHeight_double());
        for (IndicatorEntry entry : entries) drawIndicator(entry, markerRadius, x, z, yaw, aspect, event.partialTicks);
        GL11.glTranslatef(-centerX, -centerY, 0.0F);
        NanoVG.nvgTranslate(NvgContext.vg, -centerX, -centerY);
    }

    private static void drawHealth(IndicatorEntry entry, int alpha) {
        if (entry.health == null) return;
        float size = ((LivingIndicatorSettings) entry.settings).getSize();
        float width = size + 3.0F;
        float y = size * 0.5F + 2.0F;
        float left = -width * 0.5F;
        GL11.glBegin(GL11.GL_QUADS);
        GlStateManager.color(0.0F, 0.0F, 0.0F, alpha / 255.0F);
        drawQuad(left, y, width, 2.0F);
        Color color = new Color((alpha << 24) | (entry.healthColor & 0xFFFFFF), true);
        GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F,
                color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        drawQuad(left + 0.5F, y + 0.5F, (width - 1.0F) * entry.health, 1.0F);
        GL11.glEnd();
    }

    private static void drawItem(Item item, int alpha) {
        GlStateManager.disableBlend();
        mc.getRenderItem().renderItemIntoGUI(new ItemStack(item), alpha, false, -8.0F, -8.0F);
    }

    private static void drawQuad(float x, float y, float width, float height) {
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
    }

    private static void drawMarker(IndicatorEntry entry, float angle, int alpha) {
        IndicatorSettings settings = entry.settings;
        if (settings instanceof LivingIndicatorSettings) {
            GL11.glRotatef(angle, 0.0F, 0.0F, 1.0F);
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
            SizedIndicatorSettings sized = (SizedIndicatorSettings) settings;
            Color color;
            if (settings instanceof PlayerIndicatorSettings && ((PlayerIndicatorSettings) settings).teamColor.isEnabled()
                    && entry.teamColor != null) color = entry.teamColor;
            else color = sized.color.getColor();
            color = ColorUtil.withAlpha(color, alpha);
            GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            GL11.glBegin(GL11.GL_TRIANGLES);
            drawTriangle(sized.getSize());
            GL11.glEnd();
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
            GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST);
            GlStateManager.enableTexture2D();
        } else {
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
            GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST);
            GlStateManager.enableTexture2D();
            Entity entity = entry.entity.getEntity();
            Item item;
            if (entity instanceof EntityFireball) item = Items.fire_charge;
            else if (entity instanceof EntityArrow) item = Items.arrow;
            else if (entity instanceof EntityEgg) item = Items.egg;
            else if (entity instanceof EntityEnderPearl) item = Items.ender_pearl;
            else item = Items.snowball;
            drawItem(item, alpha);
        }
    }

    private void drawIndicator(IndicatorEntry entry, float radius, float cameraX, float cameraZ,
                               float yaw, float aspect, float partialTicks) {
        Entity entity = entry.entity.getEntity();
        if (RenderUtil.isInFrustum(entity)) return;
        int alpha = entry.settings.getAlpha();
        if (entry.entity.movementAnimation != null) alpha = (int) (alpha * entry.entity.movementAnimation.value);
        if (alpha < 25) return;
        double x = entry.entity.isRemoved() ? entity.posX : entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double z = entry.entity.isRemoved() ? entity.posZ : entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        float angle = entity == mc.thePlayer.ridingEntity ? 180.0F
                : com.adjustclient.util.RotationUtil.getYawTo(yaw, cameraX, cameraZ, x, z) - yaw;
        float radians = (float) ((double) angle * 0.0174532925199433);
        float dx = MathHelper.sin(radians) * radius * aspect;
        float dy = -MathHelper.cos(radians) * radius;
        GL11.glPushMatrix();
        GL11.glTranslatef(dx, dy, 0.0F);
        NanoVG.nvgTranslate(NvgContext.vg, dx, dy);
        drawLabels(entry, alpha);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        drawHealth(entry, alpha);
        drawMarker(entry, angle, alpha);
        NanoVG.nvgTranslate(NvgContext.vg, -dx, -dy);
        GL11.glPopMatrix();
    }

    private static void drawLabels(IndicatorEntry entry, int alpha) {
        boolean living = entry.settings instanceof LivingIndicatorSettings;
        float half = living ? ((SizedIndicatorSettings) entry.settings).getSize() * 0.5F : 5.0F;
        if (entry.settings.isNameEnabled()) {
            String text = (entry.enemy ? net.minecraft.util.ChatColor.BOLD.toString() : "") + entry.label;
            Fonts.TAHOMA_10.drawString(text, entry.nameX, -half - 5.5F,
                    ColorUtil.withAlpha(entry.color, alpha).getRGB());
        }
        if (entry.settings.isDistanceEnabled()) {
            float offset = living ? 3.0F : 4.0F;
            if (entry.health != null) offset = 7.0F;
            Fonts.TAHOMA_8.drawString(entry.distanceLabel, entry.distanceX, half + offset,
                    ColorUtil.withAlpha(entry.distanceColor, alpha).getRGB());
        }
    }

    @Override public void onEnable() { entries = new ArrayList<>(); }
    @Override public void onDisable() { entries = null; }
}
