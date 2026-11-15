package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.util.Vec2f;
import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public final class CreativePlaceTask {
    public final BlockPos position;
    public final Vec2f rotation;
    public final EnumFacing facing;
    public Object field16;

    public CreativePlaceTask(BlockPos position, Vec2f rotation, Vec3 eyes) {
        this.position = position;
        this.rotation = rotation;
        EnumFacing[] faces = EnumFacing.values();
        Vec3 origin = new Vec3(position.getX(), position.getY(), position.getZ());
        Arrays.sort(faces, Comparator.comparingDouble(face ->
                eyes.squareDistanceTo(origin.addVector(face.getFrontOffsetX(), face.getFrontOffsetY(), face.getFrontOffsetZ()))));
        this.facing = faces[0];
    }
}
