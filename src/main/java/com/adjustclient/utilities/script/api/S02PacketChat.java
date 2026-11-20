package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.ChatComponent;
import com.adjustclient.utilities.script.api.Packet;

public final class S02PacketChat
extends Packet {
    public ChatComponent chatComponent;
    public byte type;

    public S02PacketChat() {
    }

    public S02PacketChat(ChatComponent component) {
    }

    public S02PacketChat(ChatComponent message, byte typeIn) {
    }

    private native ChatComponent $script$read$chatComponent();

    private native void $script$write$chatComponent(ChatComponent var1);

    private native byte $script$read$type();

    private native void $script$write$type(byte var1);
}

