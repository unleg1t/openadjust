package com.adjustclient.modules.impl.player.killevent.effect;
import com.adjustclient.utilities.MinecraftInstance;
import net.minecraft.entity.EntityLivingBase;
public abstract class KillEffect implements MinecraftInstance {
    public final EntityLivingBase entity;
    public boolean finished;
    protected KillEffect(EntityLivingBase entity) { this.entity = entity; }
    public abstract void play();
}
