package com.adjustclient.script.internal;

import com.adjustclient.script.internal.HostIterator;
import java.util.ListIterator;

public final class HostListIterator<E>
extends HostIterator<E>
implements ListIterator<E> {
    @Override
    public native boolean hasPrevious();

    @Override
    public native E previous();

    @Override
    public native int nextIndex();

    @Override
    public native int previousIndex();

    @Override
    public native void set(E var1);

    @Override
    public native void add(E var1);
}

