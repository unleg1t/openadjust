package net.minecraft.network.login.server;

import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import java.io.IOException;
import java.security.PublicKey;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.util.CryptManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class S01PacketEncryptionRequest implements Packet<INetHandlerLoginClient>
{
    private String hashedServerId;
    private PublicKey publicKey;
    private byte[] verifyToken;

    public S01PacketEncryptionRequest()
    {
    }

    public S01PacketEncryptionRequest(String serverId, PublicKey key, byte[] verifyToken)
    {
        this.hashedServerId = serverId;
        this.publicKey = key;
        this.verifyToken = verifyToken;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.hashedServerId = buf.readStringFromBuffer(20);
        this.publicKey = CryptManager.decodePublicKey(buf.readByteArray());
        this.verifyToken = buf.readByteArray();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(this.hashedServerId);
        buf.writeByteArray(this.publicKey.getEncoded());
        buf.writeByteArray(this.verifyToken);
    }

    public void processPacket(INetHandlerLoginClient handler)
    {
        if (handler instanceof NetHandlerLoginClient && ServerTracker.INSTANCE != null && ServerTracker.INSTANCE.serverState != null)
        {
            BlockerState state = ServerTracker.INSTANCE.serverState;
            if (((NetHandlerLoginClient) handler).getNetworkManager() == state.field32 && state.field36 != null)
            {
                state.field40 = System.currentTimeMillis() - state.field36.lastMs;
            }
        }
        handler.handleEncryptionRequest(this);
    }

    @SideOnly(Side.CLIENT)
    public String getServerId()
    {
        return this.hashedServerId;
    }

    @SideOnly(Side.CLIENT)
    public PublicKey getPublicKey()
    {
        return this.publicKey;
    }

    @SideOnly(Side.CLIENT)
    public byte[] getVerifyToken()
    {
        return this.verifyToken;
    }
}
