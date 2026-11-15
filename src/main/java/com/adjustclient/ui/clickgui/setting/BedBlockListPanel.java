package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.NvgContext;
import com.adjustclient.render.font.image.NvgDraw;
import java.awt.Color;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefensePreset;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.clickgui.ModifierPhase;
import com.adjustclient.ui.clickgui.frame.ElementDecorator;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import com.adjustclient.event.Render2DEvent;

public final class BedBlockListPanel extends ElementDecorator {

    BedDefenseSettingComponent owner;
    Timer hoverTimer;
    Animation fade;
    Animation fadeOut;
    BedDefensePreset shown;
    long obf32;

    public BedBlockListPanel(BedDefenseSettingComponent owner) {
        this.owner = owner;
    }

    @Override
    public ModifierPhase getPhase() {
        return ModifierPhase.POST;
    }

    @Override
    public void reset() {
        this.hoverTimer = null;
        this.fade = null;
        this.fadeOut = null;
        this.shown = null;
        this.obf32 = 0L;
    }

    ItemStack iconFor(BedBlockEntry entry) {
        Block block = BedPreviewRenderer.blockFor(entry.blockType(), entry.blockId());
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            return new ItemStack(item);
        }
        return new ItemStack(Item.getItemFromBlock(Blocks.anvil), 1, 100);
    }

    @Override
    public void render(net.minecraft.client.gui.ScaledResolution screen, Render2DEvent queue, Object drawList, Vec2f mouse, boolean firstPass) {
        BedDefenseSettingComponent o = this.owner;
        if (o.pressedId == null && o.menu == null && o.hoveredPreset != null) {
            if (this.shown != null && this.shown.id != null && this.shown.id.equals(o.hoveredPreset.id)) {
                this.shown = o.hoveredPreset;
                if (this.hoverTimer == null) {
                    this.hoverTimer = new Timer(System.currentTimeMillis());
                }
            } else {
                this.shown = o.hoveredPreset;
                this.hoverTimer = new Timer(System.currentTimeMillis());
                this.fade = null;
                this.fadeOut = null;
                this.obf32 = 0L;
            }
            if (this.hoverTimer.hasElapsed(300L)) {
                if (this.fade == null) {
                    this.fade = new Animation(Easing.LINEAR, 90L);
                }
                this.fade.update(1.0F);
                if (this.fade.finished) {
                    if (this.fadeOut == null) {
                        this.fadeOut = new Animation(Easing.LINEAR, 90L);
                        this.obf32 = System.currentTimeMillis();
                    }
                    this.fadeOut.update(1.0F);
                }
            }
        } else {
            this.hoverTimer = null;
            if (this.fadeOut != null && this.fadeOut.value != 0.0F) {
                this.fadeOut.update(0.0F);
            } else if (this.fade != null) {
                this.fade.update(0.0F);
                if (this.fade.finished) {
                    this.fade = null;
                    this.fadeOut = null;
                    this.shown = null;
                }
            }
        }
        if (this.fade == null || this.fade.value == 0.0F || this.shown == null) {
            return;
        }
        BedDefensePreset preset = this.shown;
        ((Render2DEvent) drawList).postTasks.add(() -> this.renderPanel(preset, screen, mouse));
    }

    void renderPanel(BedDefensePreset preset, net.minecraft.client.gui.ScaledResolution screen, Vec2f mouse) {
        if (preset == null || this.fade == null) {
            return;
        }
        float a = this.fade.value;
        float contentFade = this.fadeOut != null ? this.fadeOut.value : 0.0F;
        List<BedBlockEntry> kinds = this.owner.blockCounts(preset);
        int columns = Math.min(5, kinds.size());
        float w = Math.max(132.0F, (float) columns * 18.0F + 2.0F);
        float halfW = w <= 132.0F ? 66.0F : w / 2.0F;
        float x = Math.max(2.0F, Math.min(mouse.x + 5.0F, (float) screen.getScaledWidth() - w - 2.0F));
        float h = (float) ((kinds.size() + 4) / 5) * 18.0F + 98.0F + 1.0F;
        float y = mouse.y - 6.0F;
        if (y + h > (float) screen.getScaledHeight() - 2.0F) {
            y = (mouse.y - h) - 4.0F;
        }
        y = Math.max(2.0F, y);
        int bgAlpha = (int) (a * 170.0F);
        NvgDraw.fillRect((bgAlpha << 24) | 0x19191C, x, y, x + w, y + h);
        NvgDraw.strokeRect(true, (bgAlpha << 24) | 0x232326, x, y, x + w, y + h, 0.5F);
        float innerLeft = x + 1.0F;
        float innerRight = (x + w) - 1.0F;
        float headerTop = y + 1.0F;
        float headerBottom = y + 14.0F;
        NvgDraw.fillRect(((int) (a * 105.0F) << 24) | 0x232327, innerLeft, headerTop, innerRight, headerBottom);
        float previewBottom = headerBottom + 76.0F;
        NvgDraw.fillRect(((int) (a * 100.0F) << 24) | 0x111114, innerLeft, headerBottom, innerRight, previewBottom);
        float infoBottom = previewBottom + 8.0F;
        NvgDraw.fillRect(((int) (a * 75.0F) << 24) | 0x232327, innerLeft, previewBottom, innerRight, infoBottom);
        float gridY = infoBottom - 1.0F;
        int cellColor = new Color(45, 45, 49, (int) (a * 85.0F)).getRGB();
        for (int i = 0; i < kinds.size(); i++) {
            float cellX = innerLeft + (float) (i % 5) * 18.0F;
            float cellY = gridY + (float) (i / 5) * 18.0F;
            NvgDraw.fillRect(cellColor, cellX, cellY, cellX + 18.0F, cellY + 18.0F);
        }
        NvgContext.flushFrame();
        if (contentFade > 0.0F) {
            long elapsed = Math.max(0L, System.currentTimeMillis() - this.obf32);
            float rotation = ((float) (elapsed % 16000L) * 360.0F) / 16000.0F + 35.0F;
            BedPreviewRenderer.render(preset, elapsed, screen, rotation, innerLeft, headerBottom, w - 2.0F, 76.0F);
        }
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        float itemFade = Math.min(contentFade, ClickGuiScreen.INSTANCE.fade.animation.value);
        int itemAlpha = (int) (itemFade * 200.0F);
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i < kinds.size(); i++) {
            float cellX = innerLeft + (float) (i % 5) * 18.0F;
            float cellY = gridY + (float) (i / 5) * 18.0F;
            mc.getRenderItem().renderItemIntoGUI(this.iconFor(kinds.get(i)), itemAlpha, false, cellX + 1.0F, cellY + 1.0F);
        }
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        boolean enabled = preset.enabled;
        String state = enabled ? "Enabled" : "Disabled";
        int textAlpha = (int) (contentFade * 235.0F);
        float stateX = ((x + w) - Fonts.TAHOMA_10.getStringWidth(state)) - 4.0F;
        int stateColor = enabled ? (textAlpha << 24) | 0x96D7B4 : new Color(220, 105, 105, textAlpha).getRGB();
        float stateY = ((14.0F - Fonts.TAHOMA_10.size) / 2.0F + y) + 1.0F;
        Fonts.TAHOMA_10.drawString(state, stateX, stateY, stateColor);
        float maxNameWidth = Math.max(0.0F, (stateX - x) - 6.0F);
        String name = Fonts.TAHOMA_12.trimStringToWidth(preset.name, maxNameWidth, false);
        if (!name.equals(preset.name)) {
            String trimmed = name.trim();
            name = trimmed.isEmpty() ? "..." : trimmed + "...";
        }
        float nameY = ((14.0F - Fonts.TAHOMA_12.size) / 2.0F + y) + 1.0F;
        Fonts.TAHOMA_12.drawString(name, x + 4.0F, nameY, (textAlpha << 24) | 0xE1E1E4);
        String priority = "Priority #" + this.owner.indexOfPreset(preset);
        String blocks = preset.placements.size() + " blocks";
        BedBlockPos dims = this.owner.sizeOf(preset);
        String dimsText = dims.width() + "x" + dims.height() + "x" + dims.depth();
        int infoAlpha = (int) (contentFade * 210.0F);
        int infoColor = (infoAlpha << 24) | 0xB4B4B9;
        float infoY = ((8.0F - Fonts.TAHOMA_10.size) / 2.0F + previewBottom) + 1.5F;
        Fonts.TAHOMA_10.drawString(priority, x + 4.0F, infoY, infoColor);
        Fonts.TAHOMA_10.drawCenteredString(blocks, halfW + x, infoY, infoColor);
        Fonts.TAHOMA_10.drawString(dimsText, ((x + w) - Fonts.TAHOMA_10.getStringWidth(dimsText)) - 4.0F, infoY, infoColor);
        int countColor = (textAlpha << 24) | 0xEBEBEB;
        for (int i = 0; i < kinds.size(); i++) {
            int row = i / 5;
            int col = i - 5 * row;
            float cellRight = ((float) col * 18.0F + innerLeft) + 18.0F;
            float countY = ((float) row * 18.0F + gridY) + 13.0F;
            String countText = kinds.get(i).count() + "x";
            float countX = (cellRight - Fonts.TAHOMA_8.getStringWidth(countText)) - 1.0F;
            Fonts.TAHOMA_8.drawString(countText, countX, countY, countColor);
        }
    }
}
