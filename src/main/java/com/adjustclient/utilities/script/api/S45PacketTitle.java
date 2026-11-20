package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.ChatComponent;
import com.adjustclient.utilities.script.api.Packet;
import com.adjustclient.utilities.script.api.TitleType;

public final class S45PacketTitle
extends Packet {
    public TitleType type;
    public ChatComponent message;
    public int fadeInTime;
    public int displayTime;
    public int fadeOutTime;

    public S45PacketTitle() {
    }

    public S45PacketTitle(TitleType type, ChatComponent message) {
    }

    public S45PacketTitle(int fadeInTime, int displayTime, int fadeOutTime) {
    }

    public S45PacketTitle(TitleType type, ChatComponent message, int fadeInTime, int displayTime, int fadeOutTime) {
    }

    private native TitleType $script$read$type();

    private native void $script$write$type(TitleType var1);

    private native ChatComponent $script$read$message();

    private native void $script$write$message(ChatComponent var1);

    private native int $script$read$fadeInTime();

    private native void $script$write$fadeInTime(int var1);

    private native int $script$read$displayTime();

    private native void $script$write$displayTime(int var1);

    private native int $script$read$fadeOutTime();

    private native void $script$write$fadeOutTime(int var1);
}

