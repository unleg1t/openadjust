package com.adjustclient.protocol;

public class NativeProtocolAdapter extends ProtocolAdapter {
    public Object field8;

    public NativeProtocolAdapter() {
        this.interactionHandler = new com.adjustclient.protocol.interaction.NativeInteractionHandler();
    }
}
