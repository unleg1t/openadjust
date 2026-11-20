package com.adjustclient.modules.impl.player.autohypixel.feature.chatstats;

import net.minecraft.util.IChatComponent;

public interface ChatStatsProvider {
    void showStats(String name, IChatComponent displayName);
}
