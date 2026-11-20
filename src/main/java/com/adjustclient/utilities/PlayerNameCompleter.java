package com.adjustclient.utilities;

import java.util.ArrayList;
import java.util.List;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;

public final class PlayerNameCompleter {
    public static String[] complete(String prefix, boolean ignored) {
        List<String> names = ServerTracker.INSTANCE.playerTracker.players.values().stream()
                .map(player -> player.info.getGameProfile().getName())
                .toList();
        List<String> matches = new ArrayList<>();
        for (String name : names) {
            if (StreamerModule.nameMap != null)
                name = net.minecraft.util.StringUtils.stripControlCodes(StreamerModule.replaceNames(name));
            if (prefix == null || name.toLowerCase().startsWith(prefix.toLowerCase())) matches.add(name);
        }
        return matches.toArray(new String[0]);
    }
}
