package com.adjustclient.modules.impl.render.esp;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.adjustclient.modules.impl.render.esp.render.EspRenderer;
import com.adjustclient.modules.impl.render.esp.settings.*;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.utilities.Timer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;

public final class ESPModule extends Module {
    public static ESPModule INSTANCE;
    public List<EspRenderer> renderers;
    public ItemValidator itemValidator;
    public final Timer closestPlayerTimer = new Timer();
    public BlockPos closestPlayerPosition;
    public Int2ObjectOpenHashMap<List<EntityLivingBase>> outlineBuckets;
    public final ModeSetting<EspTheme> theme;
    public final PlayerEspSettings players;
    public final PlayerEspSettings friendly;
    public final PlayerEspSettings enemy;
    public final MobEspSettings hostile;
    public final LivingEspSettings passive;
    public final ItemEspSettings items;
    public final TntEspSettings tnt;
    public boolean collecting;
    public Object field88;

    public ESPModule() {
        super("ESP", Category.RENDER);
        INSTANCE = this;
        theme = new ModeSetting<>(this, "Theme", EspTheme.MINECRAFT, EspTheme.values());
        players = new PlayerEspSettings(this, "Players", true);
        friendly = new PlayerEspSettings(this, "Friendly", true);
        enemy = new PlayerEspSettings(this, "Enemy", false);
        hostile = new MobEspSettings(this, "Hostile", false, false);
        passive = new LivingEspSettings(this, "Passive", false, false);
        items = new ItemEspSettings(this);
        tnt = new TntEspSettings(this);
        setHidden(false);
    }

    @com.adjustclient.core.events.EventHandler
    public void onRender(com.adjustclient.core.events.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        onRender();
    }

    public void onRender() {
        if (renderers == null) renderers = new ArrayList<>();
        renderers.clear();
        List<com.adjustclient.utilities.anticheat.entity.TrackedEntity<?>> entities =
                com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.entityTracker.getEntities(filterMask(), null);
        if (items.enabled.isEnabled()) {
            List<com.adjustclient.utilities.anticheat.entity.TrackedItem> pooledItems = new ArrayList<>();
            for (var entity : entities) {
                if (entity instanceof com.adjustclient.utilities.anticheat.entity.TrackedItem item) {
                    if (!item.removed && !item.collected && acceptsItem(item)) pooledItems.add(item);
                } else {
                    createRenderer(entity);
                }
            }
            new ItemPool(this, pooledItems, items).pool();
        } else {
            for (var entity : entities) createRenderer(entity);
        }
        createClosestPlayerPreview();
        sortEntities(renderers);
        if (itemValidator != null) itemValidator.snapshot = null;
    }
    @com.adjustclient.core.events.EventHandler
    public void renderScaled(com.adjustclient.core.events.Render2DEvent event) {
        if (!isEnabled() || renderers == null) return;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        int configuredScale = com.adjustclient.managers.config.user.UserConfiguration.scale.getInt();
        double scale = configuredScale == 16 ? 1000.0 : (configuredScale - 1) * 0.25 + 2.0;
        net.minecraft.client.gui.ScaledResolution resolution = event.scaledResolution;
        if (scale != 2.0) {
            resolution = new net.minecraft.client.gui.ScaledResolution(mc, 2);
            mc.entityRenderer.setupOverlayRendering(resolution);
            com.adjustclient.core.rendering.font.NvgContext.setScale((float) resolution.getScaleFactor_double());
        }
        org.lwjgl.opengl.GL11.glPushMatrix();
        outlineBuckets = new Int2ObjectOpenHashMap<>();
        org.joml.Vector4d[] positions = new org.joml.Vector4d[renderers.size()];
        com.adjustclient.core.events.Render2DEvent deferred = new com.adjustclient.core.events.Render2DEvent(null, 0.0F);
        if (collectRenderPositions(positions, resolution, event.partialTicks)) {
            net.minecraft.client.renderer.GlStateManager.disableBlend();
            renderRendererList(deferred, positions, resolution);
        }
        if (!deferred.postTasks.isEmpty()) {
            com.adjustclient.core.rendering.font.NvgContext.flushFrame();
            deferred.runPostTasks();
        }
        org.lwjgl.opengl.GL11.glPopMatrix();
        if (scale != 2.0) {
            mc.entityRenderer.setupOverlayRendering(event.scaledResolution);
            com.adjustclient.core.rendering.font.NvgContext.setScale((float) event.scaledResolution.getScaleFactor_double());
        }
        boolean renderedOutlines = outlineBuckets != null && !outlineBuckets.isEmpty();
        renderEntities(event.scaledResolution, event.partialTicks);
        if (renderedOutlines) {
            mc.entityRenderer.setupOverlayRendering(event.scaledResolution);
            net.minecraft.client.renderer.GlStateManager.enableAlpha();
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    @com.adjustclient.core.events.EventHandler
    public void acceptsPlayer(com.adjustclient.core.events.EntityNameRenderEvent event) {
        if (!isEnabled()) return;
        if (collecting) {
            event.setCancelled(true);
            return;
        }
        if (!(event.entity instanceof net.minecraft.entity.player.EntityPlayer player)
                || com.adjustclient.modules.impl.combat.antibot.AntiBotModule.isBot(player.getEntityId())) return;
        if ((players.enabled.isEnabled() && players.nametagsEnabled.isEnabled())
                || (friendly.enabled.isEnabled() && friendly.nametagsEnabled.isEnabled())
                || (enemy.enabled.isEnabled() && enemy.nametagsEnabled.isEnabled())) {
            event.setCancelled(true);
        }
    }
    public void createRenderer(com.adjustclient.utilities.anticheat.entity.TrackedEntity<?> entity) {
        EntityEspSettings settings;
        if (entity.matchesType(224)) settings = friendly;
        else if (entity.matchesType(80)) settings = items;
        else if (entity.matchesType(576)) settings = tnt;
        else if (entity.matchesType(68)) settings = hostile;
        else if (entity.matchesType(72)) settings = passive;
        else settings = players;
        if (entity instanceof com.adjustclient.utilities.anticheat.entity.TrackedFakePlayer) {
            if (enemy.enabled.isEnabled() && com.adjustclient.Adjust.INSTANCE.tagManager.getTag(
                    entity.getEntity().getName()) == com.adjustclient.core.tag.PlayerTag.TARGET) {
                settings = enemy;
            } else if (com.adjustclient.modules.impl.exploit.murdermystery.MurderMysteryModule.hasRole(
                    entity.getDisplayName(), com.adjustclient.utilities.hypixel.game.murdermystery.MurderMysteryRole.MURDERER)) {
                settings = enemy;
            }
        }
        if (!settings.dormant.isEnabled() && entity.isRemoved()) return;
        if (!settings.invisible.isEnabled() && entity.getEntity().isInvisible()) return;
        if (settings == items && !acceptsItem((com.adjustclient.utilities.anticheat.entity.TrackedItem) entity)) return;
        renderers.add(new com.adjustclient.modules.impl.render.esp.render.EntityEspRenderer(
                entity, theme.getValue(), settings, this));
    }
    public boolean acceptsItem(com.adjustclient.utilities.anticheat.entity.TrackedItem entity) {
        net.minecraft.item.ItemStack stack = entity.getItemStack();
        if (!items.acceptsItem(net.minecraft.item.Item.getIdFromItem(stack.getItem()))) return false;
        if (!items.validate.isEnabled()) return true;
        if (itemValidator == null) itemValidator = new ItemValidator();
        var server = com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.utilities.server.ServerType.HYPIXEL) {
            var state = com.adjustclient.Adjust.gameStateTracker.state;
            if (state.lobby != null && !state.lobby && state.game == com.adjustclient.utilities.hypixel.HypixelGame.SKYWARS
                    && entity.getEntity().posY < 10.0) return false;
        }
        return itemValidator.validate(stack);
    }
    public void sortEntities(List<EspRenderer> entities) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        net.minecraft.entity.Entity camera = mc.cameraOverride != null ? mc.cameraOverride : mc.getRenderViewEntity();
        if (camera == null) return;
        net.minecraft.util.Vec3 origin = new net.minecraft.util.Vec3(camera.posX, camera.posY, camera.posZ);
        entities.sort(Comparator.comparingDouble(renderer -> {
            net.minecraft.util.Vec3 position;
            if (renderer instanceof com.adjustclient.modules.impl.render.esp.render.EntityEspRenderer entityRenderer) {
                net.minecraft.entity.Entity entity = entityRenderer.target.getEntity();
                position = new net.minecraft.util.Vec3(entity.posX, entity.posY, entity.posZ);
            } else {
                position = ((com.adjustclient.modules.impl.render.esp.render.ItemPoolEspRenderer) renderer).center;
            }
            return -origin.squareDistanceTo(position);
        }));
    }
    public void getItemBucket(int color, EntityLivingBase entity) {
        if (outlineBuckets != null) {
            outlineBuckets.computeIfAbsent(color, ignored -> {
                return new ArrayList<>();
            }).add(entity);
        }
    }
    public void renderEntities(net.minecraft.client.gui.ScaledResolution resolution, float partialTicks) {
        if (outlineBuckets == null) return;
        collecting = true;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        for (var entry : outlineBuckets.int2ObjectEntrySet()) {
            com.adjustclient.core.rendering.shader.OutlineShader.INSTANCE.beginCapture(partialTicks);
            for (EntityLivingBase entity : entry.getValue()) {
                mc.getRenderManager().renderEntityStatic(entity, partialTicks, true);
            }
            com.adjustclient.core.rendering.shader.OutlineShader.INSTANCE.endCapture(entry.getIntKey(), resolution, 1.0F);
        }
        net.minecraft.client.renderer.GlStateManager.bindTexture(0);
        net.minecraft.client.renderer.GlStateManager.disableAlpha();
        collecting = false;
        outlineBuckets = null;
    }
    public void renderRendererList(com.adjustclient.core.events.Render2DEvent event, org.joml.Vector4d[] positions,
            net.minecraft.client.gui.ScaledResolution resolution) {
        for (int i = 0; i < renderers.size(); i++) {
            org.joml.Vector4d bounds = positions[i];
            if (bounds == null) continue;
            EspRenderer renderer = renderers.get(i);
            if (!renderer.initialized) {
                renderer.update();
                renderer.initialized = true;
            }
            if (renderer instanceof com.adjustclient.modules.impl.render.esp.render.EntityEspRenderer entityRenderer) {
                entityRenderer.textY(event, bounds, resolution);
                continue;
            }
            var pooled = (com.adjustclient.modules.impl.render.esp.render.ItemPoolEspRenderer) renderer;
            pooled.updateDistance(event, bounds, resolution);
        }
    }
    public boolean collectRenderPositions(org.joml.Vector4d[] positions,
            net.minecraft.client.gui.ScaledResolution resolution, float partialTicks) {
        boolean anyVisible = false;
        for (int i = 0; i < renderers.size(); i++) {
            positions[i] = renderers.get(i).project(resolution, partialTicks);
            anyVisible |= positions[i] != null;
        }
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.entityRenderer.worldProjectionActive) {
            mc.entityRenderer.worldProjectionActive = false;
            org.lwjgl.opengl.GL11.glClear(256);
            org.lwjgl.opengl.GL11.glMatrixMode(5889);
            org.lwjgl.opengl.GL11.glLoadIdentity();
            org.lwjgl.opengl.GL11.glOrtho(0.0, resolution.getScaledWidth_double(),
                    resolution.getScaledHeight_double(), 0.0, 1000.0, 3000.0);
            org.lwjgl.opengl.GL11.glMatrixMode(5888);
            org.lwjgl.opengl.GL11.glLoadIdentity();
            org.lwjgl.opengl.GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        }
        return anyVisible;
    }
    @Override public void onEnable() { renderers = new ArrayList<>(); }
    @Override public void onDisable() {
        renderers = null;
        itemValidator = null;
    }
    public int filterMask() {
        boolean showPlayers = players.enabled.isEnabled();
        boolean showHostile = hostile.enabled.isEnabled();
        boolean showPassive = passive.enabled.isEnabled();
        boolean showItems = items.enabled.isEnabled();
        boolean showFriendly = friendly.enabled.isEnabled();
        int mask = showPlayers ? 3 : 1;
        if (showHostile) mask |= 4;
        if (showPassive) mask |= 8;
        if (showItems) mask |= 0x10;
        if (showFriendly) mask |= 0x60;
        else mask |= 0x40;
        if (enemy.enabled.isEnabled()) mask |= 0x800;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView != 0 || mc.cameraOverride != null
                || (com.adjustclient.modules.impl.render.ThirdPersonModule.INSTANCE != null
                        && com.adjustclient.modules.impl.render.ThirdPersonModule.INSTANCE.freeLooking)) mask |= 0x80;
        if (tnt.enabled.isEnabled()) mask |= 0x200;
        return mask;
    }
    public void createClosestPlayerPreview() {
        if (!players.enabled.isEnabled() || closestPlayerPosition == null || closestPlayerTimer.hasElapsed(5000L)) return;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        net.minecraft.client.entity.EntityOtherPlayerMP player = new net.minecraft.client.entity.EntityOtherPlayerMP(
                mc.theWorld, new com.mojang.authlib.GameProfile(mc.thePlayer.getUniqueID(), "Closest player"));
        double x = closestPlayerPosition.getX();
        double y = closestPlayerPosition.getY();
        double z = closestPlayerPosition.getZ();
        player.setPosition(x, y, z);
        player.prevPosX = player.lastTickPosX = x;
        player.prevPosY = player.lastTickPosY = y;
        player.prevPosZ = player.lastTickPosZ = z;
        player.setHealth(0.0F);
        var tracked = new com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity(player);
        renderers.add(new com.adjustclient.modules.impl.render.esp.render.EntityEspRenderer(
                tracked, theme.getValue(), players, this));
    }
    @com.adjustclient.core.events.EventHandler
    public void handlePacket(com.adjustclient.core.events.LowLevelPacketReceiveEvent event) {
        if (event.packet instanceof net.minecraft.network.play.server.S01PacketJoinGame) {
            closestPlayerPosition = null;
            return;
        }
        if (!isEnabled()) return;
        if (!(event.packet instanceof net.minecraft.network.play.server.S05PacketSpawnPosition spawn)
                || !players.enabled.isEnabled()) return;
        var server = com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null
                || server.antiCheatModel.serverType != com.adjustclient.utilities.server.ServerType.HYPIXEL) return;
        var state = com.adjustclient.Adjust.gameStateTracker.state;
        if (state.startTimer == null || state.isSpectator()) return;
        if (state.game != com.adjustclient.utilities.hypixel.HypixelGame.SKYWARS
                && state.game != com.adjustclient.utilities.hypixel.HypixelGame.BLITZ_SG
                && state.game != com.adjustclient.utilities.hypixel.HypixelGame.TNT_TAG) return;
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            for (net.minecraft.entity.player.EntityPlayer player : mc.theWorld.playerEntities) {
                if (player.isUser()
                        || com.adjustclient.modules.impl.combat.antibot.AntiBotModule.isBot(player.getEntityId())) continue;
                if (!player.getGameProfile().getName().contains("§")) {
                    closestPlayerPosition = null;
                    return;
                }
            }
            closestPlayerTimer.reset();
            closestPlayerPosition = spawn.getSpawnPos();
        } catch (java.util.ConcurrentModificationException ignored) {
        }
    }

}
