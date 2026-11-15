package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;

public class SPacketChunkData implements ProtocolPacket<PlayClientHandler> {
    private int chunkX;
    private int chunkZ;
    private int availableSections;
    public byte[] buffer;
    private List<NBTTagCompound> tileEntityTags;
    private boolean fullChunk;

    public SPacketChunkData() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayClientHandler handler) {
    }
}
