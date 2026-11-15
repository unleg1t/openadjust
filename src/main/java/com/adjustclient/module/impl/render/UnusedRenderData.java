package com.adjustclient.module.impl.render;

import com.adjustclient.util.Timer;
import net.minecraft.entity.player.EntityPlayer;

final class UnusedRenderData {
    final EntityPlayer entity;
    final Timer timer = new Timer();
    UnusedRenderData(EntityPlayer entity) { this.entity = entity; }
}
