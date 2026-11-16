package net.minecraftforge.client.model;

import net.minecraft.util.EnumFacing;

public interface ITransformation
{
    Object getMatrix();

    EnumFacing rotate(EnumFacing facing);

    int rotate(EnumFacing facing, int vertexIndex);
}
