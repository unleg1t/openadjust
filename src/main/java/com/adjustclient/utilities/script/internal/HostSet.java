package com.adjustclient.utilities.script.internal;

import java.util.AbstractSet;
import java.util.Iterator;

public final class HostSet<E>
extends AbstractSet<E> {
    @Override
    public native int size();

    @Override
    public native boolean add(E var1);

    @Override
    public native boolean contains(Object var1);

    @Override
    public native boolean remove(Object var1);

    @Override
    public native void clear();

    @Override
    public native Iterator<E> iterator();
}

