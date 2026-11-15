package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ScaledResolution
{
    private final double scaledWidthD;
    private final double scaledHeightD;
    public int scaledWidth;
    public int scaledHeight;
    private double scaleFactor;

    public ScaledResolution(Minecraft p_i46445_1_)
    {
        this.scaledWidth = p_i46445_1_.displayWidth;
        this.scaledHeight = p_i46445_1_.displayHeight;
        this.scaleFactor = 1.0D;
        boolean flag = p_i46445_1_.isUnicode();
        int i = p_i46445_1_.gameSettings.guiScale;

        if (i == 0)
        {
            i = 1000;
        }

        while (this.scaleFactor < (double)i && (double)this.scaledWidth / (this.scaleFactor + 1.0D) >= 320.0D && (double)this.scaledHeight / (this.scaleFactor + 1.0D) >= 240.0D)
        {
            ++this.scaleFactor;
        }

        if (flag && this.scaleFactor % 2.0D != 0.0D && this.scaleFactor != 1.0D)
        {
            --this.scaleFactor;
        }

        this.scaledWidthD = (double)this.scaledWidth / this.scaleFactor;
        this.scaledHeightD = (double)this.scaledHeight / this.scaleFactor;
        this.scaledWidth = MathHelper.ceiling_double_int(this.scaledWidthD);
        this.scaledHeight = MathHelper.ceiling_double_int(this.scaledHeightD);
    }

    public ScaledResolution(Minecraft minecraft, double scale)
    {
        if (scale % 0.25D != 0.0D)
        {
            throw new RuntimeException("Scale must be a multiple of 0.25");
        }

        this.scaledWidth = minecraft.displayWidth;
        this.scaledHeight = minecraft.displayHeight;
        double d0 = 1.0D;

        while (scale > d0 && (double)this.scaledWidth / (d0 + 1.0D) >= 320.0D && (double)this.scaledHeight / (d0 + 1.0D) >= 240.0D)
        {
            d0 += 0.25D;
        }

        this.scaleFactor = Math.max(2.0D, d0);
        this.scaledWidthD = (double)this.scaledWidth / this.scaleFactor;
        this.scaledHeightD = (double)this.scaledHeight / this.scaleFactor;
        this.scaledWidth = MathHelper.ceiling_double_int(this.scaledWidthD);
        this.scaledHeight = MathHelper.ceiling_double_int(this.scaledHeightD);
    }

    public int getScaledWidth()
    {
        return this.scaledWidth;
    }

    public int getScaledHeight()
    {
        return this.scaledHeight;
    }

    public double getScaledWidth_double()
    {
        return this.scaledWidthD;
    }

    public double getScaledHeight_double()
    {
        return this.scaledHeightD;
    }

    public int getScaleFactor()
    {
        return (int)this.scaleFactor;
    }

    public double getScaleFactor_double()
    {
        return this.scaleFactor;
    }
}
