package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSlime extends BlockBreakable
{
    public BlockSlime()
    {
        super(Material.clay, false, MapColor.grassColor);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.slipperiness = 0.8F;
    }

    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
    {
        if (entityIn.isSneaking())
        {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        }
        else
        {
            entityIn.fall(fallDistance, 0.0F);
        }
    }

    public void onLanded(World worldIn, Entity entityIn)
    {
        if (entityIn.isSneaking())
        {
            super.onLanded(worldIn, entityIn);
        }
        else if (entityIn.motionY < 0.0D)
        {
            com.adjustclient.event.SlimeBounceEvent event = new com.adjustclient.event.SlimeBounceEvent(true);
            com.adjustclient.event.EventBus.post(event);

            if (event.bounceAllowed)
            {
                entityIn.motionY = -entityIn.motionY;
            }
        }
    }

    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn)
    {
        if (Math.abs(entityIn.motionY) < 0.1D && !entityIn.isSneaking())
        {
            double d0 = 0.4D + Math.abs(entityIn.motionY) * 0.2D;
            com.adjustclient.event.SlimeSlowdownEvent event = new com.adjustclient.event.SlimeSlowdownEvent(0.8F, d0);
            com.adjustclient.event.EventBus.post(event);
            this.slipperiness = event.slipperiness;
            entityIn.motionX *= event.horizontalDamping;
            entityIn.motionZ *= event.horizontalDamping;
        }

        super.onEntityCollidedWithBlock(worldIn, pos, entityIn);
    }
}
