package com.adjustclient.utilities.lunar.apollo.protocol.configurable;

import com.adjustclient.utilities.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.utilities.proto.ProtoReader;
import java.util.ArrayList;
import java.util.List;

public final class OverrideConfigurableSettingsMessage implements ProtoMessage {
    public List<ApolloConfigurableSettings> settings = new ArrayList<>();
    public Object field8;

    public static OverrideConfigurableSettingsMessage parse(ProtoReader reader) {
        OverrideConfigurableSettingsMessage message = new OverrideConfigurableSettingsMessage();
        int tag;
        while ((tag = reader.readTag()) != 0) {
            if (tag == 10) {
                message.settings.add(ApolloConfigurableSettings.parse(reader.readMessage()));
            } else {
                reader.skipField(tag);
            }
        }
        return message;
    }
}
