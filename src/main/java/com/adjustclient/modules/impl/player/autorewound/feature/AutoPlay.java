package com.adjustclient.modules.impl.player.autorewound.feature;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.PlayerKillEvent;
import com.adjustclient.core.events.RewoundGameEndedEvent;
import com.adjustclient.modules.impl.player.autorewound.AutoRewoundModule;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.Cooldown;
import java.util.ArrayList;

public final class AutoPlay extends AutoRewoundFeature {
    public static AutoPlay INSTANCE;
    public final BooleanSetting enabled;
    public final NumberSetting delay;
    public final BooleanSetting playOnDeath;
    public Cooldown requeue;

    public AutoPlay(AutoRewoundModule owner) {
        super(owner);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(owner, "Auto Play");
        enabled = new BooleanSetting(group, "Enabled", false, "Yes", "No");
        group.enabledPredicate = () -> isFeatureEnabled();
        delay = new NumberSetting(group, "Delay", 1.0F, 0.0F, 5.0F, 0.1F).formatter(NumberSetting.SECONDS);
        playOnDeath = new BooleanSetting(group, "Play on death", true, "On", "Off");
    }

    @Override
    protected boolean isFeatureEnabled() {
        return enabled.isEnabled();
    }

    @EventHandler
    public void onTick(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive() || requeue == null || !requeue.isExpired((long) (delay.getFloat() * 1000.0F))) {
            return;
        }
        Adjust.chatMessageSender.sendMessage("/requeue", false);
        requeue = null;
    }

    @EventHandler
    public void onKill(PlayerKillEvent event) {
        if (!isActive()) return;
        if (mc.getSession().getUsername().equals(event.victim) && event.finalKill && playOnDeath.isEnabled()) {
            scheduleRequeue(null);
        }
    }

    @EventHandler
    public void scheduleRequeue(RewoundGameEndedEvent event) {
        if (!isActive() || requeue != null) return;
        requeue = new Cooldown((long) (delay.getFloat() * 1000.0F));
        if (delay.getFloat() <= 0.0F) return;
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = "Auto Play";
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.addText("Sending you to a new game in ", 0xFFBEBCBD)
                .append(delay.getFormattedValue())
                .addText(".", 0xFFBEBCBD);
        notification.lifetimeSteps = Math.max(30, (int) (delay.getFloat() * 20.0F));
        notification.clearable = true;
        notification.show();
    }
}
