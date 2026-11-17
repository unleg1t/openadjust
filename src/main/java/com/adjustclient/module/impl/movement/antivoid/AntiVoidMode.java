package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.SimulationPlayer;
import net.minecraft.block.Block;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

public abstract class AntiVoidMode implements Listener, MinecraftInstance, ModeOption {
    protected final AntiVoidModule module;
    protected final String name;
    protected float collisionWidth = 0.5F;

    protected AntiVoidMode(AntiVoidModule module, String name) {
        this.module = module;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isActive() {
        return module.isEnabled() && module.mode.getValue() == this;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    protected boolean isSafeEnvironment(EntityPlayer player) {
        if (player.onGround && player.isSneaking()) return true;
        Block block = player.worldObj.getBlockState(new BlockPos(player.posX,
                player.getEntityBoundingBox().minY, player.posZ)).getBlock();
        if ((block == Blocks.ladder || block == Blocks.vine)
                && (player instanceof SimulationPlayer || !((AbstractClientPlayer) player).isSpectator())) return true;
        return player.inWater || player.ridingEntity != null || player.noClip;
    }
}
