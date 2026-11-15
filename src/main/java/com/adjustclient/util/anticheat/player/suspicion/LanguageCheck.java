package com.adjustclient.util.anticheat.player.suspicion;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.hypixel.api.model.player.HypixelLanguage;
import com.adjustclient.ui.hud.notification.NotificationBuilder;

public final class LanguageCheck extends SuspicionCheck {
    public Object field4;
    @Override public boolean applies() {
        return ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }
    @Override public double score(PlayerEntry player, double score) {
        HypixelPlayer data = player.hypixelStats;
        if (data == null) return score;
        try {
            HypixelLanguage language = data.getUserLanguage();
            if (language == HypixelLanguage.ENGLISH) return score;
            if (language == HypixelLanguage.CHINESE_SIMPLIFIED) return score * 1.25D;
            if (language == HypixelLanguage.CHINESE_TRADITIONAL) return score * 1.25D;
            return score * 0.96D;
        } catch (IllegalArgumentException unknown) {
            score *= 0.96D;
            if (!data.languageWarned) {
                NotificationBuilder notification = new NotificationBuilder("Warning");
                notification.lifetimeSteps = 400;
                notification.addText("An unknown language ", 0xFFBEBCBD)
                        .append(data.userLanguage)
                        .addText(" was just received, if you got this notification please report it to Adjust developers.", 0xFFBEBCBD)
                        .show();
                data.languageWarned = true;
            }
        }
        return score;
    }
}
