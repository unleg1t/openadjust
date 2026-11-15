package com.adjustclient.lunar.apollo.protocol.common;

import com.adjustclient.lunar.apollo.protocol.ProtoMessage;
import com.adjustclient.util.proto.ProtoReader;

public final class ApolloItemStackIcon implements ProtoMessage {
    private final Integer itemId;
    private final String itemName;
    private final Object field12 = null;

    public ApolloItemStackIcon(Integer itemId, String itemName) {
        this.itemId = itemId;
        this.itemName = itemName;
    }

    public Integer getItemId() { return itemId; }
    public String getItemName() { return itemName; }

    public static ApolloItemStackIcon parse(ProtoReader in) {
        Integer itemId = null;
        String itemName = null;
        for (int tag; (tag = in.readTag()) != 0;) {
            switch (tag) {
                case 8:
                    itemId = Integer.valueOf((int) in.readEnum());
                    itemName = null;
                    break;
                case 18:
                    itemName = in.readString();
                    itemId = null;
                    break;
                case 24: in.readEnum(); break;
                default: in.skipField(tag);
            }
        }
        return new ApolloItemStackIcon(itemId, itemName);
    }
}
