package com.adjustclient.module.impl.render.esp.render;

import com.adjustclient.render.RenderUtil;
import com.adjustclient.util.anticheat.entity.TrackedEntity;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.joml.Vector4d;
import static com.adjustclient.render.font.image.NvgDraw.fillRect;

public class EntityEspRenderer extends CulledEspRenderer {
    private static ResourceLocation warningTexture;
    public static int primitiveSlot1334;
    public com.adjustclient.module.impl.render.esp.settings.EspTheme generalSettings;
    public com.adjustclient.module.impl.render.esp.settings.EntityEspSettings modeSetting;
    public com.adjustclient.module.impl.render.esp.ESPModule boxSetting;
    public Float healthSetting;
    public Float field28;
    public com.adjustclient.render.font.TextRenderer nameSetting;
    public com.adjustclient.render.font.TextRenderer itemNameFont;
    public String displayName;
    public String adjustUserName;
    public String healthText;
    public String absorptionText;
    public String distanceText;
    public List<com.adjustclient.module.impl.render.esp.EspPlayerFlag> flagLines;
    public List<EspIconEntry> equipmentIcons;
    public List<EspPotionEntry> potionIcons;
    public String heldItemName;
    public com.adjustclient.module.impl.render.esp.settings.EspTheme effectiveTheme;
    public float textScale;
    public float halfWidth;
    public float displayNameWidth;
    public float healthTextWidth;
    public float field96;
    public float distanceTextWidth;
    public float statusTextWidth;
    public float potionWidth;
    public float healthHue;
    public float healthRatio;
    public float barRatio;
    public float nameWidth;
    public int nameColor;
    public int healthColor;
    public int barColor;
    public Object field140;
    public boolean hacker;
    public boolean healthColorCached;

    static {
        warningTexture = new ResourceLocation("adjust", "textures/warning/normal.png");
    }

    // Preview allocation initializes these fields at its call site.
    public EntityEspRenderer() { }

    public EntityEspRenderer(TrackedEntity<?> target,
            com.adjustclient.module.impl.render.esp.settings.EspTheme theme,
            com.adjustclient.module.impl.render.esp.settings.EntityEspSettings settings,
            com.adjustclient.module.impl.render.esp.ESPModule owner) {
        this.target = target;
        this.generalSettings = theme;
        this.modeSetting = settings;
        this.boxSetting = owner;
    }

    @Override public void update() { updateSettings(); }

    @Override public Vector4d project(ScaledResolution resolution, float partialTicks) {
        return super.project(resolution, partialTicks);
    }

    public void updateSettings() {
        effectiveTheme = generalSettings;
        if (modeSetting.nametagsEnabled.isEnabled()) render();
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.LivingEspSettings living
                && living.healthBarEnabled.isEnabled()) maxHealth();
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.MobEspSettings mob) {
            if (mob.itemNameEnabled.isEnabled()) updateDisplayName(mob);
            if (mob.equipment.isEnabled()) renderArmor();
            if (mob.potions.isEnabled()) collectEquipment();
            if (mob instanceof com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings player) {
                if (player.playerFlagsEnabled.isEnabled()) collectPlayerFlags(player);
                hacker = player.playerHacking.isEnabled()
                        && com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector.isCheater(target.entityId);
            }
            if (mob.armorEnabled.isEnabled()) health();
        } else if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.ItemEspSettings item) {
            if (item.durabilityEnabled.isEnabled()) health();
        } else if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.TntEspSettings tnt) {
            if (tnt.countdownEnabled.isEnabled()) health();
        }
    }

    public com.adjustclient.render.font.TextRenderer fontForScale(float scale) {
        return switch (effectiveTheme) {
            case MINECRAFT -> (com.adjustclient.render.font.TextRenderer) Minecraft.getMinecraft().fontRendererObj;
            case PIXEL -> (com.adjustclient.render.font.TextRenderer) Minecraft.getMinecraft().pixelFontRenderer;
            case ADJUST -> com.adjustclient.render.font.Fonts.tahoma(scale * 20.0F);
        };
    }

    public void updateDisplayName(com.adjustclient.module.impl.render.esp.settings.MobEspSettings settings) {
        halfWidth = settings.itemNameScale.getFloat() * 0.5F;
        itemNameFont = fontForScale(halfWidth);
        if (!(target instanceof TrackedLivingEntity)) return;
        ItemStack stack = entityOf((EntityLivingBase) target.getEntity());
        if (stack == null || stack.getItem() == null) return;
        if (settings.customItemNamesOnly.isEnabled()
                && (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("display", 10))) return;
        heldItemName = stack.getRarity().rarityColor + stack.getDisplayName();
        if (target.removed) heldItemName = net.minecraft.util.StringUtils.stripControlCodes(heldItemName);
        nameWidth = itemNameFont.getStringWidth(heldItemName, halfWidth);
    }

    public void collectPlayerFlags(com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings playerSettings) {
        if (!(target instanceof com.adjustclient.util.anticheat.entity.TrackedFakePlayer)) return;
        flagLines = new ArrayList<>();
        for (var flag : com.adjustclient.module.impl.render.esp.EspPlayerFlag.values()) {
            var settings = playerSettings.flagGroups.get(flag);
            if (settings.enabled.isEnabled() && flag.test((com.adjustclient.util.anticheat.entity.TrackedFakePlayer) target)) flagLines.add(flag);
        }
    }

    public static String buildItemLines(TrackedEntity<?> tracked) {
        String prefix = net.minecraft.util.ChatColor.GRAY.toString() + net.minecraft.util.ChatColor.RESET;
        if (tracked instanceof com.adjustclient.util.anticheat.entity.TrackedItem item) {
            if (item.collected) {
                String collector = item.field40 == null ? "Collected" : ((TrackedLivingEntity) item.field40).getDisplayName();
                return prefix + " (" + collector + ")";
            }
            ItemStack stack = item.getItemStack();
            return stack.stackSize == 1 ? "" : prefix + " x" + stack.stackSize;
        }
        if (tracked instanceof com.adjustclient.util.anticheat.entity.TrackedLocalPlayer local) {
            return local.lookupResult == null ? "" : prefix + " (" + local.lookupResult + ")";
        }
        if (tracked instanceof com.adjustclient.util.anticheat.entity.TrackedTnt tnt) {
            return tnt.fuseText == null ? prefix : prefix + " (" + tnt.fuseText + ")";
        }
        return "";
    }

    public void drawBox(TrackedLivingEntity tracked) {
        if (healthColorCached) return;
        EntityLivingBase living = tracked.getEntity();
        float maximum = living.getMaxHealth();
        healthHue = Math.min(living.getHealth(), maximum) / maximum;
        barColor = tracked.removed ? new Color(233, 235, 234).getRGB()
                : Color.HSBtoRGB(healthHue * 0.33333334F, 0.60000002F, 0.60000002F) | 0xFF000000;
        healthColorCached = true;
    }

    public float healthFraction(com.adjustclient.util.anticheat.entity.TrackedItem tracked) {
        ItemStack stack = tracked.getItemStack();
        int damage = stack.getItemDamage();
        int maximum = stack.getMaxDamage();
        if (maximum < 1) return 0.0F;
        if ((float) damage <= -1.0F) return 1.0F;
        float remaining = maximum - damage;
        if (remaining <= 0.0F) return 1.0F;
        return remaining / maximum;
    }

    public static List<ItemStack[]> armorLines(EntityLivingBase living) {
        List<ItemStack[]> rows = new ArrayList<>();
        var autoHypixel = com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule.INSTANCE;
        if (autoHypixel.isEnabled() && !living.isUser()
                && com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTracker.inGame()) {
            var player = autoHypixel.bedWarsTracker.getPlayer(living.getName());
            if (player != null) {
                List<ItemStack> equipment = new ArrayList<>();
                equipment.add(entityOf(living));
                java.util.Collections.addAll(equipment, player.renderItems);
                rows.add(equipment.toArray(new ItemStack[0]));
                List<ItemStack> recent = new ArrayList<>();
                for (var item : player.recentItems) if (item != null) recent.add(item.stack);
                rows.add(recent.toArray(new ItemStack[0]));
                return rows;
            }
        }
        rows.add(new ItemStack[] { entityOf(living), living.getCurrentArmor(3), living.getCurrentArmor(2),
                living.getCurrentArmor(1), living.getCurrentArmor(0) });
        return rows;
    }

    public void createIconList(int alpha) {
        if (!(target instanceof TrackedLivingEntity) || target.removed) return;
        var settings = (com.adjustclient.module.impl.render.esp.settings.LivingEspSettings) modeSetting;
        Color color = settings.outlineColor.getColor();
        int argb = (Math.min(color.getAlpha(), alpha) << 24) | (color.getRGB() & 0xFFFFFF);
        boxSetting.getItemBucket(argb, (EntityLivingBase) target.getEntity());
    }

    public void drawIcon(int alpha, float center, float y) {
        GlStateManager.enableBlend();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha / 255.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(warningTexture);
        net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture(
                center * 2.0F - 8.0F, (y - 8.0F) * 2.0F,
                0.0F, 0.0F, 16.0F, 16.0F, 16.0F, 16.0F);
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
    }

    public void textY(com.adjustclient.event.Render2DEvent event, Vector4d bounds, ScaledResolution resolution) {
        float left = (float) bounds.x;
        float top = (float) bounds.y;
        float right = (float) bounds.z;
        float bottom = (float) bounds.w;
        float center = left + (right - left) * 0.5F;
        int alpha = 255;
        if (target.isRemoved()) {
            int duration = target.seenInFrustum ? (int) (target.retentionMillis / 2.5F) : target.retentionMillis;
            long elapsed = System.currentTimeMillis() - target.removalTimer.lastMs;
            if (elapsed >= duration - 1000L) {
                long remaining = duration - elapsed;
                alpha = remaining >= 30L ? (int) (remaining / 1000.0F * 255.0F) : 0;
            }
        }
        alpha = Math.min(alpha, modeSetting.maxAlpha.getInt());
        if (alpha < 10) return;
        if (modeSetting.useFov.isEnabled()) {
            if (healthSetting != null) {
                alpha -= net.minecraft.util.MathHelper.clamp_int((int) (255.0F * healthSetting), 0, 255);
            } else {
                float dx = resolution.getScaledWidth() * 0.5F - center;
                float dy = resolution.getScaledHeight() * 0.5F - (top + (bottom - top) * 0.5F);
                double distance = Math.min(Math.sqrt(dx * dx + dy * dy) * resolution.getScaleFactor_double(),
                        resolution.getScaledWidth());
                alpha -= (int) (distance / resolution.getScaledWidth() * alpha);
            }
            if (alpha < 25) return;
        }
        float above = 2.0F;
        if (displayName != null && modeSetting.nametagBind.getValue().isHeld())
            above += healthColor(bounds, modeSetting, alpha, event, top - above, center);
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.MobEspSettings mob) {
            int iconAlpha = alpha;
            if (mob.fovAlpha.isEnabled()) {
                float dx = resolution.getScaledWidth() * 0.5F - center;
                float width = resolution.getScaledWidth() * 0.30000001F;
                float distance = (float) Math.min(Math.abs(dx) * resolution.getScaleFactor_double(), width);
                iconAlpha -= (int) (distance / width * iconAlpha);
            }
            if (iconAlpha >= 25) {
                if (equipmentIcons != null && !equipmentIcons.isEmpty())
                    above += drawEquipment(iconAlpha, mob, center, top - above);
                if (potionIcons != null) above += barX(event, iconAlpha, null, center, top - above);
                if (hacker && mob instanceof com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings player
                        && player.playerHackingLocation.getValue() == com.adjustclient.module.impl.render.esp.settings.EspIconLocation.ICON)
                    drawIcon(iconAlpha, center, top - above);
            }
            if (flagLines != null) renderFlags(event, mob, alpha, right, top);
        }
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.BorderedEspSettings)
            drawText(bounds, modeSetting, alpha, left, top, right, bottom);
        float below = 2.0F;
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.MobEspSettings mob && mob.armorEnabled.isEnabled()
                || modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.ItemEspSettings item && item.durabilityEnabled.isEnabled()
                || modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.TntEspSettings tnt && tnt.countdownEnabled.isEnabled())
            below += drawDistance(bounds, modeSetting, alpha, left, top, bottom);
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.LivingEspSettings living) {
            if (living.healthBarEnabled.isEnabled()) renderItemIcons(living, alpha, left, top, bottom);
            if (heldItemName != null) renderNametag(bounds, event, living, alpha, bottom + below, center);
            if (living.outlineEnabled.isEnabled()) createIconList(alpha);
        }
    }

    public void textX(int alpha, Object ignored, float center, float width, float y) {
        new Color(233, 235, 234, alpha);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha / 255.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float advance = 0.0F;
        for (EspPotionEntry potion : potionIcons) {
            float x = (center - width + advance) * 2.0F;
            float top = (y - 8.0F) * 2.0F;
            Minecraft.getMinecraft().getTextureManager().bindTexture(net.minecraft.client.gui.inventory.GuiContainer.inventoryBackground);
            RenderUtil.drawPotionIcon(18 * (potion.iconIndex % 8), 18 * (potion.iconIndex / 8) + 198, x, top);
            if (potion.text != null) Minecraft.getMinecraft().fontRendererObj.drawString(potion.text,
                    x + 17.0F - potion.width, top + 9.0F, alpha << 24 | 0xE9EBEA, true);
            advance += 9.5F;
        }
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public float barX(com.adjustclient.event.Render2DEvent event, int alpha, Object ignored, float x, float y) {
        if (potionWidth == 0.0F) return 0.0F;
        float width = potionWidth;
        event.postTasks.add(() -> {
            textX(alpha, null, x, width, y);
        });
        return 9.5F;
    }

    public void drawHealthBar(com.adjustclient.render.font.TextRenderer font,
            com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings settings, int alpha, Object ignored,
            float x, float y) {
        for (int index = 0; index < flagLines.size(); index++) {
            var flag = flagLines.get(index);
            Color color = settings.flagGroups.get(flag).color.getColor();
            int argb = alpha << 24 | color.getRGB() & 0xFFFFFF;
            font.drawString(flag.label, x + 2.0F, y + 4.0F * index, argb, 0.5F);
        }
    }

    public float healthColor(Object bounds, Object settingsObject, Object alphaObject, Object eventObject,
                             float bottom, float center) {
        var settings = (com.adjustclient.module.impl.render.esp.settings.EntityEspSettings) settingsObject;
        var event = (com.adjustclient.event.Render2DEvent) eventObject;
        int alpha = (Integer) alphaObject;
        boolean background = settings.background.isEnabled();
        var layout = background ? settings.rectangles.getValue()
                : com.adjustclient.module.impl.render.esp.settings.EspRectangleLayout.TOGETHER;
        boolean separated = layout == com.adjustclient.module.impl.render.esp.settings.EspRectangleLayout.SEPARATED;
        boolean healthColored = settings instanceof com.adjustclient.module.impl.render.esp.settings.LivingEspSettings living
                && living.nametagHealthColored.isEnabled();
        boolean warning = hacker && settings instanceof com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings player
                && player.playerHackingLocation.getValue() == com.adjustclient.module.impl.render.esp.settings.EspIconLocation.NAMETAG;
        float iconWidth = textScale * 10.0F + 1.5F;
        float width = displayNameWidth + (warning ? iconWidth : 0.0F);
        if (effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.ADJUST) alpha = Math.min(255, alpha + 30);
        float extraHealth = field96 + distanceTextWidth;
        float distanceAdvance = distanceText == null ? 0.0F : statusTextWidth + 2.0F;
        float left;
        float distanceX = 0.0F;
        if (!separated) {
            width += distanceAdvance;
            if (healthText != null) width += extraHealth + 2.0F;
            left = center - width * 0.5F - 1.5F;
        } else {
            left = center - width * 0.5F - 1.5F;
            if (healthText != null) left -= extraHealth * 0.5F + 2.0F;
            if (distanceText != null) left += statusTextWidth * 0.5F + 2.0F;
        }
        float adjustX = 0.0F;
        if (adjustUserName != null) left -= healthTextWidth * 0.5F + 2.0F;
        float nameX = left + 1.5F + (separated ? 0.0F : distanceAdvance);
        if (warning) nameX += iconWidth;
        float fontHeight = nameSetting instanceof com.adjustclient.render.font.NvgFontRenderer nvg
                ? nvg.size : ((net.minecraft.client.gui.FontRenderer) nameSetting).FONT_HEIGHT;
        float top = effectiveTheme.pixel ? bottom - fontHeight * textScale - 2.0F
                : bottom - (fontHeight - 0.5F) - 2.0F;
        int backgroundColor = new Color(0, 0, 0, Math.min(alpha, 145)).getRGB();
        if (background) fillRect(backgroundColor, left, top, left + width + 3.0F, bottom);
        float nameRight = nameX + displayNameWidth;
        float healthX = nameRight;
        float labelRight = background ? left + width + 3.0F : nameRight + extraHealth;
        if (separated) {
            labelRight = left + width + 3.0F;
            healthX = 0.0F;
            if (healthText != null) {
                healthX = labelRight + 1.0F;
                labelRight = healthX + extraHealth + 3.0F;
                fillRect(healthX, top, labelRight, bottom);
            }
            if (distanceText != null) {
                distanceX = left - 1.0F - statusTextWidth - 3.0F;
                fillRect(distanceX, top, distanceX + statusTextWidth + 3.0F, bottom);
            }
        } else {
            distanceX = left;
        }
        if (adjustUserName != null) {
            adjustX = labelRight + 1.0F;
            if (background) fillRect(adjustX, top, adjustX + healthTextWidth + 3.0F, bottom);
        }
        if (effectiveTheme.pixel && field28 != null) {
            alpha = (int) (alpha * field28);
            if (alpha < 25) return 8.0F;
        }
        if (warning) {
            var icons = com.adjustclient.render.font.Fonts.icons(textScale * 20.0F);
            icons.drawStringNoShadow("g", nameX - (textScale * 10.0F - 2.0F) - 3.0F,
                    top + 2.5F, new Color(250, 175, 5, alpha).getRGB());
        }
        float labelTop = top;
        float labelNameX = nameX;
        float labelHealthX = healthX;
        float labelDistanceX = distanceX;
        float labelAdjustX = adjustX;
        int labelAlpha = alpha;
        String labelName = displayName;
        float labelScale = textScale;
        boolean hasAdjustName = adjustUserName != null;
        boolean hasHealth = healthText != null;
        boolean hasDistance = distanceText != null;
        Runnable draw = () -> {
            buildRenderLines(labelName, labelTop, labelNameX, labelScale, labelAdjustX,
                    labelHealthX, labelDistanceX, labelAlpha, hasAdjustName,
                    hasHealth, healthColored, hasDistance);
        };
        if (effectiveTheme.pixel) event.postTasks.add(() -> {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            draw.run();
        });
        else draw.run();
        return 8.0F;
    }

    public void renderNametag(Object bounds, com.adjustclient.event.Render2DEvent event,
            com.adjustclient.module.impl.render.esp.settings.LivingEspSettings settings, int alpha,
            float y, float center) {
        String text = heldItemName;
        float half = nameWidth * 0.5F;
        if (settings.background.isEnabled()) {
            float fontHeight = itemNameFont instanceof com.adjustclient.render.font.NvgFontRenderer nvg
                    ? nvg.size : ((net.minecraft.client.gui.FontRenderer) itemNameFont).FONT_HEIGHT;
            float bottom = y + (effectiveTheme.pixel ? fontHeight * halfWidth : fontHeight - 0.5F) + 2.0F;
            com.adjustclient.render.font.image.NvgDraw.fillRect(new Color(0, 0, 0, Math.min(145, alpha)).getRGB(),
                    center - half - 1.5F, y, center + half + 1.5F, bottom);
        }
        int drawAlpha = effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.ADJUST
                ? Math.min(255, alpha + 20) : alpha;
        Runnable draw = () -> {
            float top = y;
            if (settings.background.isEnabled()) top += 1.5F;
            if (effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.ADJUST) top += 1.0F;
            new Color(233, 235, 234, drawAlpha);
            int color = drawAlpha << 24 | 0xE9EBEA;
            if (itemNameFont instanceof com.adjustclient.render.font.NvgFontRenderer nvg)
                nvg.drawString(text, center - half, top, color);
            else itemNameFont.drawString(text, center - half, top, color, halfWidth);
        };
        if (effectiveTheme.pixel) event.postTasks.add(() -> {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            draw.run();
        });
        else draw.run();
    }

    public void renderFlags(com.adjustclient.event.Render2DEvent event,
            com.adjustclient.module.impl.render.esp.settings.MobEspSettings settings, int alpha, float x, float y) {
        var player = (com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings) settings;
        com.adjustclient.render.font.TextRenderer font;
        if (effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.MINECRAFT)
            font = (com.adjustclient.render.font.TextRenderer) Minecraft.getMinecraft().fontRendererObj;
        else if (effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.PIXEL)
            font = (com.adjustclient.render.font.TextRenderer) Minecraft.getMinecraft().pixelFontRenderer;
        else {
            font = com.adjustclient.render.font.Fonts.TAHOMA_8;
            y += 1.0F;
            alpha = Math.min(alpha + 30, 255);
        }
        float drawY = y;
        int drawAlpha = alpha;
        Runnable draw = () -> {
            for (int index = 0; index < flagLines.size(); index++) {
                var flag = flagLines.get(index);
                Color color = player.flagGroups.get(flag).color.getColor();
                int argb = drawAlpha << 24 | color.getRGB() & 0xFFFFFF;
                font.drawString(flag.label, x + 2.0F, drawY + 4.0F * index, argb, 0.5F);
            }
        };
        if (effectiveTheme.pixel) {
            event.postTasks.add(() -> {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                draw.run();
            });
        } else {
            drawHealthBar(font, player, alpha, null, x, y);
        }
    }

    public void renderItemIcons(com.adjustclient.module.impl.render.esp.settings.LivingEspSettings settings,
            int alpha, float x, float top, float bottom) {
        if (healthHue <= 0.0F) return;
        float thickness = settings.healthThinner.isEnabled() ? 2.5F : 3.0F;
        float outer = x - thickness;
        float inner = x - 2.0F;
        float height = bottom - top;
        float healthTop = bottom - healthHue * height;
        int background = new Color(10, 10, 10, Math.max(0, alpha - 60)).getRGB();
        com.adjustclient.render.font.image.NvgDraw.fillRect(background, inner + 0.5F, top - 1.0F, outer - 0.5F, bottom + 1.0F);
        boolean custom = settings.customColorEnabled.isEnabled();
        int color = alpha << 24 | (target.isRemoved() ? 0xFFFFFF
                : custom ? settings.getHealthColor() & 0xFFFFFF : barColor & 0xFFFFFF);
        if (healthRatio > 0.0F) {
            float split = bottom - healthRatio * height;
            int absorption = alpha << 24 | (target.isRemoved() ? 0xAAAAAA
                    : custom ? com.adjustclient.render.ColorUtil.brighter(color, 0.80000001F) & 0xFFFFFF : 0xD7D74B);
            com.adjustclient.render.font.image.NvgDraw.fillRect(color, inner, split - 0.5F, outer, healthTop - 0.5F);
            com.adjustclient.render.font.image.NvgDraw.fillRect(absorption, inner, bottom + 0.5F, outer, split - 0.5F);
        } else {
            com.adjustclient.render.font.image.NvgDraw.fillRect(color, inner, bottom + 0.5F, outer, healthTop - 0.5F);
        }
    }

    public void renderTnt(com.adjustclient.module.impl.render.esp.settings.EspTargetSettings settings,
            int alpha, String text, float center, float halfWidth, float y) {
        float left = center - halfWidth;
        if (settings.background.isEnabled()) y += 1.5F;
        if (effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.ADJUST) y += 1.0F;
        new Color(233, 235, 234, alpha);
        int color = alpha << 24 | 0xE9EBEA;
        if (itemNameFont instanceof com.adjustclient.render.font.NvgFontRenderer nvg) {
            nvg.drawString(text, left, y, color);
        } else {
            var font = (net.minecraft.client.gui.FontRenderer) itemNameFont;
            GlStateManager.enableAlpha();
            font.randomStyle = false;
            font.boldStyle = false;
            font.italicStyle = false;
            font.underlineStyle = false;
            font.strikethroughStyle = false;
            font.renderString(text, color, true, left + this.halfWidth, y + this.halfWidth, this.halfWidth);
            font.renderString(text, color, false, left, y, this.halfWidth);
        }
    }

    public void drawText(Object bounds, Object settingsObject, Object alphaObject,
                         float left, float top, float right, float bottom) {
        var settings = (com.adjustclient.module.impl.render.esp.settings.BorderedEspSettings) settingsObject;
        int alpha = (Integer) alphaObject;
        new Color(0, 0, 0, alpha);
        int rgb;
        if (target.isRemoved()) rgb = 0xB4B4B4;
        else if (settings instanceof com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings player
                && player.borderTeamColor.isEnabled()) {
            Integer teamColor = com.adjustclient.util.WorldUtil.getTeamColor(target.getEntity().getName());
            rgb = teamColor == null ? 0xFFFFFF : teamColor;
        } else rgb = settings.borderColor.getColor().getRGB();
        int color = alpha << 24 | rgb & 0xFFFFFF;
        int black = alpha << 24;
        switch (settings.borderMode.getValue()) {
            case DISABLED -> { }
            case FULL -> {
                com.adjustclient.render.font.image.NvgDraw.strokeRect(false, black, left - 1.0F, top - 1.0F, right + 1.0F, bottom + 1.0F, 1.5F);
                com.adjustclient.render.font.image.NvgDraw.strokeRect(false, color, left - 0.5F, top - 0.5F, right + 0.5F, bottom + 0.5F, 0.5F);
            }
            case CORNERED -> {
                float length = -(left - right) / 3.5F;
                float x0 = left - 0.5F;
                float y0 = top - 0.5F;
                float x1 = x0 + length;
                fillRect(color, x0, y0, x1, top);
                float t0 = top + 0.5F;
                float l0 = x0 + 0.5F;
                float t1 = length + t0;
                float t2 = t0 - 0.5F;
                fillRect(x0, t2, l0, t1);
                float t3 = y0 - 0.5F;
                float l1 = left + length;
                fillRect(black, x0, t3, l1, y0);
                float l2 = left + 0.5F;
                float l3 = l2 - 0.5F;
                fillRect(l3, t2, l2, t1);
                fillRect(left, t2, l1, t0);
                fillRect(x1, y0, l1 + 0.5F - 0.5F, top);
                float l4 = x0 - 0.5F;
                fillRect(l4, t3, x0, t1);
                fillRect(l4, t2 + length + 0.5F, x0 + 1.0F, t1 + 0.5F);
                float r0 = right - length;
                fillRect(color, r0, y0, right, top);
                float r1 = top + length;
                float r2 = right + 0.5F;
                fillRect(right, y0, r2, r1);
                fillRect(black, r0, t3, r2, y0);
                float r3 = right - 0.5F;
                fillRect(r3, t2, right, r1);
                fillRect(r0, t2, r3, t0);
                fillRect(r0 - 0.5F, t3, r0 - 0.5F + 0.5F, y0 + 1.0F);
                float r4 = right + 1.0F;
                fillRect(r4 - 0.5F, t3, r4, r1);
                fillRect(r3, t1 - 0.5F, r4, t1 - 0.5F + 0.5F);
                float b0 = bottom + 0.5F - 0.5F;
                float b1 = bottom + 0.5F;
                fillRect(color, x0, b0, x1, b1);
                float b2 = b0 - length;
                fillRect(x0, b2, l0, b1);
                float b3 = bottom + 1.0F;
                fillRect(black, x0, b3 - 0.5F, x1, b3);
                fillRect(l3, b2, l2, bottom);
                float b4 = bottom - 0.5F;
                fillRect(l2, b4, x1, bottom);
                float b5 = x1 + 0.5F;
                float b6 = b1 + 0.5F;
                fillRect(x1, b4, b5, b6);
                fillRect(l4, b2, x0, b6);
                float b7 = bottom - length - 0.5F;
                float b8 = b7 + 0.5F;
                float b9 = bottom - length;
                fillRect(l4, b7, left + 1.0F - 0.5F, b8);
                fillRect(color, r0, b0, right, b1);
                fillRect(right, b2, r2, b1);
                fillRect(black, r0, b6, r2, b1);
                fillRect(r3, b9, right, b4);
                fillRect(r0, b4, right, bottom);
                fillRect(r3 - length, b4, r3 - length + 0.5F, b6);
                fillRect(r3 + 1.0F, b4 - length + 0.5F, r4, b3);
                fillRect(r3, b7, r3 + 1.5F, b8);
            }
        }
    }

    public float drawDistance(Vector4d bounds, Object settings, int alpha,
                              float left, float top, float bottom) {
        if (barRatio <= 0.0F) return 0.0F;
        int selected;
        boolean thinner;
        com.adjustclient.module.impl.render.esp.settings.EspBarPosition position;
        if (settings instanceof com.adjustclient.module.impl.render.esp.settings.MobEspSettings mob) {
            selected = mob.getArmorColor();
            thinner = mob.armorThinner.isEnabled();
            position = mob.getArmorPosition();
        } else if (settings instanceof com.adjustclient.module.impl.render.esp.settings.ItemEspSettings item) {
            selected = item.getDurabilityColor();
            thinner = item.durabilityThinner.isEnabled();
            position = com.adjustclient.module.impl.render.esp.settings.EspBarPosition.BOTTOM;
        } else {
            var tnt = (com.adjustclient.module.impl.render.esp.settings.TntEspSettings) settings;
            selected = tnt.getCountdownColor();
            thinner = tnt.countdownThinner.isEnabled();
            position = tnt.countdownPosition.getValue();
        }
        int color = alpha << 24 | (target.isRemoved() ? 0xFFFFFF : selected & 0xFFFFFF);
        int background = new Color(10, 10, 10, Math.max(0, alpha - 60)).getRGB();
        float thickness = thinner ? 2.5F : 3.0F;
        float right = (float) bounds.z;
        if (position == com.adjustclient.module.impl.render.esp.settings.EspBarPosition.RIGHT) {
            float filledTop = bottom - (bottom - top) * barRatio;
            fillRect(background, right + 1.5F, top - 1.0F, right + thickness + 0.5F, bottom + 1.0F);
            fillRect(color, right + 2.0F, bottom + 0.5F, right + thickness, filledTop - 0.5F);
            return 0.0F;
        }
        float filledRight = left + (right - left) * barRatio;
        fillRect(background, left - 1.0F, bottom + 1.5F, right + 1.0F, bottom + thickness + 0.5F);
        fillRect(color, left - 0.5F, bottom + 2.0F, filledRight + 0.5F, bottom + thickness);
        return thinner ? 2.0F : 2.5F;
    }

    public void buildRenderLines(String name, float top, float nameX, float scale,
            float adjustX, float healthX, float distanceX, int alpha,
            boolean hasAdjustName, boolean hasHealth, boolean healthColored, boolean hasDistance) {
        float vertical = effectiveTheme.pixel ? 1.5F : Math.max(0.0F, 2.5F - (textScale - 0.5F));
        float y = top + vertical;
        int prefix = alpha << 24;
        int nameArgb = prefix | nameColor & 0xFFFFFF;
        if (nameSetting instanceof com.adjustclient.render.font.NvgFontRenderer nvg) {
            nvg.drawString(name, nameX, y, nameArgb);
        } else {
            var font = (net.minecraft.client.gui.FontRenderer) nameSetting;
            GlStateManager.enableAlpha();
            font.randomStyle = false;
            font.boldStyle = false;
            font.italicStyle = false;
            font.underlineStyle = false;
            font.strikethroughStyle = false;
            font.renderString(name, nameArgb, true, nameX + scale, y + scale, scale);
            font.renderString(name, nameArgb, false, nameX, y, scale);
        }
        if (hasAdjustName) {
            nameSetting.drawString(adjustUserName, adjustX + 1.5F, y, prefix | healthColor & 0xFFFFFF, scale);
        }
        if (hasHealth) {
            int color = healthColored ? prefix | barColor & 0xFFFFFF : prefix | 0xE9EBEA;
            float nextX = nameSetting.drawString(healthText, healthX + 1.5F, y, color, scale);
            if (absorptionText != null) {
                int absorptionColor = prefix | (target.isRemoved() ? 0xAAAAAA : 0xFFAA00);
                nameSetting.drawString(absorptionText, nextX + 1.5F, y, absorptionColor, scale);
            }
        }
        if (hasDistance) nameSetting.drawString(distanceText, distanceX + 1.5F, y, prefix | 0xAAAAAA, scale);
    }

    public static ItemStack entityOf(EntityLivingBase entity) {
        if (entity.isUser() && com.adjustclient.Adjust.slotSpoofer != null && com.adjustclient.Adjust.slotSpoofer.field17)
            return com.adjustclient.Adjust.slotSpoofer.getRenderItemStack();
        return entity.getHeldItem();
    }

    public float drawEquipment(Object alphaObject, Object ignored, float center, float y) {
        int alpha = (Integer) alphaObject;
        float height = (equipmentIcons.size() - 1) * 10.0F + 8.0F;
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        float rowY = 0.0F;
        for (EspIconEntry row : equipmentIcons) {
            float advance = 0.0F;
            for (ItemStack stack : (ItemStack[]) row.icon) {
                if (stack != null) {
                    if (stack.getItem() != null) {
                        RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
                        renderer.zLevel -= 300.0F;
                        renderer.renderItemIntoGUI(stack, alpha, true,
                                (center - row.advance + advance) * 2.0F, (y - height + rowY) * 2.0F);
                        renderer.zLevel += 300.0F;
                    }
                    advance += 8.0F;
                }
            }
            rowY += 10.0F;
        }
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        return height + 2.0F;
    }

    public void renderArmor() {
        equipmentIcons = new ArrayList<>();
        for (ItemStack[] row : armorLines((EntityLivingBase) target.getEntity())) {
            float width = 0.0F;
            for (ItemStack stack : row) if (stack != null) width += 4.0F;
            if (width > 0.0F) {
                EspIconEntry icon = new EspIconEntry();
                icon.icon = row;
                icon.advance = width;
                equipmentIcons.add(icon);
            }
        }
    }

    public void collectEquipment() {
        EntityLivingBase living = (EntityLivingBase) target.getEntity();
        if (living.getActivePotionEffects().isEmpty()) return;
        potionIcons = new ArrayList<>();
        for (net.minecraft.potion.PotionEffect effect : living.getActivePotionEffects()) {
            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            int icon = potion.getStatusIconIndex();
            if (icon < 0) continue;
            EspPotionEntry line = new EspPotionEntry();
            line.iconIndex = icon;
            if (effect.getAmplifier() != 0) {
                line.text = com.adjustclient.util.StringUtil.toRoman(effect.getAmplifier() + 1);
                line.width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line.text);
            }
            potionIcons.add(line);
        }
        potionWidth = potionIcons.size() * 4.75F;
    }

    public void maxHealth() {
        if (!(target instanceof TrackedLivingEntity livingTarget)) return;
        EntityLivingBase living = livingTarget.getEntity();
        float maximum = living.getMaxHealth();
        healthRatio = Math.min(maximum, living.getAbsorptionAmount()) / maximum;
        int armor = living.getTotalArmorValue();
        if (living instanceof net.minecraft.entity.monster.EntityZombie) armor = Math.min(20, Math.max(0, armor - 2));
        barRatio = armor / 20.0F;
        drawBox(livingTarget);
    }

    public void health() {
        if (target instanceof TrackedLivingEntity livingTarget) {
            EntityLivingBase living = livingTarget.getEntity();
            int armor = living.getTotalArmorValue();
            if (living instanceof net.minecraft.entity.monster.EntityZombie) armor = Math.min(20, Math.max(0, armor - 2));
            barRatio = armor / 20.0F;
        } else if (target instanceof com.adjustclient.util.anticheat.entity.TrackedTnt) {
            barRatio = ((EntityTNTPrimed) target.getEntity()).fuse / 80.0F;
        } else if (target instanceof com.adjustclient.util.anticheat.entity.TrackedItem item) {
            barRatio = healthFraction(item);
        }
    }

    public void render() {
        textScale = modeSetting.getNametagScale() * 0.5F;
        nameSetting = fontForScale(textScale);
        String name = modeSetting.getName(target);
        displayName = String.valueOf(name) + buildItemLines(target);
        if (modeSetting.boldText.isEnabled()) displayName = net.minecraft.util.ChatColor.BOLD + displayName;
        displayNameWidth = nameSetting.getStringWidth(displayName, textScale);
        nameColor = modeSetting.nametagColor.getColor().getRGB();
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings player
                && player.teamColor.isEnabled() && !target.isRemoved()) {
            Integer teamColor = com.adjustclient.util.WorldUtil.getTeamColor(target.getEntity().getName());
            if (teamColor != null) nameColor = teamColor;
        }
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.PlayerEspSettings player
                && player.adjustName.isEnabled()) {
            Entity entity = target.getEntity();
            if (entity.isUser()) {
                var users = com.adjustclient.Adjust.INSTANCE != null
                        ? (com.adjustclient.property.UserManager) com.adjustclient.Adjust.INSTANCE.field40 : null;
                var user = users != null ? (com.adjustclient.user.ClientUser) users.field16 : null;
                if (user != null) {
                    adjustUserName = net.minecraft.util.ChatColor.BOLD.toString() + user.name;
                    healthColor = com.adjustclient.module.impl.render.hud.HUDModule.getHudColor().getRGB();
                }
            } else if (com.adjustclient.Adjust.INSTANCE != null
                    && com.adjustclient.Adjust.INSTANCE.screenStack != null
                    && com.adjustclient.Adjust.INSTANCE.screenStack.serverTracker != null) {
                var user = com.adjustclient.Adjust.INSTANCE.screenStack.serverTracker.findUser(entity.getName());
                if (user != null) {
                    adjustUserName = net.minecraft.util.ChatColor.BOLD.toString() + user.name;
                    healthColor = user.getColor();
                }
            }
            if (adjustUserName != null) healthTextWidth = nameSetting.getStringWidth(adjustUserName, textScale);
        }
        if (target.isRemoved()) nameColor = -1;
        if (target instanceof com.adjustclient.util.anticheat.entity.TrackedItem item) {
            Color itemColor = com.adjustclient.module.impl.render.esp.ModernItemName.lookup(item.getItemStack());
            if (itemColor != null) nameColor = itemColor.getRGB();
        }
        if (modeSetting instanceof com.adjustclient.module.impl.render.esp.settings.LivingEspSettings livingSettings
                && livingSettings.nametagHealthEnabled.isEnabled() && target instanceof TrackedLivingEntity livingTarget) {
            EntityLivingBase entity = livingTarget.getEntity();
            var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE != null
                    ? com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState : null;
            var game = com.adjustclient.Adjust.gameStateTracker != null
                    ? com.adjustclient.Adjust.gameStateTracker.state : null;
            boolean suppressHealth = server != null && server.antiCheatModel != null
                    && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL
                    && game != null && Boolean.FALSE.equals(game.lobby) && game.startTimer == null;
            if (!suppressHealth && entity.getHealth() > 0.0F) {
                String value = com.adjustclient.util.RandomUtil.format(entity.getHealth());
                healthText = effectiveTheme.pixel
                        ? (target.removed ? "" : net.minecraft.util.ChatColor.RED.toString()) + value + " ❤"
                        : net.minecraft.util.ChatColor.BOLD + value;
                if (livingSettings.nametagHealthColored.isEnabled()
                        || effectiveTheme == com.adjustclient.module.impl.render.esp.settings.EspTheme.ADJUST) drawBox(livingTarget);
                field96 = nameSetting.getStringWidth(healthText, textScale);
                if (entity.getAbsorptionAmount() > 0.0F) {
                    String absorption = com.adjustclient.util.RandomUtil.format(entity.getAbsorptionAmount());
                    absorptionText = effectiveTheme.pixel ? absorption + " ❤" : net.minecraft.util.ChatColor.BOLD + absorption;
                    distanceTextWidth = nameSetting.getStringWidth(absorptionText, textScale) + 2.0F;
                }
            }
        }
        if (modeSetting.distance.isEnabled()) {
            var player = Minecraft.getMinecraft().thePlayer;
            int distance = player == null ? 0 : Math.round(player.getDistanceToEntity(target.getEntity()));
            distanceText = distance + "m";
            if (effectiveTheme.pixel) distanceText = "[" + distanceText + "]";
            statusTextWidth = nameSetting.getStringWidth(distanceText, textScale);
        }
    }

}
