package com.adjustclient.modules.impl.player.voicechat.channel.registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class IdentifiableRegistry<T extends Identifiable> {
    public final Map<UUID, T> byId = new HashMap<>();
    public final List<T> objects = new ArrayList<>();
    public final Comparator<T> comparator;
    public final List<T> sortedObjects = new ArrayList<>();

    public IdentifiableRegistry(Comparator<T> comparator) { this.comparator = comparator; }
    public IdentifiableRegistry() { this(null); }

    public T get(UUID id) { return byId.get(id); }

    public void register(T object) {
        if (byId.containsKey(object.getId())) {
            throw new IllegalArgumentException("Object with id " + object.getId() + " already exists");
        }
        byId.put(object.getId(), object);
        objects.add(object);
    }

    public T remove(UUID id) {
        T removed = byId.remove(id);
        if (removed == null) throw new IllegalArgumentException("Object with id " + id + " does not exist");
        objects.remove(removed);
        return removed;
    }

    public void remove(T object) {
        T removed = byId.remove(object.getId());
        if (removed == null) throw new IllegalArgumentException("Object with id " + object.getId() + " does not exist");
        objects.remove(removed);
    }
}
