package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.ItemSlowdownEvent;

public final class ItemSlowdownEventType extends ScriptEventType<ItemSlowdownEvent> {
    public static final ItemSlowdownEventType INSTANCE;

    static {
        INSTANCE = new ItemSlowdownEventType();
    }

    private ItemSlowdownEventType() {
        super("ItemSlowdownEvent", null, ItemSlowdownEvent.class);
    }
}
