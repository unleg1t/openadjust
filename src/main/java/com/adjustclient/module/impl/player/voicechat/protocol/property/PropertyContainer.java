package com.adjustclient.module.impl.player.voicechat.protocol.property;

import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PropertyContainer<K extends Enum<K> & PropertyKey> {
    public final EnumMap<K, Object> properties;

    protected PropertyContainer(Class<K> keyType) { properties = new EnumMap<>(keyType); }
    protected PropertyContainer(EnumMap<K, Object> properties) { this.properties = properties; }
    protected abstract K[] keys();

    public void read(ByteArrayInputStream in) throws IOException {
        short count = VoicePacket.readShort(in);
        properties.clear();
        for (int i = 0; i < count; i++) {
            String name = VoicePacket.readString(StandardCharsets.UTF_8, in);
            String value = VoicePacket.readString(StandardCharsets.UTF_8, in);
            for (K key : keys()) {
                if (key.getKey().equals(name)) {
                    properties.put(key, parseValue(key.getType(), value));
                    break;
                }
            }
        }
    }

    public synchronized void write(ByteArrayOutputStream out) throws IOException {
        short count = (short) properties.size();
        out.write(count >> 8);
        out.write(count);
        for (Map.Entry<K, Object> entry : properties.entrySet()) {
            VoicePacket.writeString(entry.getKey().getKey(), StandardCharsets.UTF_8, out);
            VoicePacket.writeString(formatValue(entry.getValue()), StandardCharsets.UTF_8, out);
        }
    }

    protected String formatValue(Object value) {
        if (!(value instanceof List<?>)) return value.toString();
        StringBuilder result = new StringBuilder();
        for (Object element : (List<?>) value) {
            if (result.length() != 0) result.append(',');
            result.append(formatValue(element));
        }
        return result.length() == 0 ? " " : result.toString();
    }

    protected Object parseValue(Class<?> type, String value) {
        if (type == String.class) return value;
        if (type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == UUID.class) return UUID.fromString(value);
        if (type == Long.class) return Long.parseLong(value);
        if (type == Double.class) return Double.parseDouble(value);
        if (type == List.class) return new ArrayList<>(value == null
                ? Collections.emptyList() : Arrays.asList(value.split(",")));
        if (type == Integer.class) return Integer.parseInt(value);
        return value;
    }

    public synchronized <T> void set(K key, T value) {
        if (value == null) properties.remove(key);
        else properties.put(key, value);
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<K, Object> entry : properties.entrySet()) {
            if (result.length() != 0) result.append(", ");
            result.append(entry.getKey()).append('=').append(formatValue(entry.getValue()));
        }
        return result.toString();
    }
}
