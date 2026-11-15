package com.adjustclient.setting;

import com.adjustclient.util.Timer;

public final class DelaySetting {
    public final ModeSetting<DelayType> delayType;
    public final NumberSetting ticks;
    public final NumberSetting time;
    private final Timer timer;
    public int elapsedTicks;
    public final Runnable resetAction;

    public DelaySetting(SettingContainer parent) {
        delayType = new ModeSetting<>(parent, "Delay Type", DelayType.TIME, DelayType.values());
        timer = new Timer();
        time = new NumberSetting(parent, "Time", 350, 50, 5000, 50,
                () -> delayType.getValue() != DelayType.TIME);
        time.setDescription("Count by time passed in milliseconds.");
        ticks = new NumberSetting(parent, "Ticks", 7, 1, 100, 1,
                () -> delayType.getValue() != DelayType.TICKS);
        ticks.setDescription("Count by player packets sent.");
        resetAction = () -> { timer.reset(); elapsedTicks = 0; };
    }

    public boolean hasElapsed() {
        return delayType.getValue() == DelayType.TICKS
                ? ticks.getValue().intValue() <= elapsedTicks
                : timer.lastMs + time.getValue().longValue() <= System.currentTimeMillis();
    }

    enum DelayType implements ModeOption {
        TIME("Time"), TICKS("Ticks");
        private final String name;
        private Object field20;
        DelayType(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
