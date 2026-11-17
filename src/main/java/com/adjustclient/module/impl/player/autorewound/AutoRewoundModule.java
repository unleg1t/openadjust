package com.adjustclient.module.impl.player.autorewound;

import com.adjustclient.Adjust;
import com.adjustclient.game.rewound.RewoundGame;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.autorewound.feature.AutoGG;
import com.adjustclient.module.impl.player.autorewound.feature.AutoPlay;
import com.adjustclient.module.impl.player.autorewound.feature.PitFeature;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.server.ServerType;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.ui.hud.toggled.ToggledList;
import com.adjustclient.util.anticheat.ServerTracker;

public final class AutoRewoundModule extends Module {
    public static AutoRewoundModule INSTANCE;
    public final BooleanSetting showModulesAsHypixel;
    public final ModeSetting<HideBotsMode> hideBots;

    public AutoRewoundModule() {
        super("Auto Rewound", Category.PLAYER);
        INSTANCE = this;
        setHidden(false);
        new AutoGG(this);
        new AutoPlay(this);
        new PitFeature(this);
        showModulesAsHypixel = new BooleanSetting(this, "Show modules as Hypixel", false, "On", "Off");
        showModulesAsHypixel.setDescription("Gives rewound modules a Hypixel suffix on Rewound server.");
        showModulesAsHypixel.addListener(() -> {
            if (ToggledList.INSTANCE != null) {
                ToggledList.INSTANCE.refresh();
            }
        });
        hideBots = new ModeSetting<>(this, "Hide bots", HideBotsMode.NEVER, HideBotsMode.values());
        hideBots.setDescription("Hides bots in the player list HUD widget.");
        hideBots.addListener(() -> {
            if (HUDModule.INSTANCE_ALT != null && HUDModule.INSTANCE_ALT.playerList != null) {
                HUDModule.INSTANCE_ALT.playerList.needsRebuild = true;
            }
        });
    }

    public boolean isActive() {
        return isEnabled()
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.REWOUND;
    }

    public boolean shouldHideBots() {
        if (!isActive()) return false;
        return switch (hideBots.getValue()) {
            case NEVER -> false;
            case ALWAYS -> true;
            case PIT_ONLY -> Adjust.rewoundGameTracker.state.game == RewoundGame.PIT;
        };
    }
}
