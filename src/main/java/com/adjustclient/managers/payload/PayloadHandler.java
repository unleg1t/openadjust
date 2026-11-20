package com.adjustclient.managers.payload;

import com.adjustclient.managers.user.ClientUser;
import net.minecraft.network.PacketBuffer;

public interface PayloadHandler {
    boolean handle(ClientUser sender);
    void write(PacketBuffer buffer);
    void read(PacketBuffer buffer);
}
