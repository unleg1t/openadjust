package com.adjustclient.modules.impl.render.hud.widget.radar;

import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.modules.impl.render.hud.widget.HudWidget;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.ImageLoader;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.core.rendering.font.image.NvgImage;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.joml.Vector4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.lwjgl.nanovg.NanoVG.*;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.modules.impl.render.ThirdPersonModule;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.EntityFilter;
import com.adjustclient.utilities.anticheat.entity.EntityTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedEntity;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;

public final class RadarWidget extends HudWidget {

    private NumberSetting size;
    private NumberSetting distance;
    private NumberSetting smoothing;
    private RadarGroup local;
    private RadarGroup players;
    private RadarGroup enemy;
    private RadarGroup friendly;
    private RadarGroup hostile;
    private RadarGroup passive;
    private List<RadarTarget> targets;
    private float smoothedYaw;
    private double lastFrameTime;

    public RadarWidget(Movable movable) {
        super("Radar", null, movable);
        this.targets = new ArrayList<>();
        this.lastFrameTime = System.nanoTime() / 1.0E9;
    }

    @Override
    public void createSettings(ExpandSetting parent) {
        ExpandSetting radar = new ExpandSetting(parent, this.name);
        radar.enabledPredicate = (BooleanSupplier) () -> this.enabled.isEnabled();
        this.enabled = new BooleanSetting(radar, "Enabled", false, "Yes", "No");
        this.movable.canDrag = (BooleanSupplier) () -> this.enabled.isEnabled() && Minecraft.getMinecraft().thePlayer != null;
        this.size = new NumberSetting(radar, "Size", 80, 50, 100, 5);
        this.distance = new NumberSetting(radar, "Distance", 30, 10, 120, 5);
        this.smoothing = new NumberSetting(radar, "Smoothing", 0.3F, 0.0F, 1.0F, 0.1F)
                .formatter(setting -> ((Number) setting.getValue()).floatValue() == 0.0F ? "Off" : NumberSetting.DEGREES.transform(setting));
        this.local = new RadarGroup(radar, "Local", true, new Color(67, 90, 209), 220, 2);
        this.players = new RadarGroup(radar, "Players", true, new Color(232, 73, 73, 255), 200, 2);
        this.enemy = new RadarGroup(radar, "Enemy", true, new Color(183, 138, 40, 255), 220, 2);
        this.friendly = new RadarGroup(radar, "Friendly", true, new Color(91, 121, 178, 255), 170, 2);
        this.hostile = new RadarGroup(radar, "Hostile", false, new Color(122, 122, 122, 255), 130, 1);
        this.passive = new RadarGroup(radar, "Passive", false, new Color(192, 187, 187, 255), 120, 1);
    }

    @com.adjustclient.core.events.EventHandler(priority = -20)
    public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !this.isActive()) {
            return;
        }
        this.targets.clear();
        EntityTracker tracker = ServerTracker.INSTANCE.entityTracker;
        if (tracker == null || Minecraft.getMinecraft().thePlayer == null) {
            return;
        }
        int mask = EntityFilter.of(false, this.players.isEnabled(), this.friendly.isEnabled(),
                this.hostile.isEnabled(), false, this.passive.isEnabled(), true);
        if (this.enemy.isEnabled()) {
            mask |= 0x800;
        }
        List<TrackedLivingEntity> entries = tracker.getEntities(mask, TrackedLivingEntity.class);
        for (TrackedEntity entry : entries) {
            RadarGroup group;
            if (entry.matchesType(0x800)) {
                group = this.enemy;
            } else if (entry.matchesType(0x2)) {
                group = this.players;
            } else if (entry.matchesType(0x20)) {
                group = this.passive;
            } else if (entry.matchesType(0x4)) {
                group = this.friendly;
            } else if (entry.matchesType(0x8)) {
                group = this.hostile;
            } else {
                continue;
            }
            if (!group.isEnabled()) {
                continue;
            }
            EntityLivingBase entity = (EntityLivingBase) entry.getEntity();
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            double dx = player.posX - entity.posX;
            double dz = player.posZ - entity.posZ;
            double distance = Math.sqrt(dx * dx + 0.0 + dz * dz);
            if (distance <= (double) ((Number) this.distance.getValue()).intValue()) {
                this.targets.add(new RadarTarget(entry, group));
            }
        }
    }

    /** Render2D handler: draws the radar (panel, optional cross hair, entity dots, local arrow). */
    @com.adjustclient.core.events.EventHandler
    public void onRender2D(Render2DEvent event) {
        if (!this.isActive()) {
            return;
        }
        int size = ((Number) this.size.getValue()).intValue();
        this.drawFrame(event.scaledResolution, (float) size, (float) size);
        float left = this.movable.updatePosition().x;
        float top = this.movable.updatePosition().y;
        float contentTop = top + 9.0F;
        float contentHeight = this.movable.height - 8.0F;                    // = size + 1
        float centerY = contentHeight * 0.5F + contentTop;
        float bottom = contentTop + contentHeight;
        float right = left + this.movable.width;                            // width = size
        float centerX = this.movable.width * 0.5F + left;

        if (!this.local.isEnabled()) {
            // no local arrow: draw a cross hair (two 0.5 wide lines) in Color(230, 230, 230, 120)
            NvgContext.pushEvenScale();
            Vector4f vertical = NvgContext.toPixels(centerX, contentTop, centerX + 0.5F, bottom);
            int lineColor = new Color(230, 230, 230, 120).getRGB();
            NvgDraw.fillRect(lineColor, vertical.x, vertical.y, vertical.z, vertical.w - 0.5F);
            Vector4f horizontal = NvgContext.toPixels(left, centerY, right, centerY + 0.5F);
            NvgDraw.fillRect(0.5F + horizontal.x, horizontal.y, horizontal.z - 0.5F, horizontal.w);   // current fill colour
            NvgContext.popEvenScale();
        }

        // interpolated position of the local player
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        float partialTicks = event.partialTicks;
        double playerX = (player.posX - player.lastTickPosX) * partialTicks + player.lastTickPosX;
        double playerZ = (player.posZ - player.lastTickPosZ) * partialTicks + player.lastTickPosZ;
        float halfSize = (float) size * 0.5F;
        NvgDraw.pushScissor(left + 0.5F, contentTop, (float) size - 1.0F, contentHeight);

        // frame time (System.nanoTime() via QueryPerformanceCounter)
        double now = (double) System.nanoTime() / 1.0E9;
        double delta = now - this.lastFrameTime;
        this.lastFrameTime = now;

        // yaw the radar is rotated by, optionally smoothed
        float targetYaw = ThirdPersonModule.INSTANCE.getYaw();
        float smoothing = ((Number) this.smoothing.getValue()).floatValue();
        float shownYaw;
        if (smoothing == 0.0F) {
            this.smoothedYaw = targetYaw;
            shownYaw = targetYaw;
        } else {
            float previous = this.smoothedYaw;
            float diff = MathHelper.wrapAngleTo180_float(targetYaw - previous);
            float step = (float) delta * (3.0F / smoothing);
            float current = previous;
            while (true) {
                current = current + diff * step;
                this.smoothedYaw = current;
                if (previous == current) {
                    break;
                }
                // keeps adding the same increment until the smoothed yaw is within 150 degrees of the target
                if (Math.abs(MathHelper.wrapAngleTo180_float(current - targetYaw)) <= 150.0F) {
                    break;
                }
            }
            shownYaw = previous;                                              // the value from before this frame's update is drawn
        }

        for (RadarTarget target : this.targets) {
            EntityLivingBase entity = (EntityLivingBase) target.target().getEntity();
            double dx = entity.posX - playerX;
            double dz = entity.posZ - playerZ;
            double distance = (float) Math.sqrt(dx * dx + 0.0 + dz * dz);
            float angle = (float) (Math.atan2(dx, dz) * 57.29577951308232);
            float relative = MathHelper.wrapAngleTo180_float((angle - 180.0F) + shownYaw);
            double radians = (double) relative * 0.0174532925199433;
            double hypot = Math.hypot(distance, distance);
            int maxDistance = ((Number) this.distance.getValue()).intValue();
            double scaled = hypot / (double) maxDistance * (double) halfSize;
            float dotX = (float) (Math.sin(radians) * scaled) + centerX;
            float dotY = (float) (Math.cos(radians) * scaled) + centerY;
            RadarGroup group = target.targetSettings();
            int dotSize = group.getSize() - 1;
            float dotRadius = ((float) dotSize / 2.0F) * 0.5F + 0.5F;
            if (left - dotRadius < dotX && contentTop - dotRadius < dotY && dotX <= right + dotRadius && dotY <= bottom + dotRadius) {
                NvgDraw.fillCircle((group.getAlpha() << 24) | (group.getColor() & 0xFFFFFF), dotX, dotY, dotRadius);
            }
        }

        if (this.local.isEnabled()) {
            float arrowAngle = MathHelper.wrapAngleTo180_float(shownYaw - targetYaw);
            this.drawArrow((this.local.getAlpha() << 24) | (this.local.getColor() & 0xFFFFFF), centerX, centerY, arrowAngle);
        }
        NvgDraw.popScissor();
    }

    /** Draws the local player arrow texture rotated by {@code angle} around ({@code x}, {@code y}). */
    private void drawArrow(int color, float x, float y, float angle) {
        ResourceLocation texture = new ResourceLocation("adjust", "textures/other/direction_arrow.png");
        float arrowSize = (float) this.local.getSize() + 3.0F;
        float half = arrowSize * 0.5F;
        NvgImage image = ImageLoader.getImage(texture, 0);
        nvgSave(NvgContext.vg);
        nvgTranslate(NvgContext.vg, x, y);
        nvgRotate(NvgContext.vg, -angle / 90.0F);                             // sic: divided by 90, not converted to radians
        image.draw(color, -half, -half, arrowSize, arrowSize);
        nvgRestore(NvgContext.vg);
    }
}
