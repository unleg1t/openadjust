package com.adjustclient.script.engine.event;

import com.adjustclient.event.DirectionalPacketEvent;

public final class PacketEventType extends ScriptEventType<DirectionalPacketEvent> {
    public static final PacketEventType INSTANCE;

    static {
        INSTANCE = new PacketEventType();
    }

    private PacketEventType() {
        super("PacketEvent", CANCELLABLE_EVENT_TYPE, DirectionalPacketEvent.class);
    }
}
