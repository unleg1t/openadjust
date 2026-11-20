package com.adjustclient.modules.impl.render.freecam.tool;

import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.Vec2f;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class CreativePlaceTool extends FreecamTool {
    public final Vec3 origin;
    public final Vec2f rotation;
    public Object field24;

    public CreativePlaceTool(FreecamController controller, MovingObjectPosition hit, Vec3 origin,
                             Vec2f rotation, boolean potion) {
        super(controller, hit);
        this.origin = origin;
        this.rotation = rotation;
    }

    @Override public void execute(List<Packet<?>> packets, Vec3 position) {
        ItemStack previous = mc.thePlayer.getHeldItem();
        ItemStack selected = controller.module.camera.distanceToCamera();
        if (selected == null) return;
        boolean replace = previous != selected;
        int slot = mc.thePlayer.inventory.m() + 36;
        if (replace) packets.add(new C10PacketCreativeInventoryAction(slot, selected));
        packets.add(new C03PacketPlayer.C06PacketPlayerPosLook(origin.xCoord, origin.yCoord, origin.zCoord,
                rotation.x, rotation.y, false));
        packets.add(new C08PacketPlayerBlockPlacement(selected.copy()));
        packets.add(new C03PacketPlayer.C06PacketPlayerPosLook(origin.xCoord, origin.yCoord, origin.zCoord,
                rotation.x + RandomUtil.nextFloat(0.1F, 1.4F), rotation.y, false));
        if (replace) packets.add(new C10PacketCreativeInventoryAction(slot, previous));
    }
}
