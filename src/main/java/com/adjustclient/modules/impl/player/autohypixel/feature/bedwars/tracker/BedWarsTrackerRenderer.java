package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker;

import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.item.TrackedItem;
import com.adjustclient.modules.impl.render.hud.playerlist.stats.BedWarsTeam;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class BedWarsTrackerRenderer {
    public static void draw(TeamRow row, Render2DEvent event, float x, float y) {
        Minecraft mc = Minecraft.getMinecraft();
        event.postTasks.add(() -> prepareItemRender(row.icon, mc, x, y));
        float textX = x + 2.0F + 18.0F;
        Fonts.TAHOMA_12.drawString(row.team.name + " Team", textX, y + 3.5F,
                mc.fontRendererObj.getColorCode(row.team.chatColor.getFormattingCode()) & 0xFFFFFF | 0xBE000000);
        Fonts.TAHOMA_10.drawString(row.info, textX, y + 10.5F, 0xBED5D7D6);

        float rowY = y + 20.0F;
        List<Runnable> textTasks = new ArrayList<>();
        for (PlayerRow member : row.players) {
            float curRowY = rowY;
            event.postTasks.add(() -> {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glScalef(0.2F, 0.2F, 0.2F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                RenderUtil.drawHead(member.skin, (x + 2.0F) * 5.0F, (curRowY - 2.5F) * 5.0F);
                GL11.glScalef(5.0F, 5.0F, 5.0F);
            });
            float itemX = Fonts.TAHOMA_10.drawString(member.name, x + 10.0F, curRowY, 0xBEE9EBEA) + 1.5F;
            float itemY = (curRowY - 3.5F) * 2.0F;
            if (member.renderItems != null) {
                for (ItemStack stack : member.renderItems) {
                    if (stack == null) continue;
                    float itemPos = itemX;
                    event.postTasks.add(() -> {
                        GL11.glPushMatrix();
                        GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
                        RenderHelper.enableStandardItemLighting();
                        GL11.glPopMatrix();
                        GL11.glScalef(0.5F, 0.5F, 0.5F);
                        mc.getRenderItem().renderItemIntoGUI(stack, 255, true, itemPos * 2.0F, itemY);
                        GL11.glScalef(2.0F, 2.0F, 2.0F);
                        RenderHelper.disableStandardItemLighting();
                    });
                    itemX += 8.0F;
                }
            }
            if (member.badges != null) {
                for (Badge badge : member.badges) {
                    if (badge == null) continue;
                    float itemPos = itemX;
                    event.postTasks.add(() -> {
                        GL11.glPushMatrix();
                        GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
                        RenderHelper.enableStandardItemLighting();
                        GL11.glPopMatrix();
                        GL11.glScalef(0.5F, 0.5F, 0.5F);
                        mc.getRenderItem().renderItemIntoGUI(badge.item.stack, itemPos * 2.0F, itemY);
                        GL11.glScalef(2.0F, 2.0F, 2.0F);
                        RenderHelper.disableStandardItemLighting();
                    });
                    if (badge.ageText != null) {
                        textTasks.add(() -> Fonts.TAHOMA_8.drawString(badge.ageText, itemPos, (itemY + 3.5F) * 0.5F, 0xBEE9EBEA));
                        itemX += Math.max(8.0F, badge.textWidth + 1.0F);
                    } else {
                        itemX += 8.0F;
                    }
                }
            }
            rowY += 8.5F;
        }

        if (!event.postTasks.isEmpty()) {
            NvgContext.flushFrame();
            while (!event.postTasks.isEmpty()) {
                event.postTasks.remove(0).run();
            }
        }
        while (!textTasks.isEmpty()) {
            textTasks.remove(0).run();
        }
    }

    public static void prepareItemRender(ItemStack stack, Minecraft mc, float x, float y) {
        GlStateManager.disableLighting();
        GL11.glPushMatrix();
        GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        mc.getRenderItem().renderItemIntoGUI(stack, x + 2.0F, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static final class TeamRow {
        public final ItemStack icon;
        public final BedWarsTeam team;
        public final List<PlayerRow> players;
        public final String info;
        public final float width;

        public TeamRow(BedWarsTeam team, TeamUpgradeState upgrades, List<PlayerRow> players) {
            this.icon = new ItemStack(Item.getItemFromBlock(Blocks.wool), 1, Math.max(0, team.dyeColor.getMetadata()));
            this.team = team;
            this.players = players;
            StringBuilder sb = new StringBuilder();
            if (upgrades.sharpenedSwords) {
                sb.append("Sharpness");
            }
            if (upgrades.reinforcedArmor) {
                if (sb.length() > 0) sb.append(", ");
                sb.append("Protection");
            }
            this.info = sb.length() == 0 ? "No upgrades" : sb.toString();

            float w = Fonts.TAHOMA_10.getStringWidth(this.info) + 20.0F;
            for (PlayerRow member : players) {
                float pw = Fonts.TAHOMA_10.getStringWidth(member.name) + 10.0F;
                if (member.renderItems != null) {
                    for (ItemStack stack : member.renderItems) {
                        if (stack != null) pw += 8.0F;
                    }
                }
                if (member.badges != null) {
                    for (Badge badge : member.badges) {
                        if (badge == null) continue;
                        if (badge.ageText != null) {
                            pw += Math.max(8.0F, badge.textWidth + 4.0F);
                        } else {
                            pw += 8.0F;
                        }
                    }
                }
                if (pw > w) w = pw;
            }
            this.width = w;
        }
    }

    public static final class PlayerRow {
        public final String name;
        public final ResourceLocation skin;
        public final ItemStack[] renderItems;
        public final Badge[] badges;

        public PlayerRow(TrackedPlayer tracked) {
            this.name = tracked.player.info.getGameProfile().getName();
            this.skin = tracked.player.info.getLocationSkin();
            this.renderItems = tracked.renderItems;
            TrackedItem[] recent = tracked.recentItems;
            this.badges = new Badge[recent.length];
            for (int idx = 0; idx < recent.length; idx++) {
                TrackedItem item = recent[idx];
                if (item != null) {
                    this.badges[idx] = new Badge(item);
                }
            }
        }
    }

    public static final class Badge {
        public final TrackedItem item;
        public final String ageText;
        public final float textWidth;

        public Badge(TrackedItem item) {
            this.item = item;
            if (item.superseded) {
                this.ageText = formatAge(System.currentTimeMillis() - item.timer.lastMs);
                this.textWidth = Fonts.TAHOMA_8.getStringWidth(this.ageText);
            } else {
                this.ageText = null;
                this.textWidth = 0.0F;
            }
        }

        private static String formatAge(long elapsed) {
            if (elapsed < 1L) return "0s";
            if (elapsed < 60_000L) return (elapsed / 1000L) + "s";
            long minutes = elapsed / 60_000L;
            long seconds = (elapsed - minutes * 60_000L) / 1000L;
            if (elapsed < 3_600_000L) return minutes + "m " + seconds + "s";
            long hours = elapsed / 3_600_000L;
            minutes = (elapsed - hours * 3_600_000L) / 60_000L;
            if (elapsed < 86_400_000L) return hours + "h " + minutes + "m " + seconds + "s";
            long days = elapsed / 86_400_000L;
            hours = (elapsed - days * 86_400_000L) / 3_600_000L;
            return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
        }
    }
}
