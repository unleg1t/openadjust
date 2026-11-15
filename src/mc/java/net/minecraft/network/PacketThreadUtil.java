package net.minecraft.network;

import net.minecraft.util.IThreadListener;

public class PacketThreadUtil
{
    public static <T extends INetHandler> void checkThreadAndEnqueue(final Packet<T> p_180031_0_, final T p_180031_1_, IThreadListener p_180031_2_) throws ThreadQuickExitException
    {
        if (!p_180031_2_.isCallingFromMinecraftThread())
        {
            p_180031_2_.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    if (p_180031_1_ instanceof net.minecraft.client.network.NetHandlerPlayClient nh
                            && !nh.getNetworkManager().isChannelOpen()
                            && !(p_180031_0_ instanceof net.minecraft.network.play.server.S40PacketDisconnect))
                    {
                        return;
                    }
                    p_180031_0_.processPacket(p_180031_1_);
                }
            });
            throw ThreadQuickExitException.INSTANCE;
        }
    }
}
