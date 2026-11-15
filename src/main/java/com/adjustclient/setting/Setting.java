package com.adjustclient.setting;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.ui.clickgui.ClickGuiItem;
import com.adjustclient.ui.hud.toggled.ToggledList;
import com.adjustclient.util.Named;
import com.adjustclient.vfs.VirtualFileSystem;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> implements ClickGuiItem, Named {
    public static int allocationCount;
    protected SettingContainer parent;
    public String name;
    public BooleanSupplier hideIf;
    public ArrayList<SettingChangeListener<T>> listeners;
    public T value;
    protected T defaultValue;
    public String description;
    protected String displayName;
    public java.util.function.Supplier<String> field36;
    public T previousValue;
    public boolean field44;

    protected Setting(SettingContainer parent, String name, T defaultValue, BooleanSupplier hideIf) {
        this.parent = parent;
        this.name = name;
        this.value = defaultValue;
        this.hideIf = hideIf;
        if (parent != null) parent.getSettings().add(this);
        this.defaultValue = defaultValue;
        allocationCount++;
    }

    public void fireChanged() {
        notifyListeners(previousValue, value);
    }

    public void notifyListenersExcept(T oldValue, T newValue, SettingChangeListener<T>... excluded) {
        if (listeners == null) return;
        for (SettingChangeListener<T> listener : listeners) {
            if (excluded != null && Arrays.stream(excluded).anyMatch(
                    candidate -> candidate == listener)) {
                continue;
            }
            try {
                listener.onChange(oldValue, newValue);
            } catch (Throwable error) {
                error.printStackTrace();
                Logger.log(LogLevel.ERROR, true, "Error during property update of '{}': {}", name, error);
            }
        }
    }

    public void reset() {
        setValue(defaultValue);
    }

    public boolean setValueSilently(T value) {
        if (this.value == value) return false;
        previousValue = this.value;
        this.value = value;
        return true;
    }

    public boolean isChanged() {
        return !Objects.equals(value, defaultValue) && !field44;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return Objects.requireNonNullElse(displayName, name);
    }

    public void notifyListeners(T oldValue, T newValue) {
        if (ToggledList.INSTANCE != null) ToggledList.INSTANCE.dirty = true;
        if (VirtualFileSystem.INSTANCE != null && !UserConfiguration.isUserSetting(this)) {
            VirtualFileSystem.INSTANCE.markDirty();
        }
        if (listeners != null) {
            for (SettingChangeListener<T> listener : listeners) {
                try {
                    listener.onChange(oldValue, newValue);
                } catch (Throwable error) {
                    error.printStackTrace();
                    Logger.log(LogLevel.ERROR, true, "Error during property update of '{}': {}", name, error);
                }
            }
        }
        previousValue = null;
    }

    public void setValue(T value) {
        if (setValueSilently(value)) notifyListeners(previousValue, value);
    }

    public Setting<T> addListener(SettingChangeListener<T> listener) {
        if (listeners == null) listeners = new ArrayList<>(1);
        listeners.add(listener);
        return this;
    }

    public Setting<T> addListener(Runnable listener) {
        return addListener((oldValue, newValue) -> listener.run());
    }

    public abstract void write(DataOutputStream output) throws IOException;
    public abstract void read(DataInputStream input) throws IOException;

    public boolean isTransient() { return field44; }
    public T getValue() { return value; }
    public T getDefaultValue() { return defaultValue; }
    public SettingContainer getParent() { return parent; }
    public String getDescription() { return description; }
    public String getLabel() { return field36 == null ? name : field36.get(); }
    public void setDescription(String description) { this.description = description; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public boolean isHidden() { return hideIf != null && hideIf.getAsBoolean(); }
    public void setHideIf(BooleanSupplier hideIf) { this.hideIf = hideIf; }
}
