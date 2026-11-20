package com.adjustclient.core.gui.clickgui.setting.preview;

import com.adjustclient.modules.impl.render.esp.ESPModule;
import com.adjustclient.modules.impl.render.esp.EspPreviewSetting;
import com.adjustclient.modules.impl.render.esp.render.EntityEspRenderer;
import com.adjustclient.modules.impl.render.esp.settings.EntityEspSettings;
import com.adjustclient.modules.impl.render.esp.settings.EspTheme;
import com.adjustclient.modules.impl.render.esp.settings.MobEspSettings;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.gui.clickgui.ClickGuiScreen;
import com.adjustclient.core.gui.clickgui.setting.SettingComponent;
import com.adjustclient.core.gui.screen.AdjustScreen;
import com.adjustclient.utilities.PositionSnapshot;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.utilities.anticheat.entity.EspPreviewData;
import com.adjustclient.utilities.anticheat.entity.TrackedFakePlayer;
import com.mojang.authlib.GameProfile;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;

public final class EspPreviewComponent extends SettingComponent<EspPreviewSetting> {

    private EntityEspRenderer target;
    private ESPModule espModule;

    public EspPreviewComponent(EspPreviewSetting setting) {
        super(setting);
        if (setting != null && setting.getParent() instanceof ExpandSetting expand
                && expand.getParent() instanceof ESPModule module) {
            this.espModule = module;
        }
    }

    /**
     * - not ready (target == null): "Please wait..." centred at (bounds.x + bounds.width/2, y + 4.0), Tahoma 10, colour rgba(135,135,135,150)
     *   (alpha 150 + 50 * hover once the row animation exists);
     * - ready: flushes NVG, enables GL_SCISSOR_TEST, falls back (-111,-111) mouse sentinel to (centreX, baseY + 14), draws the entity with
     *   GuiInventory.drawEntityOnScreen(centreX, baseY + 44, 22, ...) (after enabling depth / blend during GUI fade), and invokes
     *   {@code EntityEspRenderer.textY} under glPushMatrix()/disableBlend() with proximity to box centre / screen diagonal and fade alpha.
     */
    @Override
    public void renderContent(com.adjustclient.core.events.Render2DEvent context, Vec2f mouse) {
        float height = this.getHeight();
        if (height != this.height) {
            if (ClickGuiScreen.INSTANCE != null) {
                ClickGuiScreen.INSTANCE.layoutDirty = true; // relayout wanted
            }
        }
        if (this.target == null) {
            int alpha = 150 + (this.hover.animation == null ? 0 : (int) (50.0F * this.hover.animation.value));
            float cx = this.bounds.x + this.bounds.width / 2.0F;
            Fonts.TAHOMA_10.drawString("Please wait...",
                    cx - Fonts.TAHOMA_10.getStringWidth("Please wait...") * 0.5F,
                    this.y + 4.0F, new Color(135, 135, 135, alpha).getRGB());
            return;
        }
        EntityEspSettings settings = this.setting.getValue();
        EspTheme theme = this.target.generalSettings;
        EntityPlayer player = ((TrackedFakePlayer) this.target.target).getEntity();
        int baseX = (int) (this.bounds.x + 4.0F);
        int baseY = (int) this.y + 3;
        if (settings.nametagsEnabled.isEnabled()) {
            baseY += theme == EspTheme.ADJUST ? 7 : 8;
        }
        NvgContext.flushFrame();
        boolean scissorWasEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        try {
            int centreX = (int) (this.width / 2.0F) + baseX - 4;
            Vec2f aim = (mouse.x == -111.0F && mouse.y == -111.0F)
                    ? new Vec2f((float) centreX, (float) (baseY + 14))
                    : mouse;
            GlStateManager.enableDepth();
            if (ClickGuiScreen.INSTANCE != null && ClickGuiScreen.INSTANCE.fade != null
                    && ClickGuiScreen.INSTANCE.fade.animation != null
                    && !ClickGuiScreen.INSTANCE.fade.animation.finished) {
                net.minecraft.client.renderer.entity.RendererLivingEntity.previewOpacityOverride =
                        ClickGuiScreen.INSTANCE.fade.animation.value;
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GuiInventory.drawEntityOnScreen(centreX, baseY + 44, 22,
                    (float) centreX - aim.x, (float) (baseY + 44) - aim.y - 30.0F, player);
            GlStateManager.disableDepth();
            GL11.glPushMatrix();
            try {
                GlStateManager.disableBlend();
                double cx = (double) baseX + (double) this.width * 0.5;
                Vector4d box = new Vector4d(cx - 14.0, (double) baseY, cx + 7.0, (double) baseY + 46.0);
                double boxCx = (box.x + box.z) / 2.0;
                double boxCy = (box.w + box.y) / 2.0;
                float dx = (float) (aim.x - boxCx);
                float dy = (float) (aim.y - boxCy);
                ScaledResolution resolution = (context != null && context.scaledResolution != null)
                        ? context.scaledResolution
                        : AdjustScreen.screenResolution;
                if (resolution == null) {
                    return;
                }
                int sw = resolution.getScaledWidth();
                int sh = resolution.getScaledHeight();
                float proximity = (float) (Math.sqrt(dx * dx + dy * dy) / Math.sqrt((double) (sh * sh + sw * sw)));
                this.target.healthSetting = Float.valueOf(Math.min(proximity, 1.0F));
                float fadeAlpha = (ClickGuiScreen.INSTANCE != null && ClickGuiScreen.INSTANCE.fade != null
                        && ClickGuiScreen.INSTANCE.fade.animation != null)
                        ? ClickGuiScreen.INSTANCE.fade.animation.value
                        : 1.0F;
                this.target.field28 = Float.valueOf(fadeAlpha);
                this.target.textY(context, box, resolution);
                this.target.healthSetting = null;
                this.target.field28 = null;
            } finally {
                GL11.glPopMatrix();
            }
        } finally {
            if (scissorWasEnabled) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
            } else {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }
    }

    @Override
    public boolean onMousePressed(Vec2f mouse, int button, boolean pressed) {
        return false;
    }

    @Override
    public float getHeight() {
        if (this.target == null) {
            return 9.0F;
        }
        EntityEspSettings settings = this.setting.getValue();
        EspTheme theme = this.target.generalSettings;
        float height = 52.0F;
        if (settings.nametagsEnabled.isEnabled()) {
            height = theme == EspTheme.ADJUST ? 59.0F : 60.0F;
        }
        if (settings instanceof MobEspSettings mob && mob.armorEnabled.isEnabled()) {
            height += mob.armorThinner.isEnabled() ? 1.5F : 2.0F;
        }
        if (this.target.heldItemName != null) {
            height += theme == EspTheme.ADJUST ? 6.0F : 8.0F;
        }
        return height;
    }

    /**
     * Rebuilds the preview EntityEspRenderer every tick (creating the fake EntityOtherPlayerMP + TrackedFakePlayer lazily
     * when target == null), calls updateSettings() if uninitialized, and clears deferred equipment/potion icon lists.
     */
    @Override
    public void onUpdate() {
        EntityEspRenderer renderer;
        if (this.target != null) {
            renderer = new EntityEspRenderer(this.target.target, this.espModule.theme.getValue(), this.setting.getValue(), this.espModule);
        } else {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.getSession() == null) {
                return;
            }
            GameProfile profile = mc.getSession().getProfile();
            EntityOtherPlayerMP player = new EntityOtherPlayerMP(null, profile);
            player.playerInfo = new NetworkPlayerInfo(profile);
            player.getDataWatcher().updateObject(10, (byte) 127);
            TrackedFakePlayer tracked = new TrackedFakePlayer(player);
            tracked.team = player.worldObj != null ? player.getWorldScoreboard().getPlayersTeam(player.getName()) : null;
            EspPreviewData previewData = new EspPreviewData();
            previewData.tracked = tracked;
            previewData.positions = new PositionSnapshot[15];
            tracked.fakePreview = previewData;
            renderer = new EntityEspRenderer(tracked, this.espModule.theme.getValue(), this.setting.getValue(), this.espModule);
        }
        this.target = renderer;
        if (!renderer.initialized) {
            renderer.updateSettings();
            renderer.initialized = true;
        }
        this.target.potionIcons = null;
        this.target.equipmentIcons = null;
    }
}
