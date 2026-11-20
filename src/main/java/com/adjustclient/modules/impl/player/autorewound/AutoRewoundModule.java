package com.adjustclient.modules.impl.player.autorewound;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.rewound.RewoundGame;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.player.autorewound.feature.AutoGG;
import com.adjustclient.modules.impl.player.autorewound.feature.AutoPlay;
import com.adjustclient.modules.impl.player.autorewound.feature.PitFeature;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.gui.hud.toggled.ToggledList;
import com.adjustclient.utilities.anticheat.ServerTracker;

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
