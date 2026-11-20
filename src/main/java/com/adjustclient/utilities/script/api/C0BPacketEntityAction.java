package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.EntityAction;
import com.adjustclient.utilities.script.api.Packet;

public final class C0BPacketEntityAction
extends Packet {
    public EntityAction action;
    public int horsePower;

    private native EntityAction $script$read$action();

    private native void $script$write$action(EntityAction var1);

    private native int $script$read$horsePower();

    private native void $script$write$horsePower(int var1);
}

