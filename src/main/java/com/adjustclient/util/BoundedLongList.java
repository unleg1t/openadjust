package com.adjustclient.util;

public final class BoundedLongList extends it.unimi.dsi.fastutil.longs.LongArrayList {
    public int maxSize;
    public BoundedLongList(int maxSize) { this.maxSize = maxSize; }
    @Override public void add(int index, long element) {
        if (size() >= maxSize) removeLong(0);
        super.add(index, element);
    }
    @Override public boolean add(long element) {
        if (size() >= maxSize) removeLong(0);
        return super.add(element);
    }
}
