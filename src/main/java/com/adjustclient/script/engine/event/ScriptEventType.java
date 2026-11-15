package com.adjustclient.script.engine.event;

import com.adjustclient.script.engine.type.ClassTypeMirror;
import com.adjustclient.event.Event;

public abstract class ScriptEventType<T extends Event> extends ClassTypeMirror<T> {
    public static final ClassTypeMirror<?> CANCELLABLE_EVENT_TYPE;
    public final String simpleName;
    public Object field24;

    static {
        CANCELLABLE_EVENT_TYPE = new ClassTypeMirror<>(
                "com.adjustclient.script.api.AdjustScript$CancellableEvent", null, null);
    }

    protected ScriptEventType(String simpleName, ClassTypeMirror<?> superType, Class<?> hostType) {
        super("com.adjustclient.script.api.AdjustScript$" + simpleName, superType, hostType);
        this.simpleName = simpleName;
    }
}
