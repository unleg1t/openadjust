package com.adjustclient.module.impl.render;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public final class ItemPhysicsModule extends Module {
    public static java.util.Random random;
    public static ItemPhysicsModule INSTANCE;
    public final NumberSetting rotationSpeed;
    public ItemPhysicsModule() {
        super("Item Physics", Category.RENDER);
        INSTANCE = this;
        rotationSpeed = new NumberSetting(this, "Rotation speed", 1.5F, 1.0F, 3.0F, 0.5F);
    }
    static { random = new java.util.Random(); }
    public void renderItemError(ItemStack stack, IBakedModel model) {
        try {
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, model, true);
        } catch (Throwable error) {
            Logger.log(LogLevel.WARN, true, "Failed to render item: {}", error);
            error.printStackTrace();
        }
    }
    public void renderItem(RenderEntityItem renderer, EntityItem item, double x, double y, double z) {
        ItemStack stack = item.getEntityItem();
        if (stack.getItem() == null) return;
        random.setSeed(Item.getIdFromItem(stack.getItem()) + stack.getMetadata());
        renderer.bindTexture(TextureMap.locationBlocksTexture);
        renderer.getRenderManager().renderEngine.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        IBakedModel model = mc.getRenderItem().getItemModelMesher().getItemModel(stack);
        boolean gui3d = model.isGui3d();
        boolean physical3d = model.isGui3d();
        int count = stack.stackSize >= 49 ? 5 : stack.stackSize >= 33 ? 4
                : stack.stackSize >= 17 ? 3 : stack.stackSize >= 2 ? 2 : 1;
        GL11.glTranslatef((float) x, (float) y, (float) z);
        if (model.isGui3d()) GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(item.rotationYaw, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(0.0F, 0.0F, physical3d ? -0.079999998F : -0.039999999F);
        if (physical3d || mc.gameSettings != null) {
            float speed = INSTANCE.rotationSpeed.getFloat();
            if (physical3d) {
                if (!item.onGround) item.rotationPitch += speed;
            } else if (item.worldObj != null) {
                if (item.onGround) item.rotationPitch = 0.0F;
                else item.rotationPitch += speed;
            }
            GL11.glRotatef(item.rotationPitch, 1.0F, 0.0F, 0.0F);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushMatrix();
        model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GROUND);
        renderItemError(stack, model);
        GL11.glPopMatrix();
        if (!gui3d) GL11.glTranslatef(0.0F, 0.0F, 0.053750001F);
        for (int i = 1; i < count; i++) {
            GL11.glPushMatrix();
            if (gui3d) {
                float offsetX = (random.nextFloat() * 2.0F - 1.0F) * 0.15000001F;
                float offsetY = (random.nextFloat() * 2.0F - 1.0F) * 0.15000001F;
                float offsetZ = (random.nextFloat() * 2.0F - 1.0F) * 0.15000001F;
                GL11.glTranslatef(offsetX, offsetY, offsetZ);
            }
            model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GROUND);
            renderItemError(stack, model);
            GL11.glPopMatrix();
            if (!gui3d) GL11.glTranslatef(0.0F, 0.0F, 0.053750001F);
        }
        GL11.glPopMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        renderer.bindTexture(TextureMap.locationBlocksTexture);
        renderer.getRenderManager().renderEngine.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }
}
