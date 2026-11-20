package com.adjustclient.modules.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.HypixelGameEndedEvent;
import com.adjustclient.modules.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.StringSetting;
import java.util.List;

public final class AutoGG extends AutoHypixelFeature {
    public static AutoGG INSTANCE;
    public final BooleanSetting enabled;
    public final StringSetting message;

    public AutoGG(AutoHypixelModule owner) {
        super(owner);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(owner, "Auto GG");
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        group.enabledPredicate = () -> enabled.isEnabled();
        message = new StringSetting(group, "Message", List.of("gg"), 1, 100, null);
    }
    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    @EventHandler public void onGameEnd(HypixelGameEndedEvent event) {
        if (!isActive()) return;
        String text = message.getValue().getFirst();
        if (!text.isEmpty()) Adjust.chatMessageSender.sendMessage("/ac " + text, false);
    }
}
