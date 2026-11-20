package com.adjustclient.utilities.sunshine.check.raytrace;
import net.minecraft.util.*;
public class BlockRaytrace extends RaytraceResult {
    public final String type;
    public final BlockPos position;
    public final Vec3 hitVec;
    public Object field16;
    public BlockRaytrace(String type, BlockPos position, Vec3 hitVec) { this.type=type; this.position=position; this.hitVec=hitVec; }
    @Override public boolean matches(RaytraceResult other) {
        BlockRaytrace b=(BlockRaytrace)other;
        java.util.Objects.requireNonNull(b.position);
        if (!b.position.equals(position)) return false;
        return hitVec==null || b.hitVec==null || (Double.compare(hitVec.xCoord,b.hitVec.xCoord)==0
                && Double.compare(hitVec.yCoord,b.hitVec.yCoord)==0 && Double.compare(hitVec.zCoord,b.hitVec.zCoord)==0);
    }
    @Override public String toString() {
        return "BlockRaytrace{type='"+type+"', position="+position+", hitVec="
                +(hitVec==null?"null":"("+hitVec.xCoord+", "+hitVec.yCoord+", "+hitVec.zCoord+")")+"}";
    }
}
