package com.adjustclient.utilities.sunshine.listener;
import java.util.*;
public final class MotionContext<T extends CheckListener> {
    public final Class<T> listenerType;
    public final Set<T> listeners = new HashSet<>();
    public Object field12;
    public MotionContext(Class<T> listenerType) { this.listenerType = listenerType; }
}
