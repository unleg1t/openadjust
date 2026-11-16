package com.adjustclient.module.impl.render.esp.render;

import com.adjustclient.module.impl.render.esp.ItemPool;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.util.anticheat.entity.TrackedItem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.joml.Vector4d;

public class ItemPoolEspRenderer extends GroupedEspRenderer {
    public final List<TrackedItem> targets;
    public final com.adjustclient.module.impl.render.esp.settings.EspTheme theme;
    public final com.adjustclient.module.impl.render.esp.settings.ItemEspSettings settings;
    public List<ItemPoolLine> lines = new ArrayList<>();
    public com.adjustclient.module.impl.render.esp.settings.EspTheme effectiveTheme;
    public com.adjustclient.render.font.TextRenderer font;
    public float scale;
    public Object field44;

    public ItemPoolEspRenderer(java.util.function.Supplier<net.minecraft.util.Vec3> positionSupplier,
            net.minecraft.util.Vec3 center, List<TrackedItem> targets,
            com.adjustclient.module.impl.render.esp.settings.EspTheme theme,
            com.adjustclient.module.impl.render.esp.settings.ItemEspSettings settings) {
        this.positionSupplier = positionSupplier;
        this.center = center;
        this.targets = targets;
        this.theme = theme;
        this.settings = settings;
    }

    @Override public void update() { updateFont(); }
    @Override public Vector4d project(ScaledResolution resolution, float partialTicks) {
        return projectGroup(resolution, partialTicks);
    }

    public void updateFont() {
        effectiveTheme = theme;
        scale = settings.nametagScale.getFloat() * 0.5F;
        font = switch (effectiveTheme) {
            case MINECRAFT -> (com.adjustclient.render.font.TextRenderer) net.minecraft.client.Minecraft.getMinecraft().fontRendererObj;
            case PIXEL -> (com.adjustclient.render.font.TextRenderer) net.minecraft.client.Minecraft.getMinecraft().pixelFontRenderer;
            case ADJUST -> com.adjustclient.render.font.Fonts.tahoma(scale * 20.0F);
        };
        rebuild();
    }

    public ItemPoolKey keyFor(TrackedItem tracked) {
        ItemStack stack = tracked.getItemStack();
        String name = stack.hasDisplayName() ? stack.getDisplayName() : null;
        int metadata = stack.getItem().getHasSubtypes() ? stack.getMetadata() : 0;
        return new ItemPoolKey(stack.getItem(), metadata, name);
    }

    public void updateDistance(com.adjustclient.event.Render2DEvent event, Vector4d bounds,
            ScaledResolution resolution) {
        if (!settings.nametagsEnabled.isEnabled() || !settings.nametagBind.getValue().isHeld()) return;
        int alpha = settings.maxAlpha.getInt();
        if (alpha < 25) return;
        float left = (float) bounds.x;
        float top = (float) bounds.y;
        float right = (float) bounds.z;
        float bottom = (float) bounds.w;
        float x = (right - left) * 0.5F + left;
        if (settings.useFov.isEnabled()) {
            float dx = resolution.getScaledWidth() * 0.5F - x;
            float dy = resolution.getScaledHeight() * 0.5F - ((bottom - top) * 0.5F + top);
            float distance = Math.min((float) Math.sqrt(dx * dx + dy * dy) * (float) resolution.getScaleFactor_double(),
                    (float) resolution.getScaledWidth());
            alpha -= (int) (distance / resolution.getScaledWidth() * alpha);
            if (alpha < 25) return;
        }
        drawLines(event, x, top, alpha);
    }

    public void drawLines(com.adjustclient.event.Render2DEvent event, float x, float y, int alpha) {
        if (lines.isEmpty()) return;
        if (effectiveTheme.pixel) event.postTasks.add(() -> {
            net.minecraft.client.renderer.GlStateManager.enableBlend();
            net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        });
        int background = new Color(0, 0, 0, Math.min(145, alpha)).getRGB();
        for (int index = 0; index < lines.size(); index++) {
            ItemPoolLine line = lines.get(index);
            float left = x - line.width * 0.5F;
            int color = alpha << 24 | line.color.getRGB() & 0xFFFFFF;
            if (effectiveTheme.pixel) {
                float top = y + scale * 14.0F * index;
                if (settings.background.isEnabled()) {
                    float padding = scale * 4.0F;
                    com.adjustclient.render.font.image.NvgDraw.fillRect(background, left - padding,
                            top - padding, left + line.width + padding, top + scale * 6.0F + padding);
                }
                event.postTasks.add(() -> {
                    ((com.adjustclient.render.font.TextRenderer) font).drawString(line.text, left, top, color, scale);
                });
            } else {
                // non-pixel themes use an 11.0F line pitch, not the pixel theme's 14.0F
                float top = y + index * (scale * 11.0F);
                ((com.adjustclient.render.font.TextRenderer) font).drawString(line.text, left, top, color, scale);
            }
        }
    }

    public void rebuild() {
        Map<ItemPoolKey, ItemPoolEntry> grouped = new java.util.HashMap<>();
        for (TrackedItem tracked : targets) {
            ItemStack stack = tracked.getItemStack();
            ItemPoolEntry entry = grouped.computeIfAbsent(keyFor(tracked), key -> {
                return new ItemPoolEntry(stack);
            });
            entry.count += Math.max(1, stack.stackSize);
            entry.name = stack.getItem() == null ? "Item" : stack.getItem().getItemStackDisplayName(stack);
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display", 10)) {
                var display = stack.getTagCompound().getCompoundTag("display");
                if (display.hasKey("Name", 8)) entry.name = display.getString("Name");
            }
        }
        List<ItemPoolEntry> ordered = new ArrayList<>(grouped.values());
        ordered.sort(Comparator.comparingInt(entry -> {
            var item = entry.stack.getItem();
            if (item instanceof net.minecraft.item.ItemSword) return Integer.MIN_VALUE;
            if (item instanceof net.minecraft.item.ItemTool tool)
                return Integer.MIN_VALUE + 10 + tool.getToolMaterial().ordinal();
            return Integer.MAX_VALUE - entry.count;
        }));
        List<ItemPoolLine> result = new ArrayList<>();
        var textRenderer = (com.adjustclient.render.font.TextRenderer) font;
        for (ItemPoolEntry entry : ordered) {
            if (result.size() >= 3) break;
            ItemPoolLine line = new ItemPoolLine();
            line.text = entry.count == 1 ? entry.name : entry.name + net.minecraft.util.ChatColor.GRAY + " x" + entry.count;
            Color color = com.adjustclient.module.impl.render.esp.ModernItemName.lookup(entry.stack);
            line.color = color == null ? settings.nametagColor.getColor() : color;
            line.width = textRenderer.getStringWidth(line.text, scale);
            result.add(line);
        }
        if (ordered.size() > result.size()) {
            int remaining = ordered.size() - result.size();
            ItemPoolLine more = new ItemPoolLine();
            more.text = net.minecraft.util.ChatColor.GRAY + "" + remaining + " more item" + (remaining == 1 ? "" : "s") + "...";
            more.color = settings.nametagColor.getColor();
            more.width = textRenderer.getStringWidth(more.text, scale);
            result.add(more);
        }
        if (settings.distance.isEnabled()) {
            var player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
            long distance = Math.round(Math.sqrt(player.getDistanceSq(center.xCoord, center.yCoord, center.zCoord)));
            ItemPoolLine line = new ItemPoolLine();
            line.text = net.minecraft.util.ChatColor.GRAY + "" + distance + "m";
            line.color = settings.nametagColor.getColor();
            line.width = textRenderer.getStringWidth(line.text, scale);
            result.add(0, line);
        }
        lines = result;
    }

}
