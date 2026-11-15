package com.adjustclient.module.util;

import com.adjustclient.util.Timer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CpsCounter {
    public final List<Click> clicks = new ArrayList<>();
    private Object field8;

    public enum Button {
        LEFT, RIGHT;
        private Object field16;
    }

    static final class Click {
        final Timer timer = new Timer();
        private Object field8;
    }

    public void recordClick() { clicks.add(new Click()); }

    public boolean canClick(CpsSettings settings) {
        if (settings.clickDelay == -1) settings.rollClickDelay();
        return canClick(settings.clickDelay, Button.LEFT, 0);
    }

    public boolean canClick(int delayMs, Button button, int offsetMs) {
        Click latest = null;
        int count = 0;
        for (Iterator<Click> iterator = clicks.iterator(); iterator.hasNext();) {
            Click click = iterator.next();
            if (click.timer.hasElapsed(1000 - offsetMs)) iterator.remove();
            else { latest = click; count++; }
        }
        int rate = Math.max(1, Math.min(20, delayMs - count / 11));
        int interval = 920 / rate;
        if (interval < 53 || latest == null) return true;
        return latest.timer.hasElapsed(interval - offsetMs);
    }
}
