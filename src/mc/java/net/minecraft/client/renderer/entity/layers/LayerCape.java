package net.minecraft.client.renderer.entity.layers;

import com.adjustclient.Adjust;
import com.adjustclient.event.CapePositionEvent;
import com.adjustclient.event.EntityOpacityEvent;
import com.adjustclient.event.EventBus;
import com.adjustclient.game.entity.tracker.ServerPositionTracker;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.user.Cape;
import com.adjustclient.user.ClientUser;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCape implements LayerRenderer<AbstractClientPlayer>
{
    private final RenderPlayer playerRenderer;

    public LayerCape(RenderPlayer playerRendererIn)
    {
        this.playerRenderer = playerRendererIn;
    }

    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        EntityOpacityEvent opacityEvent = this.playerRenderer.currentOpacityEvent;
        boolean invisible = opacityEvent != null ? opacityEvent.invisible : entitylivingbaseIn.isInvisible();
        float alpha = opacityEvent != null ? opacityEvent.bodyAlpha : 1.0F;

        if (entitylivingbaseIn.hasPlayerInfo() && !invisible)
        {
            ClientUser user = null;
            if (entitylivingbaseIn instanceof EntityPlayerSP)
            {
                if (Adjust.INSTANCE != null && Adjust.INSTANCE.field40 != null)
                {
                    user = Adjust.INSTANCE.field40.field16;
                }
            }
            else if (Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null && Adjust.INSTANCE.screenStack.serverTracker != null && entitylivingbaseIn.getGameProfile() != null)
            {
                user = Adjust.INSTANCE.screenStack.serverTracker.findUser(entitylivingbaseIn.getGameProfile().getName());
            }

            Cape customCape = null;
            ResourceLocation capeLocation = null;
            if (user != null)
            {
                Cape chosen = user.useClientColor && UserConfiguration.INSTANCE != null ? UserConfiguration.INSTANCE.cape : user.cape;
                if (chosen != null && chosen != Cape.NONE)
                {
                    customCape = chosen;
                    capeLocation = chosen.texture;
                }
            }
            if (customCape == null)
            {
                if (!entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE))
                {
                    return;
                }
                capeLocation = entitylivingbaseIn.getLocationCape();
            }
            if (capeLocation == null)
            {
                return;
            }

            CapePositionEvent positionEvent = new CapePositionEvent(
                entitylivingbaseIn,
                entitylivingbaseIn.prevChasingPosX, entitylivingbaseIn.prevChasingPosY, entitylivingbaseIn.prevChasingPosZ,
                entitylivingbaseIn.chasingPosX, entitylivingbaseIn.chasingPosY, entitylivingbaseIn.chasingPosZ,
                entitylivingbaseIn.prevPosX, entitylivingbaseIn.prevPosY, entitylivingbaseIn.prevPosZ,
                entitylivingbaseIn.posX, entitylivingbaseIn.posY, entitylivingbaseIn.posZ,
                entitylivingbaseIn.prevRenderYawOffset, entitylivingbaseIn.renderYawOffset
            );
            if (ServerPositionTracker.INSTANCE != null)
            {
                ServerPositionTracker.INSTANCE.onRenderPosition(positionEvent);
            }
            EventBus.post(positionEvent);

            if (customCape != null && customCape.field33 && user != null)
            {
                int color = user.getColor();
                float r = Math.max(0.15F, (float)(color >> 16 & 255) / 255.0F);
                float g = Math.max(0.15F, (float)(color >> 8 & 255) / 255.0F);
                float b = Math.max(0.15F, (float)(color & 255) / 255.0F);
                GlStateManager.color(r, g, b, alpha);
            }
            else
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            }

            if (alpha < 1.0F)
            {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.depthMask(false);
            }

            this.playerRenderer.bindTexture(capeLocation);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            double d0 = positionEvent.prevChasingPosX + (positionEvent.chasingPosX - positionEvent.prevChasingPosX) * (double)partialTicks - (positionEvent.prevPosX + (positionEvent.posX - positionEvent.prevPosX) * (double)partialTicks);
            double d1 = positionEvent.prevChasingPosY + (positionEvent.chasingPosY - positionEvent.prevChasingPosY) * (double)partialTicks - (positionEvent.prevPosY + (positionEvent.posY - positionEvent.prevPosY) * (double)partialTicks);
            double d2 = positionEvent.prevChasingPosZ + (positionEvent.chasingPosZ - positionEvent.prevChasingPosZ) * (double)partialTicks - (positionEvent.prevPosZ + (positionEvent.posZ - positionEvent.prevPosZ) * (double)partialTicks);
            float f = positionEvent.prevRenderYawOffset + (positionEvent.renderYawOffset - positionEvent.prevRenderYawOffset) * partialTicks;
            double d3 = (double)MathHelper.sin(f * (float)Math.PI / 180.0F);
            double d4 = (double)(-MathHelper.cos(f * (float)Math.PI / 180.0F));
            float f1 = (float)d1 * 10.0F;
            f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
            float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
            float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;

            if (f2 < 0.0F)
            {
                f2 = 0.0F;
            }

            float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
            f1 = f1 + MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

            if (entitylivingbaseIn.isSneaking())
            {
                f1 += 25.0F;
            }

            GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            this.playerRenderer.getMainModel().renderCape(0.0625F);
            GlStateManager.popMatrix();

            if (alpha < 1.0F)
            {
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
