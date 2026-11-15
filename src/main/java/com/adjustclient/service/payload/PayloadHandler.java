package com.adjustclient.service.payload;

import com.adjustclient.user.ClientUser;
import net.minecraft.network.PacketBuffer;

public interface PayloadHandler {
    boolean handle(ClientUser sender);
    void write(PacketBuffer buffer);
    void read(PacketBuffer buffer);
}
