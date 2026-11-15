package com.adjustclient.script.engine.event;

import com.adjustclient.event.ItemSlowdownEvent;

public final class ItemSlowdownEventType extends ScriptEventType<ItemSlowdownEvent> {
    public static final ItemSlowdownEventType INSTANCE;

    static {
        INSTANCE = new ItemSlowdownEventType();
    }

    private ItemSlowdownEventType() {
        super("ItemSlowdownEvent", null, ItemSlowdownEvent.class);
    }
}
