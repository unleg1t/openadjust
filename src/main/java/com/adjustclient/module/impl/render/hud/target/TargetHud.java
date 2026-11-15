package com.adjustclient.module.impl.render.hud.target;

import com.adjustclient.event.Render2DEvent;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.util.RandomUtil;

public final class TargetHud implements MinecraftInstance {

    public BooleanSetting enabled;
    private List<TargetHudEntry> targets;
    private ExpandSetting group;

    public TargetHud(HUDModule hud) {
        this.targets = new ArrayList<>();
        this.group = new ExpandSetting(hud, "Target HUD");
        this.group.enabledPredicate = (BooleanSupplier) () -> this.enabled.isEnabled();
        this.enabled = new BooleanSetting(this.group, "Enabled", true, "Yes", "No");
    }

    /**
     * Shows {@code entity} (moves nothing when it is already listed: its fade-out flag is cleared and its tick counter set to
     * {@code ticks}); otherwise a new entry is put at the front of the list.
     */
    public void setTarget(EntityPlayer entity, int ticks) {
        for (TargetHudEntry entry : this.targets) {
            if (entry.target == entity) {
                entry.fadingOut = false;
                entry.ticks = ticks;
                return;
            }
        }
        this.targets.addFirst(new TargetHudEntry(entity, ticks));
    }

    public void setTarget(EntityPlayer entity) {
        this.setTarget(entity, 0);
    }

    /** Removes all entries (world change / teleport). */
    public void clear() {
        this.targets.clear();
    }

    /** Tick handler: keeps the target list up to date. */
    public void tick() {
        if (!this.enabled.isEnabled()) {
            this.targets.clear();
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiChat) {
            this.setTarget(mc.thePlayer, 69);
        } else {
            if (KillAuraModule.INSTANCE.isEnabled()) {
                com.adjustclient.module.impl.combat.killaura.target.TargetInfo holder = KillAuraModule.INSTANCE.targetSelector.rotationTarget;
                if (holder != null) {
                    Entity target = holder.entity.getEntity();
                    if (target instanceof EntityPlayer) {                  // type-id range 15370 .. 15375
                        this.setTarget((EntityPlayer) target, 0);
                    }
                }
            }
        }
        Iterator<TargetHudEntry> iterator = this.targets.iterator();
        while (iterator.hasNext()) {
            TargetHudEntry entry = iterator.next();
            if (!entry.fadingOut) {
                EntityPlayer target = entry.target;
                if (!target.isDead && target.getDataWatcher().getWatchableObjectFloat(6) > 0.0F) {
                    EntityPlayer player = mc.thePlayer;
                    double dx = player.posX - target.posX;
                    double dy = player.posY - target.posY;
                    double dz = player.posZ - target.posZ;
                    if (dx * dx + dy * dy + dz * dz <= 144.0) {
                        entry.ticks++;
                        // after 20 ticks only the local player stays (while the chat is open)
                        if (entry.ticks < 21 || (target == player && mc.currentScreen instanceof GuiChat)) {
                            entry.update();
                            continue;
                        }
                    }
                }
            }
            // invalid / too far / expired: fade out, then remove
            if (entry.fadingOut) {
                if (entry.fade.isFinished()) {
                    iterator.remove();
                }
            } else {
                entry.fadingOut = true;
            }
        }
    }

    private static int clamp255(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }

    /**
     * Render2D part of the HUD module (called with the HUD colour and the position of the first box: screen centre + (18, 18)).
     * Draws every entry (two per row, 1 px apart), queues the head and the item drawing as post tasks of the event.
     */
    public void render(Render2DEvent event, Color hudColor, float startX, float startY) {
        float x = startX;
        float y = startY;
        int column = 0;
        float partialTicks = event.partialTicks;
        for (TargetHudEntry entry : this.targets) {
            Animation fade = entry.fade;
            if (entry.fadingOut) {
                fade.duration = 250L;
                fade.update(0.0F);
            } else {
                fade.update(1.0F);
            }
            float fadeValue = fade.getValue();
            int alpha = clamp255((int) (fadeValue * 255.0F));
            Color color = hudColor;
            if (color.getAlpha() != alpha) {
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            }
            int bgAlpha = clamp255((int) (fadeValue * 170.0F));
            float width = entry.width;
            float height = entry.height;
            float right = x + width;
            float bottom = y + height;
            int bgColor = (bgAlpha << 24) | 0x131313;
            NvgDraw.fillRect(bgColor, x, y, right, bottom);

            final float boxX = x;
            final float boxY = y;
            final int boxAlpha = alpha;
            event.postTasks.add(() -> this.drawHead(entry, boxAlpha, partialTicks, boxX, boxY));

            entry.healthAnim.update(entry.healthRatio);
            entry.trailAnim.update(entry.healthRatio);

            // health bar: 3 px high at the bottom of the box, 1.5 px inset left and right
            NvgContext.pushEvenScale();
            Vector4f bar = NvgContext.toPixels(x + 1.5F, bottom - 4.5F, right - 1.5F, bottom - 1.5F);
            NvgDraw.fillRect(((alpha >> 1) << 24) | 0x27272A, bar.x, bar.y, bar.z, bar.w);   // border colour
            float thickness;
            if (NvgContext.scaleFactor == null) {
                thickness = 0.5F;
            } else {
                float factor = NvgContext.scaleFactor;
                thickness = (float) (NvgContext.roundScaledPixels ? Math.round(factor) : (int) factor) * 0.5F;
            }
            float innerLeft = bar.x + thickness;
            float innerTop = bar.y + thickness;
            float innerRight = bar.z - thickness;
            float innerBottom = bar.w - thickness;
            NvgDraw.fillRect(bgColor, innerLeft, innerTop, innerRight, innerBottom);
            float trailValue = entry.trailAnim.getValue();
            float healthValue = entry.healthAnim.getValue();
            float healthRight = innerLeft + ((healthValue * (innerRight - bar.x)) - thickness);
            float trailRight = ((trailValue * (innerRight - bar.x)) - thickness) + innerLeft;
            ColorSetting hudSetting = HUDModule.colorSetting;
            if (hudSetting.getMode() == ColorSetting.Mode.RAINBOW) {
                // dark trail then bright health: four horizontal gradients each, hue = (time % 5000 + offset) / 5000
                float trailSpan = trailRight - innerLeft;
                this.fillGradientBar(alpha, 0.504F, innerLeft, innerTop, innerBottom, trailSpan);
                float healthSpan = healthRight - innerLeft;
                this.fillGradientBar(alpha, 0.8F, innerLeft, innerTop, innerBottom, healthSpan);
            } else {
                int rgb = color.getRGB();
                int dark = new Color((int) (((rgb >> 16) & 0xFF) * 0.63F), (int) (((rgb >> 8) & 0xFF) * 0.63F),
                        (int) ((rgb & 0xFF) * 0.63F), rgb >>> 24).getRGB();     // the HUD colour darkened to 63 %
                RenderUtil.drawThemedRect(dark, innerLeft, innerTop, trailRight, innerBottom, entry.healthRatio);
                RenderUtil.drawThemedRect(color.getRGB(), innerLeft, innerTop, healthRight, innerBottom, entry.healthRatio);
            }
            if (entry.absorptionAnim != null) {
                entry.absorptionAnim.update(entry.absorptionRatio);
                float absorptionRight = (((innerRight - bar.x) * entry.absorptionAnim.getValue()) - thickness) + innerLeft;
                int rgb = color.getRGB();
                int a24 = (rgb >>> 24) << 24;
                if (((rgb >> 16) & 0xFF) != 0 || (rgb & 0xFFFF) != 0) {
                    // brighter variant of the HUD colour: every channel / 0.45, clamped to 255
                    int r = Math.min((int) (((rgb >> 16) & 0xFF) / 0.45F), 255);
                    int g = Math.min((int) (((rgb >> 8) & 0xFF) / 0.45F), 255);
                    int b = Math.min((int) ((rgb & 0xFF) / 0.45F), 255);
                    RenderUtil.drawThemedRect(a24 | (r << 16) | (g << 8) | b, innerLeft, innerTop, absorptionRight, innerBottom, entry.absorptionRatio);
                } else {
                    RenderUtil.drawThemedRect(a24 | 0x010101, innerLeft, innerTop, absorptionRight, innerBottom, entry.absorptionRatio);   // pure black HUD colour
                }
            }
            NvgContext.popEvenScale();

            // text
            int textAlpha = clamp255((int) (fadeValue * 220.0F));
            int textColor = (textAlpha << 24) | 0xE6E6EC;
            float textX = x + 19.5F;
            float textY = y + 3.5F;
            String shownName = entry.name;
            if (StreamerModule.nameMap != null) {                            // Streamer module: hide the real name
                shownName = StreamerModule.replaceName(entry.name, entry.target.getGameProfile().getName());
            }
            float nameEndX = Fonts.TAHOMA_12.drawString(shownName, textX, textY, textColor);
            if (entry.clientTag != null) {
                RenderUtil.drawColoredString(Fonts.TAHOMA_12, entry.clientTag, color.getRGB(), nameEndX + 2.25F, textY);
            }
            float change = entry.healthChange;
            String sign = null;
            String number = null;
            if (change > 0.0F) {
                sign = "+";
                number = RandomUtil.format(change);
            } else if (change < 0.0F) {
                sign = "-";
                number = RandomUtil.format(Math.abs(change));
            }
            if (sign != null) {
                float numberX = (right - 2.0F) - Fonts.TAHOMA_10.getStringWidth(number);
                Fonts.TAHOMA_8.drawString(sign, numberX - Fonts.TAHOMA_8.getStringWidth(sign), y + 16.5F, textColor);
                Fonts.TAHOMA_10.drawString(number, numberX, y + 16.0F, textColor);
            }

            final float itemsX = x + 20.0F;
            final float itemsY = y + 10.0F;
            event.postTasks.add(() -> this.drawItems(entry, boxAlpha, itemsX, itemsY));

            // two boxes per row
            if (column + 1 >= 2) {
                column = 0;
                y = (height + 1.0F) + y;
                x = startX;
            } else {
                x = (width + 1.0F) + x;
                column++;
            }
        }
    }

    private void fillGradientBar(int alpha, float brightness, float left, float top, float bottom, float span) {
        int[] offsets = {5000, 4700, 4400, 4100, 3800};
        for (int i = 0; i < 4; i++) {
            float start = (float) (System.currentTimeMillis() % 5000L) + (float) offsets[i];
            float end = (float) (System.currentTimeMillis() % 5000L) + (float) offsets[i + 1];
            int startColor = (Color.getHSBColor(start / 5000.0F, 0.45F, brightness).getRGB() & 0xFFFFFF) | (alpha << 24);
            int endColor = (Color.getHSBColor(end / 5000.0F, 0.45F, brightness).getRGB() & 0xFFFFFF) | (alpha << 24);
            NvgDraw.fillGradientRect(startColor, endColor, left + span * (i * 0.25F), top, left + span * ((i + 1) * 0.25F), bottom);
        }
    }

    /**
     * Draws the target's head with plain GL (half scale, doubled coordinates) tinted red while the target is hurt: the tint grows
     * with the hurt state of the entity (interpolated with the partial ticks).
     */
    private void drawHead(TargetHudEntry entry, int alpha, float partialTicks, float x, float y) {
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        EntityPlayer target = entry.target;
        int previous = target.previousHurtTime;
        int current = target.hurtTime;
        int maxValue = target.maxHurtTime;
        float interpolated = (float) previous + (float) (current - previous) * partialTicks;
        float numerator;
        float denominator;
        if (target.getDataWatcher().getWatchableObjectFloat(6) > 0.0F) {
            numerator = Math.min(interpolated, (float) maxValue);
            denominator = (float) maxValue * 2.15F;
        } else {
            numerator = Math.min((float) Math.max(target.deathTime, 0), 20.0F);
            denominator = 43.0F;
        }
        float ratio = numerator / denominator;
        float headX = (x + 2.0F) * 2.0F;
        float headY = (y + 2.0F) * 2.0F;
        float t = ratio < 0.0F ? 0.0F : Math.min(ratio, 1.0F);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        Color neutral = Color.WHITE;
        int r = (int) (t * 237.0F + neutral.getRed() * (1.0F - t));
        int g = (int) (neutral.getGreen() * (1.0F - t) + t * 60.0F);
        int b = (int) (neutral.getBlue() * (1.0F - t) + t * 60.0F);
        GlStateManager.color(r / 255.0F, g / 255.0F, b / 255.0F, alpha / 255.0F);
        RenderUtil.drawHead(entry.skin, headX, headY);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
    }

    /** Draws the five items (held item, armor) of the entry in a row, 8 px apart, at half scale. */
    private void drawItems(TargetHudEntry entry, int alpha, float x, float y) {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        float offset = 0.0F;
        for (ItemStack stack : entry.items) {
            if (stack != null) {
                float itemX = (offset + x) * 2.0F;
                float itemY = y * 2.0F;
                GlStateManager.disableLighting();
                RenderHelper.enableGUIStandardItemLighting();
                Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, alpha, true, itemX, itemY);
                RenderHelper.disableStandardItemLighting();
                offset = offset + 8.0F;
            }
        }
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
