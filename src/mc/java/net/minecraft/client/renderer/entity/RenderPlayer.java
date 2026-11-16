package net.minecraft.client.renderer.entity;

import com.adjustclient.Adjust;
import com.adjustclient.command.impl.debug.DebugEpsteinSubCommand;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.SkeletonRenderEvent;
import com.adjustclient.module.impl.combat.antibot.AntiBotModule;
import com.adjustclient.module.impl.render.skeleton.SkeletonModule;
import com.adjustclient.module.impl.render.skeleton.SkeletonPose;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import optifine.PlayerItemsLayer;

@SideOnly(Side.CLIENT)
public class RenderPlayer extends RendererLivingEntity<AbstractClientPlayer>
{
    private boolean smallArms;

    public RenderPlayer(RenderManager renderManager)
    {
        this(renderManager, false);
    }

    public RenderPlayer(RenderManager renderManager, boolean useSmallArms)
    {
        super(renderManager, new ModelPlayer(0.0F, useSmallArms), 0.5F);
        this.smallArms = useSmallArms;
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
        this.addLayer(new PlayerItemsLayer(this));
    }

    public ModelPlayer getMainModel()
    {
        return (ModelPlayer)super.getMainModel();
    }

    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!entity.isUser() || this.renderManager.livingPlayer == entity)
        {
            double d0 = y;

            if (entity.isSneaking() && !(entity instanceof EntityPlayerSP))
            {
                d0 = y - 0.125D;
            }

            if (DebugEpsteinSubCommand.enabled
                    && !(entity instanceof EntityPlayerSP)
                    && !AntiBotModule.isBot(entity.getEntityId()))
            {
                d0 -= 0.69D;
            }

            this.setModelVisibilities(entity);
            super.doRender(entity, x, d0, z, entityYaw, partialTicks);

            if (SkeletonModule.INSTANCE != null && SkeletonModule.INSTANCE.isEnabled() && partialTicks != 1.0F && SkeletonModule.INSTANCE.poses != null)
            {
                SkeletonPose pose = SkeletonModule.INSTANCE.poses.get(entity);
                if (pose != null)
                {
                    SkeletonModule.beginSkeletons();
                    EventBus.post(new SkeletonRenderEvent(entity, this.getMainModel(), partialTicks));
                    SkeletonModule.INSTANCE.drawSkeleton(pose, this.renderManager, this.getMainModel(), partialTicks);
                    SkeletonModule.endSkeletons();
                }
            }
        }
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer)
    {
        ModelPlayer modelplayer = this.getMainModel();

        if (clientPlayer.isSpectator())
        {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        }
        else
        {
            ItemStack itemstack = clientPlayer instanceof EntityPlayerSP && Adjust.slotSpoofer != null && Adjust.slotSpoofer.field17
                    ? Adjust.slotSpoofer.getRenderItemStack()
                    : clientPlayer.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = clientPlayer.isSneaking();

            if (itemstack == null)
            {
                modelplayer.heldItemRight = 0;
            }
            else
            {
                modelplayer.heldItemRight = 1;

                if (clientPlayer.getItemInUseCount() > 0)
                {
                    EnumAction enumaction = itemstack.getItemUseAction();

                    if (enumaction == EnumAction.BLOCK)
                    {
                        modelplayer.heldItemRight = 3;
                    }
                    else if (enumaction == EnumAction.BOW)
                    {
                        modelplayer.aimedBow = true;
                    }
                }
            }
        }
    }

    protected ResourceLocation getEntityTexture(AbstractClientPlayer entity)
    {
        return entity.getLocationSkin();
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.0F, 0.1875F, 0.0F);
    }

    protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime)
    {
        float f = 0.9375F;
        GlStateManager.scale(f, f, f);
    }

    protected void renderOffsetLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_)
    {
        if (p_177069_10_ < 100.0D && entityIn.worldObj != null)
        {
            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null)
            {
                Score score = scoreboard.getValueFromObjective(entityIn.getName(), scoreobjective);
                this.renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
                y += (double)((float)this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * p_177069_9_);
            }
        }

        super.renderOffsetLivingLabel(entityIn, x, y, z, str, p_177069_9_, p_177069_10_);
    }

    public void renderRightArm(AbstractClientPlayer clientPlayer)
    {
        float f = 1.0F;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.swingProgress = 0.0F;
        modelplayer.isSneak = false;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.renderRightArm();
    }

    public void renderLeftArm(AbstractClientPlayer clientPlayer)
    {
        float f = 1.0F;
        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelplayer.isSneak = false;
        modelplayer.swingProgress = 0.0F;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.renderLeftArm();
    }

    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z)
    {
        if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping())
        {
            super.renderLivingAt(entityLivingBaseIn, x + (double)entityLivingBaseIn.renderOffsetX, y + (double)entityLivingBaseIn.renderOffsetY, z + (double)entityLivingBaseIn.renderOffsetZ);
        }
        else
        {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    protected void rotateCorpse(AbstractClientPlayer bat, float p_77043_2_, float p_77043_3_, float partialTicks)
    {
        if (bat.isEntityAlive() && bat.isPlayerSleeping())
        {
            GlStateManager.rotate(bat.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.rotateCorpse(bat, p_77043_2_, p_77043_3_, partialTicks);
        }
    }
}
