package com.adjustclient.vfs;

import com.adjustclient.util.Named;

public abstract class NamedVfsNode<T> extends VfsNode implements Named {
    protected NamedVfsNode() { }
    protected NamedVfsNode(T target) { }
}
