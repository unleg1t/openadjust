package com.adjustclient.module.impl.player.voicechat.playback.realistic;

import com.adjustclient.audio.filter.impl.EchoFilter;
import com.adjustclient.audio.filter.impl.LowPassFilter;
import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo;
import com.adjustclient.module.impl.player.voicechat.playback.VoiceFilterChain;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class RealisticSoundProcessor extends VoiceFilterChain implements MinecraftInstance {
    public final EntityPlayer entity;
    public final LowPassFilter occlusion = new LowPassFilter();
    public final EchoFilter echo = new EchoFilter();

    public RealisticSoundProcessor(EntityPlayer entity, VoiceUserInfo userInfo) {
        super(userInfo);
        this.entity = entity;
        filters.add(echo);
        filters.add(occlusion);
        echo.delaySamples = 8000;
        echo.echoCount = 15;
        occlusion.coefficient = 50.0;
    }

    @Override
    public void update() {
        super.update();
        int x = MathHelper.floor_double(entity.posX);
        int y = MathHelper.floor_double(entity.posY);
        int z = MathHelper.floor_double(entity.posZ);
        if (!VoiceChatModule.INSTANCE.settings.realisticEnabled.getAsBoolean()) {
            if (echo.strength != 0.0f) {
                echo.previousStrength = echo.strength;
                echo.strength = 0.0f;
                echo.fadeStart = System.currentTimeMillis();
                echo.fadeDuration = 0L;
            }
            if (occlusion.strength != 0.0f) {
                occlusion.previousStrength = occlusion.strength;
                occlusion.strength = 0.0f;
                occlusion.fadeStart = System.currentTimeMillis();
                occlusion.fadeDuration = 0L;
            }
            return;
        }
        World world = mc.theWorld;
        if (y < 64 && VoiceChatModule.INSTANCE.settings.echo.getAsBoolean()) {
            if (world == null) {
                if (echo.strength != 0.0f) {
                    echo.previousStrength = echo.strength;
                    echo.strength = 0.0f;
                    echo.fadeStart = System.currentTimeMillis();
                    echo.fadeDuration = 0L;
                }
            } else {
                Vec3 eye = mc.thePlayer.getPositionEyes(1.0f);
                BlockPos eyePos = new BlockPos(eye.xCoord, eye.yCoord, eye.zCoord);
                int sky = (world.getCombinedLight(eyePos, 0) >> 20) & 15;
                float target = 0.0f;
                if (Math.max(sky, (world.getCombinedLight(eyePos, 0) >> 20) & 15) == 0
                        && occlusion.getStrength() <= 0.0f) {
                    int air = 0;
                    int solid = 0;
                    for (int scanY = y; scanY < 64; scanY++) {
                        Block block = world.getBlockState(new BlockPos(x, scanY, z)).getBlock();
                        if (block != Blocks.air) {
                            if (solid >= 10) break;
                            solid++;
                        } else {
                            if (solid >= 1 || air >= 33) break;
                            air++;
                        }
                    }
                    double distance = Math.sqrt((eye.xCoord - x) * (eye.xCoord - x)
                            + (eye.yCoord - y) * (eye.yCoord - y) + (eye.zCoord - z) * (eye.zCoord - z));
                    target = (float) Math.max(0.0, Math.min(1.0, 1.0 - distance / 50.0)) * (solid / 10.0f);
                }
                if (echo.strength != target) {
                    echo.previousStrength = echo.strength;
                    echo.strength = target;
                    echo.fadeStart = System.currentTimeMillis();
                    echo.fadeDuration = 5000L;
                }
            }
        } else if (echo.strength != 0.0f) {
            echo.previousStrength = echo.strength;
            echo.strength = 0.0f;
            echo.fadeStart = System.currentTimeMillis();
            echo.fadeDuration = 5000L;
        }
        updateOcclusion(world, x, y, z);
    }

    public boolean hasOpening(World world, double startX, double startY, double startZ,
                              double endX, double endY, double endZ) {
        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        int steps = (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz));
        for (int i = 0; i < steps; i++) {
            BlockPos pos = new BlockPos(MathHelper.floor_double(startX + i * dx / steps),
                    MathHelper.floor_double(startY + i * dy / steps), MathHelper.floor_double(startZ + i * dz / steps));
            Block block = BlockUtil.getBlock(pos);
            if (block == null || !block.isFullBlock()) return true;
        }
        return false;
    }

    public void updateOcclusion(World world, int x, int y, int z) {
        if (world == null || !VoiceChatModule.INSTANCE.settings.muffle.getAsBoolean()) return;
        EntityPlayer listener = mc.thePlayer;
        if (listener == null) {
            if (occlusion.strength != 0.0f) {
                occlusion.previousStrength = occlusion.strength;
                occlusion.strength = 0.0f;
                occlusion.fadeStart = System.currentTimeMillis();
                occlusion.fadeDuration = 0L;
            }
            return;
        }
        double eyeX = listener.posX;
        double eyeY = listener.posY + listener.getEyeHeight();
        double eyeZ = listener.posZ;
        BlockPos listenerPos = new BlockPos(eyeX, eyeY, eyeZ);
        if (world.getBlockState(new BlockPos(x, y + 1, z)).getBlock().getMaterial().isLiquid()
                || world.getBlockState(listenerPos.offset(EnumFacing.UP)).getBlock().getMaterial().isLiquid()) {
            if (occlusion.strength != 1.0f) {
                occlusion.previousStrength = occlusion.strength;
                occlusion.strength = 1.0f;
                occlusion.fadeStart = System.currentTimeMillis();
                occlusion.fadeDuration = 0L;
            }
            if (echo.strength != 0.0f) {
                echo.previousStrength = echo.strength;
                echo.strength = 0.0f;
                echo.fadeStart = System.currentTimeMillis();
                echo.fadeDuration = 0L;
            }
            return;
        }
        if (((world.getCombinedLight(new BlockPos(x, y, z), 0) >> 20) & 15) >= 11
                && ((world.getCombinedLight(listenerPos, 0) >> 20) & 15) >= 11) {
            if (occlusion.strength != 0.0f) {
                occlusion.previousStrength = occlusion.strength;
                occlusion.strength = 0.0f;
                occlusion.fadeStart = System.currentTimeMillis();
                occlusion.fadeDuration = 0L;
            }
            return;
        }
        boolean enclosed = true;
        outer: for (int dx = -2; dx <= 2; dx += 2) {
            for (int dy = 0; dy <= 2; dy += 2) {
                for (int dz = -2; dz <= 2; dz += 2) {
                    if (!hasOpening(world, x + dx, y + dy, z + dz, eyeX + dx, eyeY + dy, eyeZ + dz)) {
                        enclosed = false;
                        break outer;
                    }
                }
            }
        }
        float target = enclosed ? 1.0f : 0.0f;
        if (occlusion.strength != target) {
            occlusion.previousStrength = occlusion.strength;
            occlusion.strength = target;
            occlusion.fadeStart = System.currentTimeMillis();
            occlusion.fadeDuration = 1000L;
        }
    }
}
