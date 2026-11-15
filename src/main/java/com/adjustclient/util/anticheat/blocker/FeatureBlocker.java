package com.adjustclient.util.anticheat.blocker;
import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.anticheat.feature.ModuleFeature;
import com.adjustclient.anticheat.impl.FeatureBlockState;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.SoundUtil;
import com.adjustclient.util.Timer;
import java.util.*;
import java.util.stream.*;
public final class FeatureBlocker {
    public Map<Class<? extends AntiCheat>, AntiCheat> antiCheats = new HashMap<>();
    public LinkedHashMap<Feature, FeatureBlockState> pending = new LinkedHashMap<>();
    public Timer reportTimer = new Timer();
    public Timer notificationTimer = new Timer(0L);
    public Object field20;
    public <T extends AntiCheat> T getAntiCheat(Class<T> type) { return type.cast(antiCheats.get(type)); }
    public void flushNotifications() {
        if (pending.isEmpty()) return;
        long delay = notificationTimer.hasElapsed(15000L) ? 1500L : 7500L;
        if (!reportTimer.hasElapsed(delay)) return;
        if (com.adjustclient.module.impl.player.AntiCheatModule.INSTANCE.shouldNotifyBlockedFeatures()) {
            String names = String.join(", ", pending.values().stream()
                    .flatMap(state -> streamBlocked(state))
                    .map(antiCheat -> antiCheat.name)
                    .collect(Collectors.toSet()));
            NotificationBuilder notification = new NotificationBuilder("Anti-Cheat: " + names);
            notification.key = "autoAntiCheatAlert";
            notification.addText(pending.size() >= 2 ? "Several features were automatically blocked: " : "Feature ", (int) 4290690237L);
            int count = pending.size(), i = 0;
            for (Feature feature : pending.keySet()) {
                if (feature instanceof ModuleFeature) {
                    var module = ((ModuleFeature) feature).getModule();
                    notification.append(module == null ? "null module" : module.getName());
                    if (feature.name != null) notification.addText(": ", (int) 4290690237L);
                }
                if (feature.name != null) notification.append(feature.name);
                if (i != count - 1) notification.addText(i == count - 2 ? " and " : ", ", (int) 4290690237L);
                i++;
            }
            notification.addText(count == 1 ? " was automatically blocked." : ".", (int) 4290690237L);
            notification.lifetimeSteps = Integer.valueOf(28 * count + 45);
            notification.show();
            SoundUtil.play("notification.error", 1.0F, 1.5F);
        }
        pending.clear();
        notificationTimer.reset();
    }
    public static Stream<AntiCheat> streamBlocked(FeatureBlockState state) {
        return state.blockingAntiCheats.stream();
    }
    public void reportBlocked(AntiCheat antiCheat, Feature feature) {
        FeatureBlockState existing = pending.get(feature);
        if (existing != null) {
            existing.lastReportTimer.reset();
            return;
        }
        FeatureBlockState state = new FeatureBlockState(feature);
        pending.put(feature, state);
        reportTimer.reset();
        state.blockingAntiCheats.add(antiCheat);
        state.lastReportTimer.reset();
    }
}
