package com.adjustclient.utilities.sunshine.check.player;
import java.util.List;
public enum InventoryState {
    OPEN_INVENTORY(null, false), CLOSE_INVENTORY(null, false), CLICK_SETTINGS_BUTTON(null, true),
    SWITCH_SLOT(1, true), START_SPRINT(null, false), STOP_SPRINT(0, false),
    START_SNEAK(null, false), STOP_SNEAK(0, false), SWING(0, true), ROTATE(1, false);

    InventoryState(Integer tick, boolean repeatable) {
        this.tick = tick;
        this.repeatable = repeatable;
        this.compatible = new java.util.ArrayList<>();
    }

    static {
        SWITCH_SLOT.compatible.addAll(java.util.Arrays.asList(START_SPRINT, STOP_SPRINT, START_SNEAK, STOP_SNEAK, ROTATE, SWING));
        START_SPRINT.compatible.addAll(java.util.Arrays.asList(ROTATE, SWING));
        STOP_SPRINT.compatible.addAll(java.util.Arrays.asList(ROTATE, SWING));
        START_SNEAK.compatible.addAll(java.util.Arrays.asList(START_SPRINT, STOP_SPRINT, ROTATE, SWING));
        STOP_SNEAK.compatible.addAll(java.util.Arrays.asList(START_SPRINT, STOP_SPRINT, ROTATE, SWING));
    }
    public List<InventoryState> compatible;
    public Integer tick;
    public boolean repeatable;
    public Object field28;
    @Override public String toString() { return name(); }
}
