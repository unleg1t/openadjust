package com.adjustclient.utilities.protocol.v1_12_2.play.server;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.lang.String;
import java.util.Collection;

public class SPacketTeams implements ProtocolPacket<PlayClientHandler> {
    private String name;
    private String displayName;
    private String prefix;
    private String suffix;
    private String nameTagVisibility;
    private String collisionRule;
    private int color;
    private final Collection<String> players;
    private int action;
    private int friendlyFlags;

    public SPacketTeams() {
        this.name = "";
        this.displayName = "";
        this.prefix = "";
        this.suffix = "";
        this.nameTagVisibility = "always";
        this.collisionRule = "always";
        this.players = new java.util.ArrayList<>();
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
