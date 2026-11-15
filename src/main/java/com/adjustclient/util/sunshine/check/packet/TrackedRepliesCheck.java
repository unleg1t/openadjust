package com.adjustclient.util.sunshine.check.packet;
import com.adjustclient.util.sunshine.*;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import java.util.ArrayDeque;
import com.adjustclient.util.sunshine.CheckCategory;
public class TrackedRepliesCheck extends SunshineCheck implements IncomingPacketListener, OutgoingPacketListener {
    public final ArrayDeque<ExpectedReply> expected = new ArrayDeque<>();
    public Object field24;
    public TrackedRepliesCheck() { super("Tracked Replies", "Tracks your replies to packets (transactions, teleports)"); }
    public boolean containsTeleport(ExpectedTeleport teleport) {
        for (ExpectedReply reply : expected) if (reply instanceof ExpectedTeleport && reply.matches(teleport)) return true;
        return false;
    }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        ExpectedReply sent;
        if (packet instanceof C0FPacketConfirmTransaction) {
            C0FPacketConfirmTransaction p=(C0FPacketConfirmTransaction)packet;
            sent=new ExpectedTransaction(p.getWindowId(),p.getUid(),p.accepted);
        } else if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer p=(C03PacketPlayer)packet;
            ExpectedTeleport t=new ExpectedTeleport(p.getPositionX(),p.getPositionY(),p.getPositionZ(),p.getYaw(),p.getPitch(),false);
            if (!containsTeleport(t)) return;
            sent=t;
        } else return;
        ExpectedReply next=expected.pollFirst();
        if (next==null || !next.matches(sent)) {
            fail(new ViolationDetails(CheckCategory.ALERT).put("expected",next).put("sent",sent));
            expected.clear();
        }
    }
    @Override public void onIncomingPacket(Packet<?> packet) {
        if (packet instanceof S32PacketConfirmTransaction) {
            S32PacketConfirmTransaction p=(S32PacketConfirmTransaction)packet;
            if (!p.func_148888_e()) expected.addLast(new ExpectedTransaction(p.getWindowId(),p.getActionNumber(),true));
        } else if (packet instanceof S08PacketPlayerPosLook) {
            S08PacketPlayerPosLook p=(S08PacketPlayerPosLook)packet;
            if (p.func_179834_f().isEmpty()) expected.addLast(new ExpectedTeleport(p.getX(),p.getY(),p.getZ(),p.getYaw()%360.0F,p.getPitch(),false));
        }
    }
}
