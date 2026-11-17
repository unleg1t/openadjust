package com.adjustclient.game.rewound;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.LowLevelPacketReceiveEvent;
import com.adjustclient.event.PacketAppliedEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.game.server.AbstractGameTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.anticheat.ServerTracker;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

public final class RewoundGameTracker extends AbstractGameTracker<RewoundGameState, RewoundGame> {
    public RewoundGameTracker(ServerType server, RewoundGameState initialState) {
        super(server, initialState);
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != server) {
            return;
        }
        state.onWorldReset();
    }

    @EventHandler
    public void onPacket(LowLevelPacketReceiveEvent event) {
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != server) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof S3FPacketCustomPayload payload) {
            String channel = payload.getChannelName();
            PacketBuffer buffer = payload.getBufferData();
            if ("rewound:hello".equals(channel)) {
                state = RewoundGame.UNKNOWN_GAME.stateFactory.create();
                if (mc.isCallingFromMinecraftThread()) {
                    sendRegisterPacket();
                } else {
                    mc.addScheduledTask(RewoundGameTracker::sendRegisterPacket);
                }
            } else if ("rewound:location".equals(channel)) {
                if (!isInvalidPacket(buffer)) {
                    buffer.readStringFromBuffer(32767);
                    String gameType = readOptionalString(buffer);
                    String lobbyName = readOptionalString(buffer);
                    String mode = readOptionalString(buffer);
                    readOptionalString(buffer);
                    RewoundGame game = RewoundGame.fromName(gameType);
                    if (state.game != game) {
                        state = game.stateFactory.create().copyFrom(state).onCreated();
                    }
                    if (mode != null) {
                        state.setLobby(false);
                    } else if (lobbyName != null) {
                        state.setLobby(true);
                    }
                }
            } else if ("rewound:game_start".equals(channel)) {
                if (!isInvalidPacket(buffer)) {
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    state.startGame();
                }
            } else if ("rewound:game_rejoin".equals(channel)) {
                if (!isInvalidPacket(buffer)) {
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    state.rejoined = true;
                    state.startGame();
                }
            } else if ("rewound:game_finish".equals(channel)) {
                if (!isInvalidPacket(buffer)) {
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readStringFromBuffer(37267);
                    buffer.readVarIntFromBuffer();
                    buffer.readVarIntFromBuffer();
                    state.finishGame();
                }
            }
        } else if (packet instanceof S01PacketJoinGame) {
            state = RewoundGame.UNKNOWN_GAME.stateFactory.create();
        }
        state.onPacket(packet, true);
    }

    @EventHandler(priority = 20)
    public void onPacketApplied(PacketAppliedEvent event) {
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != server) {
            return;
        }
        state.onPacket(event.packet, false);
    }

    public static boolean isInvalidPacket(PacketBuffer buffer) {
        if (!buffer.readBoolean()) {
            return true;
        }
        return buffer.readVarIntFromBuffer() != 1;
    }

    private static String readOptionalString(PacketBuffer buffer) {
        return buffer.readBoolean() ? buffer.readStringFromBuffer(32767) : null;
    }

    public static void sendRegisterPacket() {
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(1);
        buffer.writeVarIntToBuffer(4);
        writeChannelVersion(buffer, "rewound:location", 1);
        writeChannelVersion(buffer, "rewound:game_start", 1);
        writeChannelVersion(buffer, "rewound:game_finish", 1);
        writeChannelVersion(buffer, "rewound:game_rejoin", 1);
        mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("rewound:register", buffer));
    }

    private static void writeChannelVersion(PacketBuffer buffer, String channel, int version) {
        buffer.writeString(channel);
        buffer.writeVarIntToBuffer(version);
    }
}
