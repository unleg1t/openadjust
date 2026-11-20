package com.adjustclient.utilities.script.engine.event;

import com.adjustclient.core.events.DirectionalPacketEvent;

public final class PacketEventType extends ScriptEventType<DirectionalPacketEvent> {
    public static final PacketEventType INSTANCE;

    static {
        INSTANCE = new PacketEventType();
    }

    private PacketEventType() {
        super("PacketEvent", CANCELLABLE_EVENT_TYPE, DirectionalPacketEvent.class);
    }
}
