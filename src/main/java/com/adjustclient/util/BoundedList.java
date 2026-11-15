package com.adjustclient.util;

public final class BoundedList<T> extends java.util.ArrayList<T> {
    public int maxSize;
    public BoundedList(int maxSize) { this.maxSize = maxSize; }
    @Override public boolean add(T element) {
        if (size() >= maxSize) remove(0);
        return super.add(element);
    }
    @Override public void add(int index, T element) {
        if (size() >= maxSize) remove(0);
        super.add(index, element);
    }
}
