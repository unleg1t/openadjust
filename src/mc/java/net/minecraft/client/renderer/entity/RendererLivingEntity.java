package net.minecraft.client.renderer.entity;

import com.adjustclient.Adjust;
import com.adjustclient.event.EntityOpacityEvent;
import com.adjustclient.event.EventBus;
import com.adjustclient.game.entity.tracker.ServerPositionEntry;
import com.adjustclient.game.entity.tracker.ServerPositionTracker;
import com.adjustclient.game.render.FrozenPlayerRenderer;
import com.adjustclient.module.impl.movement.antivoid.LegitAntiVoidMode;
import com.adjustclient.module.impl.render.HurtcamModule;
import com.adjustclient.module.impl.render.chams.ChamsMode;
import com.adjustclient.module.impl.render.chams.ChamsModule;
import com.adjustclient.module.impl.render.chams.ChamsTargetSettings;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.service.util.ServiceUtil;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ChatColor;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import optifine.Config;
import optifine.Reflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import shadersmod.client.Shaders;

@SideOnly(Side.CLIENT)
public abstract class RendererLivingEntity<T extends EntityLivingBase> extends Render<T>
{
    private static final Logger logger = LogManager.getLogger();
    private static final DynamicTexture textureBrightness = new DynamicTexture(16, 16);
    private static Field legitSafeSpotPreviewField;
    private static Field legitSafeSpotEntityField;
    private static boolean legitSafeSpotFieldsResolved;
    protected ModelBase mainModel;
    protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);
    protected List<LayerRenderer<T>> layerRenderers = Lists.<LayerRenderer<T>>newArrayList();
    protected boolean renderOutlines = false;
    public static float NAME_TAG_RANGE = 64.0F;
    public static float NAME_TAG_RANGE_SNEAK = 32.0F;
    public static Float previewOpacityOverride;
    public com.adjustclient.event.EntityOpacityEvent currentOpacityEvent;

    public RendererLivingEntity(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn)
    {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
    }

    public <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean addLayer(U layer)
    {
        return this.layerRenderers.add((LayerRenderer<T>)layer);
    }

    protected <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean removeLayer(U layer)
    {
        return this.layerRenderers.remove(layer);
    }

    public ModelBase getMainModel()
    {
        return this.mainModel;
    }

    protected float interpolateRotation(float par1, float par2, float par3)
    {
        float f;

        for (f = par2 - par1; f < -180.0F; f += 360.0F)
        {
            ;
        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return par1 + par3 * f;
    }

    public void transformHeldFull3DItemLayer()
    {
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (Reflector.RenderLivingEvent_Pre_Constructor.exists() && Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, new Object[] {entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)}))
        {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
        this.mainModel.isRiding = entity.isRiding();

        if (Reflector.ForgeEntity_shouldRiderSit.exists())
        {
            this.mainModel.isRiding = entity.isRiding() && entity.ridingEntity != null && Reflector.callBoolean(entity.ridingEntity, Reflector.ForgeEntity_shouldRiderSit, new Object[0]);
        }

        this.mainModel.isChild = entity.isChild();

        EntityOpacityEvent opacityEvent = new EntityOpacityEvent(entity, partialTicks);
        if (entity.forceInvisible)
        {
            opacityEvent.invisible = true;
        }
        if (Adjust.frozenPlayerRenderListener != null)
        {
            com.adjustclient.game.render.FrozenPlayerRenderer frozen = Adjust.frozenPlayerRenderListener.find(entity);
            if (frozen != null)
            {
                opacityEvent.bodyAlpha = frozen.opacity;
            }
        }
        if (LegitAntiVoidMode.INSTANCE != null && LegitAntiVoidMode.INSTANCE.isActive() && isLegitAntiVoidGhost(entity))
        {
            opacityEvent.bodyAlpha = 0.2F;
        }
        EventBus.post(opacityEvent);
        this.currentOpacityEvent = opacityEvent;

        try
        {
            float entityRenderYawOffset = entity.renderYawOffset;
            float entityPrevRenderYawOffset = entity.prevRenderYawOffset;
            float entityRotationYawHead = entity.rotationYawHead;
            float entityPrevRotationYawHead = entity.prevRotationYawHead;

            if (ServerPositionTracker.INSTANCE != null)
            {
                ServerPositionEntry tracked = ServerPositionTracker.INSTANCE.findTrackedPosition(entity);
                if (tracked != null)
                {
                    entityRenderYawOffset = tracked.renderYawOffset;
                    entityPrevRenderYawOffset = tracked.previousRenderYawOffset;
                    entityRotationYawHead = tracked.headYaw;
                    entityPrevRotationYawHead = tracked.previousHeadYaw;
                }
            }

            float f = this.interpolateRotation(entityPrevRenderYawOffset, entityRenderYawOffset, partialTicks);
            float f1 = this.interpolateRotation(entityPrevRotationYawHead, entityRotationYawHead, partialTicks);
            float f2 = f1 - f;

            if (this.mainModel.isRiding && entity.ridingEntity instanceof EntityLivingBase)
            {
                EntityLivingBase entitylivingbase = (EntityLivingBase)entity.ridingEntity;
                f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                f2 = f1 - f;
                float f3 = MathHelper.wrapAngleTo180_float(f2);

                if (f3 < -85.0F)
                {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F)
                {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F)
                {
                    f += f3 * 0.2F;
                }
            }

            float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            this.renderLivingAt(entity, x, y, z);
            float f8 = this.handleRotationFloat(entity, partialTicks);
            this.rotateCorpse(entity, f8, f, partialTicks);
            GlStateManager.enableRescaleNormal();
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            this.preRenderCallback(entity, partialTicks);
            float f4 = 0.0625F;
            GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
            float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild())
            {
                f6 *= 3.0F;
            }

            if (f5 > 1.0F)
            {
                f5 = 1.0F;
            }

            GlStateManager.enableAlpha();
            this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
            this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625F, entity);

            boolean realPlayerScaled = ServiceUtil.isRealPlayer(entity);
            if (realPlayerScaled)
            {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            ChamsTargetSettings chams = FrozenPlayerRenderer.ENTITY_SETTINGS.get(entity);
            if (chams == null && ChamsModule.INSTANCE != null && ChamsModule.INSTANCE.isEnabled())
            {
                chams = ChamsModule.INSTANCE.overrides.get(entity);
            }

            if (chams != null && chams.enabled.isEnabled())
            {
                if (chams.mode.getValue() == ChamsMode.NORMAL)
                {
                    GlStateManager.enablePolygonOffset();
                    GlStateManager.doPolygonOffset(1.0F, -1100000.0F);
                    boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                    this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                    if (flag)
                    {
                        this.unsetBrightness();
                    }

                    GlStateManager.depthMask(true);

                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                    {
                        this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);
                    }

                    GlStateManager.disablePolygonOffset();
                    GlStateManager.doPolygonOffset(1.0F, 1100000.0F);
                }
                else
                {
                    if (chams.throughWallsEnabled.isEnabled())
                    {
                        GlStateManager.disableDepth();
                        Color occluded = chams.throughWallsColor.getColor();
                        boolean occludedBright = this.setBrightness(entity, true, true, false, partialTicks,
                                (float)occluded.getRed() / 255.0F,
                                (float)occluded.getGreen() / 255.0F,
                                (float)occluded.getBlue() / 255.0F,
                                (float)occluded.getAlpha() / 255.0F);
                        this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                        if (occludedBright)
                        {
                            this.unsetBrightness();
                        }

                        GlStateManager.enableDepth();
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    }

                    Color visible = chams.color.getColor();
                    boolean visibleBright = this.setBrightness(entity, true, true, true, partialTicks,
                            (float)visible.getRed() / 255.0F,
                            (float)visible.getGreen() / 255.0F,
                            (float)visible.getBlue() / 255.0F,
                            (float)visible.getAlpha() / 255.0F);
                    this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                    if (visibleBright)
                    {
                        this.unsetBrightness();
                    }

                    GlStateManager.depthMask(true);

                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                    {
                        this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);
                    }
                }
            }
            else if (this.renderOutlines)
            {
                boolean flag1 = this.setScoreTeamColor(entity);
                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                if (flag1)
                {
                    this.unsetScoreTeamColor();
                }
            }
            else
            {
                boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);

                if (flag)
                {
                    this.unsetBrightness();
                }

                GlStateManager.depthMask(true);

                if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator())
                {
                    this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);
                }
            }

            if (realPlayerScaled)
            {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
            }

            GlStateManager.disableRescaleNormal();
        }
        catch (Exception exception)
        {
            logger.error((String)"Couldn\'t render entity", (Throwable)exception);
        }
        finally
        {
            this.currentOpacityEvent = null;
            previewOpacityOverride = null;
        }

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        if (!this.renderOutlines)
        {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }

        if (Reflector.RenderLivingEvent_Post_Constructor.exists())
        {
            Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, new Object[] {entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)});
        }
    }

    private static boolean isLegitAntiVoidGhost(EntityLivingBase entity)
    {
        Object safeSpot = LegitAntiVoidMode.INSTANCE != null ? LegitAntiVoidMode.INSTANCE.safeSpot : null;
        if (safeSpot == null)
        {
            return false;
        }
        try
        {
            if (!legitSafeSpotFieldsResolved)
            {
                Field previewField = safeSpot.getClass().getDeclaredField("preview");
                previewField.setAccessible(true);
                Object preview = previewField.get(safeSpot);
                if (preview != null)
                {
                    Field entityField = preview.getClass().getDeclaredField("entity");
                    entityField.setAccessible(true);
                    legitSafeSpotEntityField = entityField;
                    legitSafeSpotPreviewField = previewField;
                    legitSafeSpotFieldsResolved = true;
                    return entityField.get(preview) == entity;
                }
                legitSafeSpotPreviewField = previewField;
                return false;
            }
            Object preview = legitSafeSpotPreviewField.get(safeSpot);
            if (preview == null)
            {
                return false;
            }
            return legitSafeSpotEntityField.get(preview) == entity;
        }
        catch (ReflectiveOperationException ignored)
        {
            return false;
        }
    }

    protected boolean setScoreTeamColor(T entityLivingBaseIn)
    {
        int i = 16777215;

        if (entityLivingBaseIn instanceof EntityPlayer)
        {
            ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)entityLivingBaseIn.getTeam();

            if (scoreplayerteam != null)
            {
                String s = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());

                if (s.length() >= 2)
                {
                    i = this.getFontRendererFromRenderManager().getColorCode(s.charAt(1));
                }
            }
        }

        float f1 = (float)(i >> 16 & 255) / 255.0F;
        float f2 = (float)(i >> 8 & 255) / 255.0F;
        float f = (float)(i & 255) / 255.0F;
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(f1, f2, f, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void unsetScoreTeamColor()
    {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor)
    {
        EntityOpacityEvent opacity = this.currentOpacityEvent;
        boolean invisible = opacity != null ? opacity.invisible : (entitylivingbaseIn.forceInvisible || entitylivingbaseIn.isInvisible());
        boolean flag = !invisible;
        boolean flag1 = !flag && !entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);

        if (flag || flag1)
        {
            if (!this.bindEntityTexture(entitylivingbaseIn))
            {
                return;
            }

            float alpha = opacity != null ? opacity.bodyAlpha : 1.0F;
            if (invisible && flag1 && alpha == 1.0F && previewOpacityOverride == null)
            {
                alpha = 0.15F;
            }
            if (previewOpacityOverride != null)
            {
                alpha = previewOpacityOverride.floatValue();
            }
            alpha = Math.max(0.0F, Math.min(1.0F, alpha));
            boolean translucent = flag1 || alpha < 1.0F;

            if (translucent)
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableColorMaterial();
                GlStateManager.colorMaterial(1032, 5634);
                GlStateManager.resetColor();
                GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scaleFactor);

            if (translucent)
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }
    }

    protected boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks)
    {
        return this.setBrightness(entityLivingBaseIn, partialTicks, true);
    }

    protected boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures)
    {
        return this.setBrightness(entitylivingbaseIn, combineTextures, false, false, partialTicks, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    protected boolean setBrightness(T entitylivingbaseIn, boolean combineTextures, boolean customChams, boolean chamsHurtFade, float partialTicks, float chamsR, float chamsG, float chamsB, float chamsA)
    {
        float f = entitylivingbaseIn.getBrightness(partialTicks);
        int i = this.getColorMultiplier(entitylivingbaseIn, f, partialTicks);
        boolean flag = (i >> 24 & 255) > 0;
        boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;
        boolean active = flag1 || customChams;
        boolean hasTranslucentOpacity = this.currentOpacityEvent != null && (this.currentOpacityEvent.bodyAlpha < 1.0F || this.currentOpacityEvent.armorAlpha < 1.0F);

        if (!flag && (!active || !combineTextures))
        {
            return false;
        }
        else
        {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, hasTranslucentOpacity ? GL11.GL_MODULATE : GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            if (hasTranslucentOpacity)
            {
                GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
                GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
            }
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            this.brightnessBuffer.position(0);

            if (customChams)
            {
                float r = Math.min(chamsR, 1.0F);
                float g = Math.min(chamsG, 1.0F);
                float b = Math.min(chamsB, 1.0F);
                float a = 1.0F - (float)(i >> 24 & 255) / 255.0F;

                if (chamsHurtFade)
                {
                    if (entitylivingbaseIn.hurtTime >= 1)
                    {
                        a = (a - ((float)entitylivingbaseIn.hurtTime - partialTicks) * 0.035F) * chamsA;
                    }
                    else if (entitylivingbaseIn.deathTime >= 1)
                    {
                        a = chamsA * (a - ((float)entitylivingbaseIn.deathTime - partialTicks) * 0.075F);
                    }
                    else
                    {
                        a = chamsA;
                    }

                    a = Math.max(0.0F, Math.min(1.0F, a));
                }

                this.brightnessBuffer.put(r);
                this.brightnessBuffer.put(g);
                this.brightnessBuffer.put(b);
                this.brightnessBuffer.put(a);

                if (Config.isShaders())
                {
                    Shaders.setEntityColor(r, g, b, a);
                }
            }
            else if (flag1)
            {
                if (HurtcamModule.INSTANCE != null && HurtcamModule.INSTANCE.isEnabled() && HurtcamModule.INSTANCE.modelColorEnabled.isEnabled())
                {
                    Color hurtColor = HurtcamModule.INSTANCE.modelColorColor.getColor();
                    float hurtR = (float)hurtColor.getRed() / 255.0F;
                    float hurtG = (float)hurtColor.getGreen() / 255.0F;
                    float hurtB = (float)hurtColor.getBlue() / 255.0F;
                    this.brightnessBuffer.put(hurtR);
                    this.brightnessBuffer.put(hurtG);
                    this.brightnessBuffer.put(hurtB);
                    this.brightnessBuffer.put(0.3F);

                    if (Config.isShaders())
                    {
                        Shaders.setEntityColor(hurtR, hurtG, hurtB, 0.3F);
                    }
                }
                else
                {
                    this.brightnessBuffer.put(1.0F);
                    this.brightnessBuffer.put(0.0F);
                    this.brightnessBuffer.put(0.0F);
                    this.brightnessBuffer.put(0.3F);

                    if (Config.isShaders())
                    {
                        Shaders.setEntityColor(1.0F, 0.0F, 0.0F, 0.3F);
                    }
                }
            }
            else
            {
                float f1 = (float)(i >> 24 & 255) / 255.0F;
                float f2 = (float)(i >> 16 & 255) / 255.0F;
                float f3 = (float)(i >> 8 & 255) / 255.0F;
                float f4 = (float)(i & 255) / 255.0F;
                this.brightnessBuffer.put(f2);
                this.brightnessBuffer.put(f3);
                this.brightnessBuffer.put(f4);
                this.brightnessBuffer.put(1.0F - f1);

                if (Config.isShaders())
                {
                    Shaders.setEntityColor(f2, f3, f4, 1.0F - f1);
                }
            }

            this.brightnessBuffer.flip();
            GL11.glTexEnvfv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, (FloatBuffer)this.brightnessBuffer);
            GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(textureBrightness.getGlTextureId());
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
            if (customChams)
            {
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
            }
            else
            {
                GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
            }
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
            if (customChams)
            {
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            }
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_REPLACE);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            return true;
        }
    }

    protected void unsetBrightness()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.disableTexture2D();
        GlStateManager.bindTexture(0);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders())
        {
            Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    protected void renderLivingAt(T entityLivingBaseIn, double x, double y, double z)
    {
        GlStateManager.translate((float)x, (float)y, (float)z);
    }

    protected void rotateCorpse(T bat, float p_77043_2_, float p_77043_3_, float partialTicks)
    {
        GlStateManager.rotate(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);

        if (bat.deathTime > 0)
        {
            float f = ((float)bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt_float(f);

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            GlStateManager.rotate(f * this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
        }
        else
        {
            String s = ChatColor.getTextWithoutFormattingCodes(bat.getName());

            if (s != null && (s.equals("Dinnerbone") || s.equals("Grumm")) && (!(bat instanceof EntityPlayer) || ((EntityPlayer)bat).isWearing(EnumPlayerModelParts.CAPE)))
            {
                GlStateManager.translate(0.0F, bat.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    protected float getSwingProgress(T livingBase, float partialTickTime)
    {
        return livingBase.getSwingProgress(partialTickTime);
    }

    protected float handleRotationFloat(T livingBase, float partialTicks)
    {
        return (float)livingBase.ticksExisted + partialTicks;
    }

    protected void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_)
    {
        for (LayerRenderer<T> layerrenderer : this.layerRenderers)
        {
            boolean flag = this.setBrightness(entitylivingbaseIn, partialTicks, layerrenderer.shouldCombineTextures());
            layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);

            if (flag)
            {
                this.unsetBrightness();
            }
        }
    }

    protected float getDeathMaxRotation(T entityLivingBaseIn)
    {
        return 90.0F;
    }

    protected int getColorMultiplier(T entitylivingbaseIn, float lightBrightness, float partialTickTime)
    {
        return 0;
    }

    protected void preRenderCallback(T entitylivingbaseIn, float partialTickTime)
    {
    }

    public void renderName(T entity, double x, double y, double z)
    {
        if (!Reflector.RenderLivingEvent_Specials_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Specials_Pre_Constructor, new Object[] {entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)}))
        {
            if (this.canRenderName(entity))
            {
                Entity view = Minecraft.getMinecraft().cameraOverride;
                Entity refEntity = view != null ? view : this.renderManager.livingPlayer;
                if (refEntity != null)
                {
                    double d0 = entity.getDistanceSqToEntity(refEntity);
                    float f = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;

                    if (d0 < (double)(f * f))
                    {
                        String s = entity.getDisplayName().getFormattedText();
                        if (StreamerModule.nameMap != null)
                        {
                            if (entity instanceof EntityPlayer)
                            {
                                GameProfile profile = ((EntityPlayer)entity).getGameProfile();
                                if (profile != null)
                                {
                                    s = StreamerModule.replaceName(s, profile.getName());
                                }
                            }
                            else if (entity instanceof EntityArmorStand)
                            {
                                s = StreamerModule.replaceKnownNames(s);
                            }
                        }
                        float f1 = 0.02666667F;
                        GlStateManager.alphaFunc(516, 0.1F);

                        if (entity.isSneaking())
                        {
                            FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                            GlStateManager.pushMatrix();
                            GlStateManager.translate((float)x, (float)y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), (float)z);
                            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                            GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
                            GlStateManager.translate(0.0F, 9.374999F, 0.0F);
                            GlStateManager.disableLighting();
                            GlStateManager.depthMask(false);
                            GlStateManager.enableBlend();
                            GlStateManager.disableTexture2D();
                            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                            int i = (int)fontrenderer.getStringWidth(s) / 2;
                            Tessellator tessellator = Tessellator.getInstance();
                            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                            worldrenderer.pos((double)(-i - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos((double)(-i - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos((double)(i + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos((double)(i + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            tessellator.draw();
                            GlStateManager.enableTexture2D();
                            GlStateManager.depthMask(true);
                            fontrenderer.drawString(s, -(int)fontrenderer.getStringWidth(s) / 2, 0, 553648127);
                            GlStateManager.enableLighting();
                            GlStateManager.disableBlend();
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            GlStateManager.popMatrix();
                        }
                        else
                        {
                            this.renderOffsetLivingLabel(entity, x, y - (entity.isChild() ? (double)(entity.height / 2.0F) : 0.0D), z, s, 0.02666667F, d0);
                        }
                    }
                }
            }

            if (Reflector.RenderLivingEvent_Specials_Post_Constructor.exists())
            {
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Specials_Post_Constructor, new Object[] {entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z)});
            }
        }
    }

    protected boolean canRenderName(T entity)
    {
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;
        if (entityplayersp == null)
        {
            return false;
        }

        if (entity instanceof EntityPlayer && entity != entityplayersp)
        {
            Team team = entity.getTeam();
            Team team1 = entityplayersp.getTeam();

            if (team != null)
            {
                Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

                switch (team$enumvisible)
                {
                    case ALWAYS:
                        return true;
                    case NEVER:
                        return false;
                    case HIDE_FOR_OTHER_TEAMS:
                        return team1 == null || team.isSameTeam(team1);
                    case HIDE_FOR_OWN_TEAM:
                        return team1 == null || !team.isSameTeam(team1);
                    default:
                        return true;
                }
            }
        }

        return Minecraft.isGuiEnabled() && entity != this.renderManager.livingPlayer && !entity.isInvisibleToPlayer(entityplayersp) && entity.riddenByEntity == null;
    }

    public void setRenderOutlines(boolean renderOutlinesIn)
    {
        this.renderOutlines = renderOutlinesIn;
    }

    static
    {
        int[] aint = textureBrightness.getTextureData();

        for (int i = 0; i < 256; ++i)
        {
            aint[i] = -1;
        }

        textureBrightness.updateDynamicTexture();
    }
}
