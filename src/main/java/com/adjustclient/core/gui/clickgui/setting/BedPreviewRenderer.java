package com.adjustclient.core.gui.clickgui.setting;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefenseBlockType;
import com.adjustclient.core.rendering.RenderUtil;

public final class BedPreviewRenderer {

    private BedPreviewRenderer() {
    }

    static Block blockFor(BedDefenseBlockType type, int blockId) {
        switch (BedDefenseConstants.$SwitchMap$Block[type.ordinal()]) {
            case 1: return Blocks.wool;
            case 2: return Blocks.glass;
            case 3: return Blocks.planks;
            case 4: return Blocks.end_stone;
            case 5: return Blocks.hardened_clay;
            case 6: return Blocks.obsidian;
            default:
                Block found = (Block) Block.blockRegistry.getObjectById(blockId);
                return found != null ? found : Blocks.barrier;
        }
    }

    static void renderBlock(BlockRendererDispatcher dispatcher, IBlockState state, BedPreviewTransform transform, float x, float y, float z) {
        if (state.getBlock().getRenderType() == -1) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 0.5F, (y + 0.5F) + transform.yOffset(), z + 0.5F);
        GL11.glRotatef(transform.rotation(), 0.35F, 1.0F, 0.2F);
        GL11.glScalef(transform.scale(), transform.scale(), transform.scale());
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        int renderType = state.getBlock().getRenderType();
        if (renderType == 2) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            Item item = Item.getItemFromBlock(state.getBlock());
            TileEntityItemStackRenderer.instance.renderByItem(new ItemStack(item));
        } else if (renderType == 3) {
            IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(state); // includes the missing-model fallback
            dispatcher.getBlockModelRenderer().renderModelBrightness(model, state, 1.0F, true);
        }
        GL11.glPopMatrix();
    }

    static void render(com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefensePreset preset, long time, net.minecraft.client.gui.ScaledResolution clip, float rotation, float x, float y, float w, float h) {
        BedDefenseBounds bounds = BedDefenseBounds.a(preset);
        float scale = bounds.fitScale(w - 8.0F, h - 8.0F);
        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        RenderUtil.pushScissor(clip, 0, x, y, w, h);
        GL11.glPushMatrix();
        GL11.glClear(256);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GL11.glTranslatef(x + w / 2.0F, y + h / 2.0F + 1.0F, 150.0F);
        GL11.glScalef(scale, -scale, scale);
        GL11.glRotatef(28.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-((bounds.maxX() + bounds.minX()) / 2.0F), -((bounds.maxY() + bounds.minY()) / 2.0F), -((bounds.maxZ() + bounds.minZ()) / 2.0F));
        // the bed: BlockBed default state with FACING = SOUTH, PART = FOOT, OCCUPIED = FALSE, and PART = HEAD
        IBlockState foot = Blocks.bed.getDefaultState()
                .withProperty(BlockBed.FACING, EnumFacing.SOUTH)
                .withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT)
                .withProperty(BlockBed.OCCUPIED, Boolean.FALSE);
        IBlockState head = foot.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
        BedPreviewTransform bedTransform = BedPreviewTransform.IDENTITY;
        renderBlock(dispatcher, foot, bedTransform, 0.0F, 0.0F, 0.0F);
        renderBlock(dispatcher, head, bedTransform, 0.0F, 0.0F, 1.0F);
        List<com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefensePlacement> placements = preset.placements;
        int n = placements.size();
        if (n != 0) {
            int tIn = (int) Math.max(650L, Math.min(3200L, 120L * n));
            int tOut = (int) Math.max(400L, Math.min(1600L, 55L * n));
            long t = time % (tIn + tOut + 2100);
            for (int i = 0; i < n; i++) {
                BedPreviewTransform transform;
                if (tIn > t) {
                    float p = (float) (t * n) / (float) tIn - (float) i;
                    if (p <= 0.0F) {
                        transform = null; // not yet visible
                    } else if (p >= 1.0F) {
                        transform = BedPreviewTransform.IDENTITY; // saturated appear transform has scale 1
                    } else {
                        float s = (float) Math.sin(p * Math.PI) * 0.12F;
                        transform = new BedPreviewTransform(1.0F - (1.0F - p) * (1.0F - p) * (1.0F - p) + s, 0.0F, 0.0F);
                    }
                } else if (t - tIn < 1800L) {
                    transform = BedPreviewTransform.IDENTITY;
                } else if (tOut > t - tIn - 1800L) {
                    float q = (float) ((t - tIn - 1800L) * n) / (float) tOut - (float) (n - 1 - i);
                    if (q >= 1.0F) {
                        transform = null;
                    } else if (q <= 0.0F) {
                        transform = BedPreviewTransform.IDENTITY;
                    } else {
                        float sign = (i & 1) != 0 ? -1.0F : 1.0F;
                        transform = new BedPreviewTransform(1.0F - q * q, q * 0.35F, sign * (q * 35.0F));
                    }
                } else {
                    transform = null;
                }
                if (transform != null && 0.0F < transform.scale()) {
                    com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefensePlacement placement = placements.get(i);
                    Block block = blockFor(placement.type, placement.blockId);
                    renderBlock(dispatcher, block.getDefaultState(), transform, placement.x, placement.y, placement.z);
                }
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        GlStateManager.disableRescaleNormal();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
