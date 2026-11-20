package com.adjustclient.utilities.protocol.v1_12_2.play.server;

import com.adjustclient.utilities.protocol.type.BossInfoColor;
import com.adjustclient.utilities.protocol.type.BossInfoOverlay;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.util.IChatComponent;

public class SPacketUpdateBossInfo implements ProtocolPacket<PlayClientHandler> {
    private UUID uniqueId;
    private SPacketUpdateBossInfoOperation operation;
    private IChatComponent name;
    private float percent;
    private BossInfoColor color;
    private BossInfoOverlay overlay;
    private boolean darkenSky;
    private boolean playEndBossMusic;
    private boolean createFog;

    public SPacketUpdateBossInfo() {
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
