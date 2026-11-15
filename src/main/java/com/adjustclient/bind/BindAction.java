package com.adjustclient.bind;

@FunctionalInterface
public interface BindAction {
    void onAction(BindMode mode);
}
