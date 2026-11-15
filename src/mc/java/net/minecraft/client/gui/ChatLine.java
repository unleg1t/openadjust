package net.minecraft.client.gui;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ChatLine
{
    private final int updateCounterCreated;
    public IChatComponent lineString;
    public IChatComponent messageComponent;
    public IChatComponent originalComponent;
    public long timestamp;
    public boolean firstWrappedLine;
    private final int chatLineID;

    public ChatLine(int p_i45000_1_, IChatComponent p_i45000_2_, int p_i45000_3_)
    {
        this(p_i45000_1_, p_i45000_2_, p_i45000_2_, p_i45000_2_, p_i45000_3_, System.currentTimeMillis(), true);
    }

    public ChatLine(int p_i45000_1_, IChatComponent lineString, IChatComponent messageComponent, IChatComponent originalComponent, int p_i45000_3_, long timestamp, boolean firstWrappedLine)
    {
        this.lineString = lineString;
        this.messageComponent = messageComponent;
        this.originalComponent = originalComponent;
        this.updateCounterCreated = p_i45000_1_;
        this.timestamp = timestamp;
        this.chatLineID = p_i45000_3_;
        this.firstWrappedLine = firstWrappedLine;
    }

    public IChatComponent getChatComponent()
    {
        return this.lineString;
    }

    public int getUpdatedCounter()
    {
        return this.updateCounterCreated;
    }

    public int getChatLineID()
    {
        return this.chatLineID;
    }
}
