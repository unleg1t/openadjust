package com.adjustclient.service.payload;

import com.adjustclient.Adjust;
import com.adjustclient.game.player.LookTargetHandler;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.ChatUtil;
import com.adjustclient.util.DistanceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Vec3;

public final class SetTargetPayload implements PayloadHandler {
    public Vec3 position;
    public float speed;
    public int entityId;
    public int duration;
    public Object field20;

    public boolean handle(ClientUser sender) {
        if (entityId == -1) {
            Adjust.lookTargetHandler.getClass();
            ChatUtil.sendClientMessage("set target");
            return true;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return false;
        Entity entity = mc.theWorld.getEntityByID(entityId);
        if (!(entity instanceof EntityLivingBase)) return false;
        if (mc.thePlayer == null) return false;
        Vec3 playerPosition = mc.thePlayer.getPositionVector();
        if (playerPosition.squareDistanceTo(DistanceUtil.closestPoint(playerPosition, entity.getEntityBoundingBox())) > 9.0) return true;
        Adjust.lookTargetHandler.target = new LookTargetHandler.LookTarget(entity, duration, position, speed);
        ChatUtil.sendClientMessage("set target");
        return true;
    }

    public void write(PacketBuffer buffer) {
    }

    public void read(PacketBuffer buffer) {
        entityId = buffer.readVarIntFromBuffer();
        duration = buffer.readVarIntFromBuffer();
        position = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        speed = buffer.readFloat();
    }
}
