package com.adjustclient.module.impl.combat.criticals;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.module.impl.combat.criticals.mode.PacketCriticalsMode;
import com.adjustclient.setting.*;

public final class CriticalsModule extends Module {
    public static CriticalsModule INSTANCE;
    public final ModeSetting<CriticalsMode> mode;
    public final NumberSetting delay;
    public int delayTicks;
    public CriticalsModule() {
        super("Criticals", Category.COMBAT);
        INSTANCE = this;
        PacketCriticalsMode packet = new PacketCriticalsMode(this);
        mode = new ModeSetting<>(this, "Mode", packet, packet);
        delay = new NumberSetting(this, "Delay", 15, 1, 20, 1).formatter(NumberSetting.TICKS);
    }
    @Override public void onEnable() { delayTicks = 0; }
    @Override public String getSuffix() { return mode.getValue().getName() + " " + delay.getInt(); }
    public void resetDelay() { delayTicks = delay.getInt(); }

    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled()) return;
        if (!event.pre && delayTicks >= 1) --delayTicks;
    }
}
