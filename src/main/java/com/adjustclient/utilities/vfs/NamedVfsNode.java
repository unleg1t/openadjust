package com.adjustclient.utilities.vfs;

import com.adjustclient.utilities.Named;

public abstract class NamedVfsNode<T> extends VfsNode implements Named {
    protected NamedVfsNode() { }
    protected NamedVfsNode(T target) { }
}
