package com.adjustclient.property;

import com.adjustclient.util.MinecraftInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Manager<T> {
    public final ArrayList<T> objects;
    public final List<T> view;
    public final Set<Runnable> listeners;

    public Manager() {
        objects = new ArrayList<>();
        view = Collections.unmodifiableList(objects);
        listeners = new HashSet<>();
    }

    protected abstract boolean matches(T object, Object key);

    public T get(Object key) {
        for (T object : objects) {
            if (object == key || matches(object, key)) return object;
        }
        return null;
    }

    public boolean add(T object) {
        if (get(object) != null) throw new IllegalStateException("Duplicate object in manager");
        objects.add(object);
        notifyListeners();
        return true;
    }

    public void notifyListeners() {
        for (Runnable listener : listeners) listener.run();
    }
}
