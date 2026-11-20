package com.adjustclient.utilities.sunshine.check.raytrace;
import net.minecraft.util.Vec3;
public class EntityRaytrace extends RaytraceResult {
    public final Vec3 hitVec;
    public final int entityId;
    public Object field12;
    public EntityRaytrace(int entityId, Vec3 hitVec) { this.entityId=entityId; this.hitVec=hitVec; }
    @Override public boolean matches(RaytraceResult other) {
        EntityRaytrace e=(EntityRaytrace)other;
        if (entityId!=e.entityId) return false;
        return hitVec==null || e.hitVec==null || (Double.compare(hitVec.xCoord,e.hitVec.xCoord)==0
                && Double.compare(hitVec.yCoord,e.hitVec.yCoord)==0 && Double.compare(hitVec.zCoord,e.hitVec.zCoord)==0);
    }
    @Override public String toString() {
        return "Entity{id="+entityId+", hitVec="+(hitVec==null?"null":"("+hitVec.xCoord+", "+hitVec.yCoord+", "+hitVec.zCoord+")")+"}";
    }
}
