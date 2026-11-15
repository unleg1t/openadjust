package com.adjustclient.audio.device;

import java.util.Objects;
public final class DeviceInfo {
    public AudioDeviceManager manager;
    public DeviceIdentifier identifier;
    public String name;
    public DeviceType type;
    public AudioFormat format;
    public Object field24;
    public DeviceInfo(AudioDeviceManager manager, DeviceIdentifier identifier, String name, DeviceType type, AudioFormat format) {
        this.manager = manager; this.identifier = identifier; this.name = name; this.type = type; this.format = format;
    }
    public int hashCode() { return Objects.hash(manager, identifier, name, type, format); }
    public String toString() { return identifier.toString(); }
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DeviceInfo d = (DeviceInfo) other;
        return manager == d.manager && Objects.equals(identifier, d.identifier) && Objects.equals(name, d.name) && type == d.type && Objects.equals(format, d.format);
    }
}
