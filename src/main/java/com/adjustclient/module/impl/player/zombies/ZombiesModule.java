package com.adjustclient.module.impl.player.zombies;

import com.adjustclient.Adjust;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.setting.*;
import com.adjustclient.event.*;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.hypixel.game.zombies.ZombiesGameState;
import com.adjustclient.hypixel.game.zombies.entity.ZombiesEntity;
import com.adjustclient.hypixel.game.zombies.entity.prediction.PredictedTarget;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import java.util.ArrayList;
import java.util.List;

public final class ZombiesModule extends Module {
    public static ZombiesModule INSTANCE;
    public final ZombiesAutoShoot autoShoot;
    public final BooleanSetting espZombies;
    public final BooleanSetting espPowerUps;
    public final BooleanSetting espLuckyChest;
    public AxisAlignedBB luckyChestBox;

    public ZombiesModule() {
        super("Zombies", Category.PLAYER);
        INSTANCE = this;
        autoShoot = new ZombiesAutoShoot(this);
        ExpandSetting esp = new ExpandSetting(this, "ESP");
        espZombies = new BooleanSetting(esp, "Zombies", true);
        espPowerUps = new BooleanSetting(esp, "Power Ups", true);
        espLuckyChest = new BooleanSetting(esp, "Lucky Chest", true);
    }

    @EventHandler public void onUpdate(PreClientTickEvent event) {
        if (!isEnabled()) return;
        luckyChestBox = null;
        if (!(Adjust.gameStateTracker.state instanceof ZombiesGameState) || mc.theWorld == null) return;
        ZombiesGameState state = (ZombiesGameState) Adjust.gameStateTracker.state;
        if (state.luckyChest == null) return;
        double closest = Double.MAX_VALUE;
        TileEntityChest chest = null;
        for (TileEntity tile : mc.theWorld.loadedTileEntityList) {
            if (tile.getClass() != TileEntityChest.class) continue;
            BlockPos pos = tile.getPos();
            double distance = state.luckyChest.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (distance < closest) { closest = distance; chest = (TileEntityChest) tile; }
        }
        if (chest == null || closest >= 25.0D) return;
        TileEntityChest adjacent = chest.adjacentChestZNeg;
        if (adjacent == null) adjacent = chest.adjacentChestXNeg;
        if (adjacent == null) adjacent = chest.adjacentChestXPos;
        if (adjacent == null) adjacent = chest.adjacentChestZPos;
        BlockPos pos = chest.getPos();
        luckyChestBox = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        if (adjacent != null) {
            BlockPos next = adjacent.getPos();
            luckyChestBox = luckyChestBox.union(new AxisAlignedBB(next, next.add(1, 1, 1)));
        }
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !(Adjust.gameStateTracker.state instanceof ZombiesGameState)) return;
        ZombiesGameState state = (ZombiesGameState) Adjust.gameStateTracker.state;
        RenderUtil.begin3d();
        RenderManager renderManager = mc.getRenderManager();
        if (espZombies.isEnabled()) {
            int count = 0;
            for (Object object : state.getPowerUps()) {
                if (++count > 512) break;
                PredictedTarget tracked = (PredictedTarget) object;
                Entity entity = tracked.getEntity();
                if (!(entity instanceof EntityLivingBase)) continue;
                EntityLivingBase living = (EntityLivingBase) entity;
                int color = autoShoot.target == living ? 0x80FFFFFF
                        : RenderUtil.rainbow(128, living.getHealth() / living.getMaxHealth(),
                                living.getHealth() / living.getMaxHealth(), 0.0F).getRGB();
                drawEntityBox(living, color, renderManager);
            }
        }
        if (espPowerUps.isEnabled()) {
            for (Object object : state.getTargets()) {
                ZombiesEntity powerUp = (ZombiesEntity) object;
                Entity entity = powerUp.getEntity();
                if (entity != null) drawEntityBox(entity, powerUp.powerUp.getColor() & 0xFFFFFF | 0x80000000, renderManager);
            }
        }
        if (espLuckyChest.isEnabled() && luckyChestBox != null) {
            GlStateManager.color(0.66666669F, 0.0F, 0.66666669F, 0.50196081F);
            RenderUtil.drawFilledBox(luckyChestBox.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ));
        }
        RenderUtil.end3d();
    }

    public void onOldOneSpawn() {
        if (mc.thePlayer == null) return;
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = getName();
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.lifetimeSteps = 36;
        notification.addText("An ", 0xFFBEBCBD).append("Old One").addText(" has spawned!", 0xFFBEBCBD).show();
    }

    public void drawEntityBox(Entity entity, int color, RenderManager renderManager) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        if (entity.getClass() == EntityItem.class) box = box.expand(0.25D, 0.25D, 0.25D);
        GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F, (color >>> 24) / 255.0F);
        RenderUtil.drawFilledBox(box.offset(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ));
    }

    @Override public String getDescription() { return "Useful mods for the Zombies minigame on Hypixel."; }
}
