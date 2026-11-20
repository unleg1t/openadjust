package com.adjustclient.core.bind;

@FunctionalInterface
public interface BindAction {
    void onAction(BindMode mode);
}
