package com.adjustclient.module.impl.render.esp.render;

import java.util.Objects;
import net.minecraft.item.Item;

record ItemPoolKey(Item item, int metadata, String name) {
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof ItemPoolKey key && Objects.equals(name, key.name)
                && metadata == key.metadata && Objects.equals(item, key.item);
    }
    @Override public int hashCode() {
        return 31 * (metadata + 31 * Objects.hashCode(item)) + Objects.hashCode(name);
    }
    @Override public String toString() {
        return "d[item=" + item + ", metadata=" + metadata + ", name=" + name + "]";
    }
}
