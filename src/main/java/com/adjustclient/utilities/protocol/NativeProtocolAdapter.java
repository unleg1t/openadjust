package com.adjustclient.utilities.protocol;

public class NativeProtocolAdapter extends ProtocolAdapter {
    public Object field8;

    public NativeProtocolAdapter() {
        this.interactionHandler = new com.adjustclient.utilities.protocol.interaction.NativeInteractionHandler();
    }
}
