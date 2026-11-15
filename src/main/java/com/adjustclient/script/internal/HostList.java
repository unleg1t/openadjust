package com.adjustclient.script.internal;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class HostList<E>
extends AbstractList<E> {
    @Override
    public native int size();

    @Override
    public native E get(int var1);

    @Override
    public native E set(int var1, E var2);

    @Override
    public native void add(int var1, E var2);

    @Override
    public native boolean add(E var1);

    @Override
    public native E remove(int var1);

    @Override
    public native boolean remove(Object var1);

    @Override
    public native boolean contains(Object var1);

    @Override
    public native int indexOf(Object var1);

    @Override
    public native int lastIndexOf(Object var1);

    @Override
    public native void clear();

    @Override
    public Iterator<E> iterator() {
        return this.listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public native ListIterator<E> listIterator(int var1);

    @Override
    public native List<E> subList(int var1, int var2);
}

