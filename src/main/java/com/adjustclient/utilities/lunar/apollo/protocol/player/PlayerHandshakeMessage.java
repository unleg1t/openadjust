package com.adjustclient.utilities.lunar.apollo.protocol.player;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloLunarClientVersion;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ApolloMinecraftVersion;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoValue;
import com.adjustclient.utilities.lunar.apollo.protocol.common.ProtoValueKind;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PlayerHandshakeMessage implements ProtoMessage {
    private final ApolloMinecraftVersion minecraftVersion;
    private final ApolloLunarClientVersion lunarClientVersion;
    private final List<ModMessage> mods;
    private final ApolloEmbeddedCheckoutSupport embeddedCheckoutSupport;
    private final Map<String, ProtoValue> modStatus;
    private final Object field24 = null;

    public PlayerHandshakeMessage(ApolloMinecraftVersion minecraftVersion,
            ApolloLunarClientVersion lunarClientVersion, List<ModMessage> mods,
            ApolloEmbeddedCheckoutSupport embeddedCheckoutSupport, Map<String, ProtoValue> modStatus) {
        this.minecraftVersion = minecraftVersion;
        this.lunarClientVersion = lunarClientVersion;
        this.mods = mods;
        this.embeddedCheckoutSupport = embeddedCheckoutSupport;
        this.modStatus = modStatus;
    }

    public ApolloMinecraftVersion getMinecraftVersion() { return minecraftVersion; }
    public ApolloLunarClientVersion getLunarClientVersion() { return lunarClientVersion; }
    public List<ModMessage> getMods() { return mods; }
    public ApolloEmbeddedCheckoutSupport getEmbeddedCheckoutSupport() { return embeddedCheckoutSupport; }
    public Map<String, ProtoValue> getModStatus() { return modStatus; }

    public static PlayerHandshakeMessage parse(ProtoReader in) {
        ApolloMinecraftVersion minecraftVersion = null;
        ApolloLunarClientVersion lunarClientVersion = null;
        List<ModMessage> mods = new ArrayList<>();
        ApolloEmbeddedCheckoutSupport embeddedCheckoutSupport = ApolloEmbeddedCheckoutSupport.EMBEDDED_CHECKOUT_SUPPORT_UNSPECIFIED;
        Map<String, ProtoValue> modStatus = new LinkedHashMap<>();
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 10 -> minecraftVersion = ApolloMinecraftVersion.parse(in.readMessage());
                case 18 -> lunarClientVersion = ApolloLunarClientVersion.parse(in.readMessage());
                case 26 -> mods.add(ModMessage.parse(in.readMessage()));
                case 32 -> {
                    long value = in.readEnum();
                    embeddedCheckoutSupport = value == 0 ? ApolloEmbeddedCheckoutSupport.EMBEDDED_CHECKOUT_SUPPORT_UNSPECIFIED
                            : value == 1 ? ApolloEmbeddedCheckoutSupport.EMBEDDED_CHECKOUT_SUPPORT_OVERLAY
                            : value == 2 ? ApolloEmbeddedCheckoutSupport.EMBEDDED_CHECKOUT_SUPPORT_WINDOW
                            : ApolloEmbeddedCheckoutSupport.UNRECOGNIZED;
                }
                case 42 -> {
                    ProtoReader entry = in.readMessage();
                    String key = "";
                    ProtoValue value = new ProtoValue(ProtoValueKind.NOT_SET, null);
                    for (int entryTag; (entryTag = entry.readTag()) != 0;) {
                        switch (entryTag) {
                            case 10 -> key = entry.readString();
                            case 18 -> value = ProtoValue.parse(entry.readMessage());
                            default -> entry.skipField(entryTag);
                        }
                    }
                    modStatus.put(key, value);
                }
                default -> in.skipField(tag);
            }
        }
        return new PlayerHandshakeMessage(minecraftVersion, lunarClientVersion, mods,
                embeddedCheckoutSupport, modStatus);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        if (minecraftVersion != null) out.writeBytes(1, minecraftVersion.toByteArray());
        if (lunarClientVersion != null) out.writeBytes(2, lunarClientVersion.toByteArray());
        for (ModMessage mod : mods) out.writeBytes(3, mod.toByteArray());
        if (embeddedCheckoutSupport.number >= 1) out.writeInt32(4, embeddedCheckoutSupport.number);
        for (Map.Entry<String, ProtoValue> entry : modStatus.entrySet()) {
            ProtoWriter entryWriter = new ProtoWriter();
            entryWriter.writeString(1, entry.getKey());
            ProtoValue value = Objects.requireNonNull(entry.getValue(), "value");
            ProtoWriter valueWriter = new ProtoWriter();
            value.writeTo(valueWriter);
            byte[] valueBytes = valueWriter.toByteArray();
            entryWriter.writeBytes(2, valueBytes, 0, valueBytes.length);
            byte[] entryBytes = entryWriter.toByteArray();
            out.writeBytes(5, entryBytes, 0, entryBytes.length);
        }
    }

    public PlayerHandshakeMessage putModStatus(String key, ProtoValue value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        modStatus.put(key, value);
        return this;
    }
}
