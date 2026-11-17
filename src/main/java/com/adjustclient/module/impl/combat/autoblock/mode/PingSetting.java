package com.adjustclient.module.impl.combat.autoblock.mode;

import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.setting.SettingContainer;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.function.BooleanSupplier;

public final class PingSetting {
    public final ExpandSetting expand;
    public final ModeSetting<PingMode> mode;
    public final NumberSetting ping;

    public PingSetting(SettingContainer parent) {
        this(parent, null);
    }

    public PingSetting(SettingContainer parent, BooleanSupplier hideIf) {
        expand = new ExpandSetting(parent, "Ping", hideIf);
        mode = new ModeSetting<>(expand, "Mode", PingMode.AUTOMATIC, PingMode.values());
        ping = new NumberSetting(expand, "Ping", 50, 0, 1000, 5,
                () -> mode.getValue() != PingMode.STATIC).formatter(NumberSetting.MILLISECONDS);
    }

    public int getPing() {
        if (mode.getValue() == PingMode.AUTOMATIC) {
            var state = ServerTracker.INSTANCE.serverState;
            if (state == null || state.packetWatcher == null || state.packetWatcher.delayCount == 0) {
                return 0;
            }
            return state.packetWatcher.totalDelay / state.packetWatcher.delayCount;
        }
        return ping.getInt();
    }
}
