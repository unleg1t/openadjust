package com.adjustclient.modules.impl.player;

import com.adjustclient.utilities.anticheat.AntiCheat;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.Collection;

public final class AntiCheatModule extends Module {
    public static AntiCheatModule INSTANCE;
    public static boolean restrictionsEnabled;
    public final BooleanSetting notifications;

    public AntiCheatModule() {
        super("Anti Cheat", Category.PLAYER);
        INSTANCE = this;
        notifications = new BooleanSetting(this, "Notifications", true);
        notifications.setDescription("Receive notifications for blocked features");
        flags |= 2;
        savedFlags |= 2;
        flags &= ~4;
        savedFlags &= ~4;
        restrictionsEnabled = true;
    }
    public boolean shouldNotifyBlockedFeatures() {
        return restrictionsEnabled && notifications.isEnabled() && !notifications.isHidden();
    }
    @Override public void onEnable() { restrictionsEnabled = true; }
    @Override public void onDisable() { restrictionsEnabled = false; }
    @Override public String getSuffix() {
        var state = ServerTracker.INSTANCE.serverState;
        if (state == null || state.featureBlocker == null) return null;
        Collection<AntiCheat> detected = state.featureBlocker.antiCheats.values();
        if (detected.isEmpty()) return null;
        StringBuilder result = new StringBuilder();
        int index = 0;
        for (AntiCheat antiCheat : detected) {
            result.append(antiCheat.name);
            if (index++ != detected.size() - 1) result.append(", ");
        }
        return result.toString();
    }
    @Override public String getDescription() {
        return "Automatically detects the anti-cheat running and blocks Adjust features that would not bypass";
    }
}
