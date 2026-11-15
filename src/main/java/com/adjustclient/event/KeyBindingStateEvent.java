package com.adjustclient.event;

public class KeyBindingStateEvent extends CancellableEvent {
    public boolean pressed;
    public boolean queuedPress;
    public net.minecraft.client.settings.KeyBinding binding;

    public KeyBindingStateEvent(net.minecraft.client.settings.KeyBinding binding, boolean pressed, boolean queuedPress) {
        this.binding = binding;
        this.pressed = pressed;
        this.queuedPress = queuedPress;
    }
}
