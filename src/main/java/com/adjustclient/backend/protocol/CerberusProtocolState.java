package com.adjustclient.backend.protocol;

import com.adjustclient.backend.protocol.packet.PacketCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum CerberusProtocolState {
    HANDSHAKE,
    SESSION;

    static {
        HandshakeProtocolState.register(HANDSHAKE);
        MainProtocolState.register(SESSION);
    }

    public final Int2ObjectMap<Supplier<? extends PacketCodec>> packetFactories = new Int2ObjectOpenHashMap<>();
    public final Map<Class<? extends PacketCodec>, Integer> packetIds = new HashMap<>();

    void registerPacket(int id, Class<? extends PacketCodec> codecClass, Supplier<? extends PacketCodec> factory) {
        packetFactories.put(id, factory);
        packetIds.put(codecClass, id);
    }

    public PacketCodec createPacket(int id) {
        Supplier<? extends PacketCodec> factory = packetFactories.get(id);
        if (factory == null) {
            throw new NullPointerException("Unknown packet id " + id);
        }
        return factory.get();
    }

    public Integer getPacketId(Class<?> codecClass) {
        return packetIds.get(codecClass);
    }

}
