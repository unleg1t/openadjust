package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Entity;
import com.adjustclient.utilities.script.api.Packet;
import com.adjustclient.utilities.script.api.UseEntityAction;
import com.adjustclient.utilities.script.api.Vec3;

public final class C02PacketUseEntity
extends Packet {
    public int entityId;
    public UseEntityAction action;
    public Vec3 hitVec;

    public C02PacketUseEntity() {
    }

    public C02PacketUseEntity(Entity entity, UseEntityAction action) {
    }

    public C02PacketUseEntity(Entity entity, Vec3 hitVec) {
    }

    private native int $script$read$entityId();

    private native void $script$write$entityId(int var1);

    private native UseEntityAction $script$read$action();

    private native void $script$write$action(UseEntityAction var1);

    private native Vec3 $script$read$hitVec();

    private native void $script$write$hitVec(Vec3 var1);
}

