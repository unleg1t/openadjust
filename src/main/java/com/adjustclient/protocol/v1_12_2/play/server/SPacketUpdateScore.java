package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.lang.String;
import net.minecraft.network.play.server.S3CPacketUpdateScore.Action;

public class SPacketUpdateScore implements ProtocolPacket<PlayClientHandler> {
    private String name;
    private String objective;
    private int value;
    private Action action;

    public SPacketUpdateScore() {
        this.name = "";
        this.objective = "";
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
