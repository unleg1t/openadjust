package com.adjustclient.utilities.sunshine.check.packet;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import java.util.*;
public final class InvalidPlayerPacketCheck extends SunshineCheck implements OutgoingPacketListener {
    public InvalidPlayerPacketCheck() { super("Invalid Player Packet", "Sent impossible numbers in player packet"); }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (!(packet instanceof C03PacketPlayer)) return;
        C03PacketPlayer p=(C03PacketPlayer)packet;
        List<String> invalid=new ArrayList<>();
        if (p.isMoving()) {
            checkNumber("x",invalid,p.getPositionX());
            checkNumber("y",invalid,p.getPositionY());
            checkNumber("z",invalid,p.getPositionZ());
        }
        if (p.getRotating()) {
            checkNumber("yaw",invalid,p.getYaw());
            checkNumber("pitch",invalid,p.getPitch());
        }
    }
    public void checkNumber(String name,List<String> invalid,float value) {
        if (Float.isNaN(value)) invalid.add("NaN_"+name);
        if (Math.abs(value)>Float.MAX_VALUE) invalid.add("infinite_"+name);
    }
    public void checkNumber(String name,List<String> invalid,double value) {
        if (Double.isNaN(value)) invalid.add("NaN_"+name);
        if (Math.abs(value)>Double.MAX_VALUE) invalid.add("infinite_"+name);
    }
}
