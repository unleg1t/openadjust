package com.adjustclient.util.sunshine.check.interact;
import com.adjustclient.util.sunshine.*;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import com.adjustclient.util.sunshine.CheckCategory;
import net.minecraft.util.*;
public class BlockPlaceCheck extends SunshineCheck implements OutgoingPacketListener {
    public BlockPlaceCheck() { super("Placement", "No description provided"); }
    public void checkVector(Vec3 vector) {
        if (!(vector.xCoord<=1.0 && vector.xCoord>=0.0))
            fail(new ViolationDetails(CheckCategory.KICK,"Hit vector out of bounds").put("vector",vector));
        if (!(vector.yCoord<=1.0 && vector.yCoord>=0.0))
            fail(new ViolationDetails(CheckCategory.KICK,"Hit vector out of bounds").put("vector",vector));
        if (vector.zCoord>1.0 || vector.zCoord<0.0)
            fail(new ViolationDetails(CheckCategory.KICK,"Hit vector out of bounds").put("vector",vector));
    }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (!(packet instanceof C08PacketPlayerBlockPlacement)) return;
        C08PacketPlayerBlockPlacement p=(C08PacketPlayerBlockPlacement)packet;
        Vec3 vector=new Vec3(p.getPlacedBlockOffsetX(),p.getPlacedBlockOffsetY(),p.getPlacedBlockOffsetZ());
        checkVector(vector);
        int facing=p.getPlacedBlockDirection();
        if (facing==255) {
            if (vector.xCoord!=0.0 || vector.yCoord!=0.0 || vector.zCoord!=0.0)
                fail(new ViolationDetails(CheckCategory.KICK,"Incorrect item use cursor").put("vector",vector));
        } else if (facing<0 || facing>=EnumFacing.values().length || EnumFacing.values()[facing]==null)
            fail(new ViolationDetails(CheckCategory.KICK,"Incorrect facing").put("direction",facing));
    }
}
