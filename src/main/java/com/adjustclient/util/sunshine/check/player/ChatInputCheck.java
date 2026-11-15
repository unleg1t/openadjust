package com.adjustclient.util.sunshine.check.player;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
public class ChatInputCheck extends SunshineCheck implements OutgoingPacketListener {
    public ChatInputCheck() { super("Chat Input", "Sent a chat message while doing another action."); }
    @Override public void onOutgoingPacket(Packet<?> packet) { }
}
