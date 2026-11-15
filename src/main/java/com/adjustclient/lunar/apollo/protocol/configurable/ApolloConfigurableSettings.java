package com.adjustclient.lunar.apollo.protocol.configurable;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.lunar.apollo.protocol.common.ProtoValue;
import com.adjustclient.lunar.apollo.protocol.common.ProtoValueKind;
import com.adjustclient.util.proto.ProtoReader;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApolloConfigurableSettings implements ProtoMessage {
    public String field4;
    public String field8;
    public Map<String, ProtoValue> properties = new LinkedHashMap<>();
    public Object field16;

    public static ApolloConfigurableSettings parse(ProtoReader reader) {
        ApolloConfigurableSettings message = new ApolloConfigurableSettings();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            switch (tag) {
                case 10:
                    message.field4 = reader.readString();
                    message.field8 = null;
                    break;
                case 18:
                    message.field8 = reader.readString();
                    message.field4 = null;
                    break;
                case 24:
                    reader.readEnum();
                    break;
                case 34:
                    ProtoReader entry = reader.readMessage();
                    String key = "";
                    ProtoValue value = new ProtoValue(ProtoValueKind.NOT_SET, null);
                    int entryTag;
                    while ((entryTag = entry.readTag()) != 0) {
                        switch (entryTag) {
                            case 10: key = entry.readString(); break;
                            case 18: value = ProtoValue.parse(entry.readMessage()); break;
                            default: entry.skipField(entryTag); break;
                        }
                    }
                    message.properties.put(key, value);
                    break;
                default:
                    reader.skipField(tag);
                    break;
            }
        }
        return message;
    }
}
