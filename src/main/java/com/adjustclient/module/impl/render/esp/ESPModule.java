package com.adjustclient.module.impl.render.esp;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.adjustclient.module.impl.render.esp.render.EspRenderer;
import com.adjustclient.module.impl.render.esp.settings.*;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.Timer;
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

    @com.adjustclient.event.EventHandler
    public void onRender(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        onRender();
    }

    public void onRender() {
        if (renderers == null) renderers = new ArrayList<>();
        renderers.clear();
        List<com.adjustclient.util.anticheat.entity.TrackedEntity<?>> entities =
                com.adjustclient.util.anticheat.ServerTracker.INSTANCE.entityTracker.getEntities(filterMask(), null);
        if (items.enabled.isEnabled()) {
            List<com.adjustclient.util.anticheat.entity.TrackedItem> pooledItems = new ArrayList<>();
            for (var entity : entities) {
                if (entity instanceof com.adjustclient.util.anticheat.entity.TrackedItem item) {
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
    @com.adjustclient.event.EventHandler
    public void renderScaled(com.adjustclient.event.Render2DEvent event) {
        if (!isEnabled() || renderers == null) return;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        int configuredScale = com.adjustclient.service.config.user.UserConfiguration.scale.getInt();
        double scale = configuredScale == 16 ? 1000.0 : (configuredScale - 1) * 0.25 + 2.0;
        net.minecraft.client.gui.ScaledResolution resolution = event.scaledResolution;
        if (scale != 2.0) {
            resolution = new net.minecraft.client.gui.ScaledResolution(mc, scale);
            mc.entityRenderer.setupOverlayRendering(resolution);
            com.adjustclient.render.font.NvgContext.setScale((float) resolution.getScaleFactor_double());
        }
        org.lwjgl.opengl.GL11.glPushMatrix();
        outlineBuckets = new Int2ObjectOpenHashMap<>();
        org.joml.Vector4d[] positions = new org.joml.Vector4d[renderers.size()];
        com.adjustclient.event.Render2DEvent deferred = new com.adjustclient.event.Render2DEvent(null, 0.0F);
        if (collectRenderPositions(positions, resolution, event.partialTicks)) {
            net.minecraft.client.renderer.GlStateManager.disableBlend();
            renderRendererList(deferred, positions, resolution);
        }
        if (!deferred.postTasks.isEmpty()) {
            com.adjustclient.render.font.NvgContext.flushFrame();
            deferred.runPostTasks();
        }
        org.lwjgl.opengl.GL11.glPopMatrix();
        if (scale != 2.0) {
            mc.entityRenderer.setupOverlayRendering(event.scaledResolution);
            com.adjustclient.render.font.NvgContext.setScale((float) event.scaledResolution.getScaleFactor_double());
        }
        renderEntities(new net.minecraft.client.gui.ScaledResolution(mc), event.partialTicks);
    }
    @com.adjustclient.event.EventHandler
    public void acceptsPlayer(com.adjustclient.event.EntityNameRenderEvent event) {
        if (!isEnabled()) return;
        if (collecting) {
            event.setCancelled(true);
            return;
        }
        if (!(event.entity instanceof net.minecraft.entity.player.EntityPlayer player)
                || com.adjustclient.module.impl.combat.antibot.AntiBotModule.isBot(player.getEntityId())) return;
        if ((players.enabled.isEnabled() && players.nametagsEnabled.isEnabled())
                || (friendly.enabled.isEnabled() && friendly.nametagsEnabled.isEnabled())
                || (enemy.enabled.isEnabled() && enemy.nametagsEnabled.isEnabled())) {
            event.setCancelled(true);
        }
    }
    public void createRenderer(com.adjustclient.util.anticheat.entity.TrackedEntity<?> entity) {
        EntityEspSettings settings;
        if (entity.matchesType(224)) settings = friendly;
        else if (entity.matchesType(80)) settings = items;
        else if (entity.matchesType(576)) settings = tnt;
        else if (entity.matchesType(68)) settings = hostile;
        else if (entity.matchesType(72)) settings = passive;
        else settings = players;
        if (entity instanceof com.adjustclient.util.anticheat.entity.TrackedFakePlayer) {
            if (enemy.enabled.isEnabled() && com.adjustclient.Adjust.INSTANCE.tagManager.getTag(
                    entity.getEntity().getName()) == com.adjustclient.tag.PlayerTag.TARGET) {
                settings = enemy;
            } else if (com.adjustclient.module.impl.exploit.murdermystery.MurderMysteryModule.hasRole(
                    entity.getDisplayName(), com.adjustclient.hypixel.game.murdermystery.MurderMysteryRole.MURDERER)) {
                settings = enemy;
            }
        }
        if (!settings.dormant.isEnabled() && entity.isRemoved()) return;
        if (!settings.invisible.isEnabled() && entity.getEntity().isInvisible()) return;
        if (settings == items && !acceptsItem((com.adjustclient.util.anticheat.entity.TrackedItem) entity)) return;
        renderers.add(new com.adjustclient.module.impl.render.esp.render.EntityEspRenderer(
                entity, theme.getValue(), settings, this));
    }
    public boolean acceptsItem(com.adjustclient.util.anticheat.entity.TrackedItem entity) {
        net.minecraft.item.ItemStack stack = entity.getItemStack();
        if (!items.acceptsItem(net.minecraft.item.Item.getIdFromItem(stack.getItem()))) return false;
        if (!items.validate.isEnabled()) return true;
        if (itemValidator == null) itemValidator = new ItemValidator();
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == com.adjustclient.server.ServerType.HYPIXEL) {
            var state = com.adjustclient.Adjust.gameStateTracker.state;
            if (state.lobby != null && !state.lobby && state.game == com.adjustclient.hypixel.HypixelGame.SKYWARS
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
            if (renderer instanceof com.adjustclient.module.impl.render.esp.render.EntityEspRenderer entityRenderer) {
                net.minecraft.entity.Entity entity = entityRenderer.target.getEntity();
                position = new net.minecraft.util.Vec3(entity.posX, entity.posY, entity.posZ);
            } else {
                position = ((com.adjustclient.module.impl.render.esp.render.ItemPoolEspRenderer) renderer).center;
            }
            return -origin.squareDistanceTo(position);
        }));
    }
    public void getItemBucket(int color, EntityLivingBase entity) {
        outlineBuckets.computeIfAbsent(color, ignored -> {
            return new ArrayList<>();
        }).add(entity);
    }
    public void renderEntities(net.minecraft.client.gui.ScaledResolution resolution, float partialTicks) {
        if (outlineBuckets == null) return;
        collecting = true;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        for (var entry : outlineBuckets.int2ObjectEntrySet()) {
            com.adjustclient.render.shader.OutlineShader.INSTANCE.beginCapture(partialTicks);
            for (EntityLivingBase entity : entry.getValue()) {
                mc.getRenderManager().renderEntityStatic(entity, partialTicks, true);
            }
            com.adjustclient.render.shader.OutlineShader.INSTANCE.endCapture(entry.getIntKey(), resolution, 1.0F);
        }
        net.minecraft.client.renderer.GlStateManager.bindTexture(0);
        net.minecraft.client.renderer.GlStateManager.disableAlpha();
        collecting = false;
        outlineBuckets = null;
    }
    public void renderRendererList(com.adjustclient.event.Render2DEvent event, org.joml.Vector4d[] positions,
            net.minecraft.client.gui.ScaledResolution resolution) {
        for (int i = 0; i < renderers.size(); i++) {
            org.joml.Vector4d bounds = positions[i];
            if (bounds == null) continue;
            EspRenderer renderer = renderers.get(i);
            if (!renderer.initialized) {
                renderer.update();
                renderer.initialized = true;
            }
            if (renderer instanceof com.adjustclient.module.impl.render.esp.render.EntityEspRenderer entityRenderer) {
                entityRenderer.textY(event, bounds, resolution);
                continue;
            }
            var pooled = (com.adjustclient.module.impl.render.esp.render.ItemPoolEspRenderer) renderer;
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
                || (com.adjustclient.module.impl.render.ThirdPersonModule.INSTANCE != null
                        && com.adjustclient.module.impl.render.ThirdPersonModule.INSTANCE.freeLooking)) mask |= 0x80;
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
        var tracked = new com.adjustclient.util.anticheat.entity.TrackedLivingEntity(player);
        renderers.add(new com.adjustclient.module.impl.render.esp.render.EntityEspRenderer(
                tracked, theme.getValue(), players, this));
    }
    @com.adjustclient.event.EventHandler
    public void handlePacket(com.adjustclient.event.LowLevelPacketReceiveEvent event) {
        if (event.packet instanceof net.minecraft.network.play.server.S01PacketJoinGame) {
            closestPlayerPosition = null;
            return;
        }
        if (!isEnabled()) return;
        if (!(event.packet instanceof net.minecraft.network.play.server.S05PacketSpawnPosition spawn)
                || !players.enabled.isEnabled()) return;
        var server = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null
                || server.antiCheatModel.serverType != com.adjustclient.server.ServerType.HYPIXEL) return;
        var state = com.adjustclient.Adjust.gameStateTracker.state;
        if (state.startTimer == null || state.isSpectator()) return;
        if (state.game != com.adjustclient.hypixel.HypixelGame.SKYWARS
                && state.game != com.adjustclient.hypixel.HypixelGame.BLITZ_SG
                && state.game != com.adjustclient.hypixel.HypixelGame.TNT_TAG) return;
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            for (net.minecraft.entity.player.EntityPlayer player : mc.theWorld.playerEntities) {
                if (player.isUser()
                        || com.adjustclient.module.impl.combat.antibot.AntiBotModule.isBot(player.getEntityId())) continue;
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
