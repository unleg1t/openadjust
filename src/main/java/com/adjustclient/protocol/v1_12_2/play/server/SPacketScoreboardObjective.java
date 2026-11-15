package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.lang.String;
import net.minecraft.scoreboard.IScoreObjectiveCriteria.EnumRenderType;

public class SPacketScoreboardObjective implements ProtocolPacket<PlayClientHandler> {
    private String objectiveName;
    private String objectiveValue;
    private EnumRenderType type;
    private int action;

    public SPacketScoreboardObjective() {
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
