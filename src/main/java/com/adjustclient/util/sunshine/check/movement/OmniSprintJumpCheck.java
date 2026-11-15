package com.adjustclient.util.sunshine.check.movement;
import com.adjustclient.util.sunshine.*;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.*;
import com.adjustclient.util.sunshine.tracker.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import com.adjustclient.util.sunshine.CheckCategory;
public final class OmniSprintJumpCheck extends SunshineCheck implements MotionListener,OutgoingPacketListener,MovementListener,SprintListener {
    public int placementTicks;
    public Object field24;
    public OmniSprintJumpCheck() { super("Omnidirectional Sprint Jump", "Detects omnidirectional sprint jumps"); }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement p=(C08PacketPlayerBlockPlacement)packet;
            if (p.getPlacedBlockDirection()!=255 && p.getStack()!=null && p.getStack().getItem() instanceof ItemBlock) placementTicks=5;
        }
    }
    @Override public void onMovement(TrackedPosition from, TrackedPosition to) { if (placementTicks>=1) placementTicks--; }
    @Override public void onJump(TrackedPosition from, TrackedPosition to, double motion) {
        if (to.teleport) return;
        double dx=to.xCoord-from.xCoord,dz=to.zCoord-from.zCoord;
        float direction=(float)(Math.atan2(-dx,dz)*57.29577951308232);
        float fromDiff=Math.abs(MathHelper.wrapAngleTo180_float(from.rotation.yaw-direction));
        float toDiff=Math.abs(MathHelper.wrapAngleTo180_float(to.rotation.yaw-direction));
        if (Math.max(fromDiff,toDiff)<=47.0F) return;
        double speed=Math.sqrt(dx*dx+dz*dz);
        double limit=com.adjustclient.util.MovementUtil.getSpeedWithPotion(0.285,0.1);
        if (speed>limit) fail(new ViolationDetails(CheckCategory.ALERT).put("from",from).put("to",to)
                .put("direction",direction).put("fromDiff",fromDiff).put("toDiff",toDiff).put("speed",speed));
    }
    @Override public void onRotation(PositionKey from, PositionKey to) {
        java.util.Objects.requireNonNull(from); java.util.Objects.requireNonNull(to);
    }
}
