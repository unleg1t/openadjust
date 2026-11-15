package com.adjustclient.module.impl.render;

import com.adjustclient.util.TextLine;
import com.adjustclient.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

final class DamageParticle extends TextLine {
    final String text;
    final float textX;
    Vec3 position;
    final Timer timer;
    final DamageParticlesModule module;
    final int color;

    DamageParticle(DamageParticlesModule module, String text, int color, EntityLivingBase entity) {
        this.module = module;
        this.text = text;
        this.textX = -Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) * 0.5F;
        this.timer = new Timer();
        this.color = color;
        java.util.Random random = com.adjustclient.util.RandomUtil.RANDOM;
        double x = (random.nextDouble() - 0.5) * 1.5;
        double y = (random.nextDouble() - 0.5) * 0.4;
        double z = (random.nextDouble() - 0.5) * 1.5;
        this.position = new Vec3(entity.posX + x, entity.posY + entity.getEyeHeight() - 0.2 + y, entity.posZ + z);
    }
}
