package com.adjustclient.util.sunshine;

import java.util.*;

public final class ViolationDetails {
    public final CheckCategory category;
    public final List<String> messages = new ArrayList<>();
    public final Map<String, Object> values = new LinkedHashMap<>();
    public Object field16;
    public ViolationDetails(CheckCategory category, String... messages) {
        this.category = category;
        Collections.addAll(this.messages, messages);
    }
    public ViolationDetails put(String key, Object value) {
        if (values.containsKey(key)) throw new RuntimeException("key " + key + " already present");
        values.put(key, value);
        return this;
    }
}
