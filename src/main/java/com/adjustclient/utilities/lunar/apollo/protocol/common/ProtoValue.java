package com.adjustclient.utilities.lunar.apollo.protocol.common;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import com.adjustclient.utilities.proto.ProtoWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProtoValue implements ProtoMessage {
    private final ProtoValueKind kind;
    private final Object value;
    private final Object field12 = null;

    public ProtoValue() {
        this(ProtoValueKind.NOT_SET, null);
    }

    public ProtoValue(ProtoValueKind kind, Object value) {
        this.kind = kind;
        this.value = value;
    }

    public ProtoValueKind getKind() { return kind; }
    public Object getValue() { return value; }

    @SuppressWarnings("unchecked")
    public Map<String, ProtoValue> getStructValue() {
        return kind == ProtoValueKind.STRUCT_VALUE ? (Map<String, ProtoValue>) value : Map.of();
    }

    @SuppressWarnings("unchecked")
    public List<ProtoValue> getListValue() {
        return kind == ProtoValueKind.LIST_VALUE ? (List<ProtoValue>) value : List.of();
    }

    public static ProtoValue parse(ProtoReader in) {
        ProtoValueKind kind = ProtoValueKind.NOT_SET;
        Object value = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 8:
                    in.readEnum();
                    kind = ProtoValueKind.NULL_VALUE;
                    value = null;
                    break;
                case 17:
                    value = Double.valueOf(Double.longBitsToDouble(in.readFixed64()));
                    kind = ProtoValueKind.NUMBER_VALUE;
                    break;
                case 26:
                    value = in.readString();
                    kind = ProtoValueKind.STRING_VALUE;
                    break;
                case 32:
                    value = Boolean.valueOf(in.readEnum() != 0);
                    kind = ProtoValueKind.BOOL_VALUE;
                    break;
                case 42:
                    value = new LinkedHashMap<>(parseStruct(in.readMessage()));
                    kind = ProtoValueKind.STRUCT_VALUE;
                    break;
                case 50:
                    value = new ArrayList<>(parseList(in.readMessage()));
                    kind = ProtoValueKind.LIST_VALUE;
                    break;
                default: in.skipField(tag);
            }
        }
        return new ProtoValue(kind, value);
    }

    @Override
    public void writeTo(ProtoWriter out) {
        switch (kind) {
            case NULL_VALUE: out.writeInt32(1, 0); break;
            case NUMBER_VALUE:
                out.writeFixed64(2, Double.doubleToRawLongBits((Double) value));
                break;
            case STRING_VALUE: out.writeString(3, (String) value); break;
            case BOOL_VALUE: out.writeBool(4, (Boolean) value); break;
            case STRUCT_VALUE: out.writeBytes(5, writeStruct().toByteArray()); break;
            case LIST_VALUE: out.writeBytes(6, writeList().toByteArray()); break;
            default: break;
        }
    }

    public static Map<String, ProtoValue> parseStruct(ProtoReader in) {
        Map<String, ProtoValue> fields = new LinkedHashMap<>();
        for (int tag; (tag = in.readTag()) != 0;) {
            if (tag == 10) {
                ProtoReader entry = in.readMessage();
                ProtoValue value = new ProtoValue(ProtoValueKind.NOT_SET, null);
                String key = "";
                for (int entryTag; (entryTag = entry.readTag()) != 0;) {
                    switch (entryTag) {
                        case 10: key = entry.readString(); break;
                        case 18: value = ProtoValue.parse(entry.readMessage()); break;
                        default: entry.skipField(entryTag);
                    }
                }
                fields.put(key, value);
            } else {
                in.skipField(tag);
            }
        }
        return fields;
    }

    public static List<ProtoValue> parseList(ProtoReader in) {
        List<ProtoValue> values = new ArrayList<>();
        for (int tag; (tag = in.readTag()) != 0;) {
            if (tag == 10) {
                values.add(ProtoValue.parse(in.readMessage()));
            } else {
                in.skipField(tag);
            }
        }
        return values;
    }

    public ProtoWriter writeStruct() {
        Iterable<Map.Entry<String, ProtoValue>> entries = getStructValue().entrySet();
        ProtoWriter out = new ProtoWriter();
        for (Map.Entry<String, ProtoValue> entry : entries) {
            ProtoWriter encodedEntry = new ProtoWriter();
            encodedEntry.writeString(1, Objects.requireNonNull(entry.getKey(), "value"));
            encodedEntry.writeBytes(2, entry.getValue().toByteArray());
            out.writeBytes(1, encodedEntry.toByteArray());
        }
        return out;
    }

    public ProtoWriter writeList() {
        Iterable<ProtoValue> values = getListValue();
        ProtoWriter out = new ProtoWriter();
        for (ProtoValue value : values) {
            out.writeBytes(1, value.toByteArray());
        }
        return out;
    }
}
