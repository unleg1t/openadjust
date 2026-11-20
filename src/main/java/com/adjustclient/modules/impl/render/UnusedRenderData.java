package com.adjustclient.modules.impl.render;

import com.adjustclient.utilities.Timer;
import net.minecraft.entity.player.EntityPlayer;

final class UnusedRenderData {
    final EntityPlayer entity;
    final Timer timer = new Timer();
    UnusedRenderData(EntityPlayer entity) { this.entity = entity; }
}
