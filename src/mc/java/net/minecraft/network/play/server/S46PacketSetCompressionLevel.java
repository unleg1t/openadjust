package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class S46PacketSetCompressionLevel implements Packet<INetHandlerPlayClient>
{
    private int threshold;

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.threshold = buf.readVarIntFromBuffer();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.threshold);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleSetCompressionLevel(this);
    }

    @SideOnly(Side.CLIENT)
    public int getThreshold()
    {
        return this.threshold;
    }
}
