package com.adjustclient.module.impl.player.autohypixel;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoGG;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoPlay;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoPoliciesAccept;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoReportAccept;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoTip;
import com.adjustclient.module.impl.player.autohypixel.feature.BanDetect;
import com.adjustclient.module.impl.player.autohypixel.feature.ChatStats;
import com.adjustclient.module.impl.player.autohypixel.feature.bedwars.AntiObsidianMisplace;
import com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.BedWarsTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.util.anticheat.ServerTracker;

public final class AutoHypixelModule extends Module {
    public static AutoHypixelModule INSTANCE;
    public final BedWarsTracker bedWarsTracker;

    public AutoHypixelModule() {
        super("Auto Hypixel", Category.PLAYER);
        INSTANCE = this;
        setHidden(false);
        ExpandSetting bedWars = new ExpandSetting(this, "Bed Wars");
        bedWarsTracker = new BedWarsTracker(this, bedWars);
        new AntiObsidianMisplace(this, bedWars);
        new AutoPlay(this);
        new AutoGG(this);
        new BanDetect(this);
        new AutoReportAccept(this);
        new AutoPoliciesAccept(this);
        new AutoTip(this);
        new ChatStats(this);
    }

    public boolean isActive() {
        return isEnabled() && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }
}
