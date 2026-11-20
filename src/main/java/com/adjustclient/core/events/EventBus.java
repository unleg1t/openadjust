package com.adjustclient.core.events;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    private static final Map<Class<?>, List<Handler>> HANDLERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Handler>> DISPATCH_CACHE = new ConcurrentHashMap<>();
    private static final Comparator<Handler> BY_PRIORITY_DESC = Comparator.comparingInt((Handler x) -> x.priority).reversed();
    private static final Object LOCK = new Object();

    private EventBus() {
    }

    /** Registers every {@link EventHandler} method of {@code listener}. */
    public static void register(Listener listener) {
        if (listener == null) {
            return;
        }
        synchronized (LOCK) {
            for (Class<?> c = listener.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (Method m : c.getDeclaredMethods()) {
                    EventHandler h = m.getAnnotation(EventHandler.class);
                    if (h == null) {
                        continue;
                    }
                    if (m.getParameterCount() != 1 || !Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        Logger.log(
                                LogLevel.WARN,
                                false,
                                "Ignoring invalid @EventHandler method {}.{} (expected single Event parameter)",
                                c.getName(),
                                m.getName()
                        );
                        continue;
                    }
                    try {
                        m.setAccessible(true);
                        MethodHandle mh = MethodHandles.lookup().unreflect(m).bindTo(listener);
                        List<Handler> list = HANDLERS.computeIfAbsent(m.getParameterTypes()[0], k -> new CopyOnWriteArrayList<>());
                        list.add(new Handler(listener, mh, h.priority()));
                        List<Handler> sorted = new ArrayList<>(list);
                        sorted.sort(BY_PRIORITY_DESC);
                        list.clear();
                        list.addAll(sorted);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            DISPATCH_CACHE.clear();
        }
    }

    /** Removes all handlers of {@code listener}. */
    public static void unregister(Listener listener) {
        if (listener == null) {
            return;
        }
        synchronized (LOCK) {
            for (List<Handler> list : HANDLERS.values()) {
                list.removeIf(h -> h.owner == listener);
            }
            DISPATCH_CACHE.clear();
        }
    }

    /**
     * Delivers {@code event} to handlers registered for its class and event superclasses in priority order,
     * then returns it (for cancellation checks).
     */
    public static <T extends Event> T post(T event) {
        if (event == null) {
            return null;
        }
        Class<?> eventClass = event.getClass();
        List<Handler> list = DISPATCH_CACHE.get(eventClass);
        if (list == null) {
            list = resolveHandlers(eventClass);
        }
        for (Handler h : list) {
            try {
                h.handle.invoke(event);
            } catch (Throwable t) {
                throw t instanceof RuntimeException r ? r : new RuntimeException(t);
            }
        }
        return event;
    }

    private static List<Handler> resolveHandlers(Class<?> eventClass) {
        synchronized (LOCK) {
            List<Handler> cached = DISPATCH_CACHE.get(eventClass);
            if (cached != null) {
                return cached;
            }
            List<Class<?>> hierarchy = new ArrayList<>();
            for (Class<?> c = eventClass; c != null && c != Event.class && c != Object.class; c = c.getSuperclass()) {
                hierarchy.add(c);
            }
            List<Handler> resolved = new ArrayList<>();
            for (int i = hierarchy.size() - 1; i >= 0; i--) {
                List<Handler> direct = HANDLERS.get(hierarchy.get(i));
                if (direct != null && !direct.isEmpty()) {
                    resolved.addAll(direct);
                }
            }
            if (resolved.size() > 1) {
                resolved.sort(BY_PRIORITY_DESC);
            }
            List<Handler> snapshot = resolved.isEmpty() ? List.of() : List.copyOf(resolved);
            DISPATCH_CACHE.put(eventClass, snapshot);
            return snapshot;
        }
    }

    private record Handler(Listener owner, MethodHandle handle, int priority) {
    }
}
