package com.adjustclient.script.internal;

import java.util.Iterator;

public class HostIterator<E>
implements Iterator<E> {
    @Override
    public native boolean hasNext();

    @Override
    public native E next();

    @Override
    public native void remove();
}

