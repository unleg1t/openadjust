package com.adjustclient.utilities.game.render;

import com.adjustclient.core.events.DebugCoordinatesEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Timer;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;

public class PositionSnapper implements Listener, MinecraftInstance {
    public DoubleOpenHashSet fractions = new DoubleOpenHashSet();
    public Timer timer = new Timer();
    public Double lastPosition;
    public Object field16;

    @EventHandler public void onMotion(DebugCoordinatesEvent event) {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.onGround) lastPosition = null;
        boolean matched = false;
        for (double fraction : fractions) {
            if ((double) (int) Math.round(event.y % 0.0625D * 100000.0D) / 100000.0D
                    == (double) (int) Math.round(fraction * 100000.0D) / 100000.0D) {
                lastPosition = event.y;
                double step = fraction % 0.015625D == 0.0D ? 0.0625D : 0.015625D;
                event.y = (int) Math.round(event.y / step) * step;
                matched = true;
                break;
            }
        }
        if (timer.hasElapsed(1000L)) fractions.clear();
        if (matched || mc.thePlayer.onGround || lastPosition == null) return;
        if (lastPosition <= mc.thePlayer.posY) event.y -= lastPosition % 0.015625D;
        else lastPosition = null;
    }
}
