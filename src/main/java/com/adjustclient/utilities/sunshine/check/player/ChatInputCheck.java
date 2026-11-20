package com.adjustclient.utilities.sunshine.check.player;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
public class ChatInputCheck extends SunshineCheck implements OutgoingPacketListener {
    public ChatInputCheck() { super("Chat Input", "Sent a chat message while doing another action."); }
    @Override public void onOutgoingPacket(Packet<?> packet) { }
}
