package com.adjustclient.modules.impl.movement.antivoid;

import com.adjustclient.utilities.game.packet.PacketTransform;
import com.adjustclient.utilities.Simulation;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import static com.adjustclient.utilities.MinecraftInstance.mc;

class BlinkPacketTransform implements PacketTransform {
    final Simulation simulation;
    final EntityPlayer source;
    final BlinkAntiVoidMode mode;

    BlinkPacketTransform(Simulation simulation, EntityPlayer source, BlinkAntiVoidMode mode) {
        this.simulation = simulation;
        this.source = source;
        this.mode = mode;
    }

    @Override public Packet<?> transform(Packet<?> packet) {
        if (!(packet instanceof C03PacketPlayer)) return packet;
        simulation.player.tick();
        if (!mode.module.isOverVoid(source.getPositionVector())) source.motionX = source.motionZ = 0.0D;
        C03PacketPlayer walking = (C03PacketPlayer) packet;
        if (walking.forkField39) {
            if (walking.isMoving()) {
                simulation.posX = walking.getPositionX();
                simulation.posY = walking.getPositionY();
                simulation.posZ = walking.getPositionZ();
                float halfWidth = simulation.width / 2.0F;
                simulation.boundingBox = new AxisAlignedBB(simulation.posX - halfWidth, simulation.posY,
                        simulation.posZ - halfWidth, simulation.posX + halfWidth,
                        simulation.posY + simulation.height, simulation.posZ + halfWidth);
            }
            return packet;
        }
        EntityPlayerSP player = mc.thePlayer;
        var handler = (com.adjustclient.utilities.protocol.interaction.NativeInteractionHandler)
                player.sendQueue.getNetworkManager().p.interactionHandler;
        return handler.createWalkingUpdatePacket(player, simulation.player.onGround,
                        simulation.player.posX, simulation.player.posY, simulation.player.posZ,
                        walking.getRotating() ? walking.getYaw() : player.lastReportedYaw,
                        walking.getRotating() ? walking.getPitch() : player.lastReportedPitch);
    }
}
