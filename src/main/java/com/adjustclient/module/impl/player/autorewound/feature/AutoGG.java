package com.adjustclient.module.impl.player.autorewound.feature;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.RewoundGameEndedEvent;
import com.adjustclient.module.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.StringSetting;
import java.util.List;

public final class AutoGG extends AutoRewoundFeature {
    public static AutoGG INSTANCE;
    public final BooleanSetting enabled;
    public final StringSetting message;

    public AutoGG(AutoRewoundModule owner) {
        super(owner);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(owner, "Auto GG");
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        group.enabledPredicate = () -> enabled.isEnabled();
        message = new StringSetting(group, "Message", List.of("gg"), 1, 100, null);
    }

    @Override
    protected boolean isFeatureEnabled() {
        return enabled.isEnabled();
    }

    @EventHandler
    public void onGameEnd(RewoundGameEndedEvent event) {
        if (!isActive()) return;
        String text = message.getValue().getFirst();
        if (!text.isEmpty()) {
            Adjust.chatMessageSender.sendMessage("/ac " + text, false);
        }
    }
}
