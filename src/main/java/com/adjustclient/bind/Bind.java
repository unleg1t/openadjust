package com.adjustclient.bind;

import com.adjustclient.event.BindChangeEvent;
import com.adjustclient.event.EventBus;
import com.adjustclient.util.MinecraftInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Bind implements MinecraftInstance {
    public final BindAction action;
    public final HashSet<BindKey> keys = new HashSet<>();
    public Set<BindKey> field12;
    public String name;
    public final ArrayList<Runnable> listeners = new ArrayList<>(1);
    public final int id;
    public boolean down;
    public boolean field29 = true;
    public boolean mutable = true;
    public boolean hidden;
    public Object field32;

    public Bind(int id, BindAction action, String name) {
        this.action = action;
        this.name = name;
        this.id = id;
    }

    public Set<BindKey> getKeys() { return keys; }

    public boolean sharesKeys(Bind other) {
        for (BindKey key : other.keys) if (keys.contains(key)) return true;
        return false;
    }

    public void addKey(BindKey key) {
        if (keys.contains(key)) {
            removeKey(key);
        } else if (mutable) {
            keys.add(key);
            onKeysChanged(Collections.emptyList(), Collections.singletonList(key));
        }
    }

    public boolean isDown() {
        return keys.stream().anyMatch(key -> key.isDown());
    }

    public void clearKeys() {
        if (!mutable) return;
        for (BindKey key : keys.toArray(BindKey[]::new)) removeKey(key);
    }

    public boolean intersects(Bind other) {
        return other.keys.stream().anyMatch(key -> keys.contains(key));
    }

    public Bind setKey(BindKey key) {
        if (!mutable) return this;
        keys.add(key);
        onKeysChanged(Collections.emptyList(), Collections.singletonList(key));
        return this;
    }

    public Bind removeKey(BindKey key) {
        if (!mutable) return this;
        // Even removing an absent key notifies the UI and registered invalidation callbacks.
        keys.remove(key);
        onKeysChanged(Collections.singletonList(key), Collections.emptyList());
        return this;
    }

    public boolean isHeld() {
        return keys.isEmpty() || isDown();
    }

    public void onKeysChanged(List<BindKey> removed, List<BindKey> added) {
        List<BindKey> removedView = Collections.unmodifiableList(removed);
        List<BindKey> addedView = Collections.unmodifiableList(added);
        EventBus.post(new BindChangeEvent(this, removedView, addedView));
        listeners.forEach(listener -> listener.run());
    }
}
