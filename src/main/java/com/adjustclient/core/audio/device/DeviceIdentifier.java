package com.adjustclient.core.audio.device;

import java.util.Objects;
public final class DeviceIdentifier {
    public String backend;
    public String name;
    public String field12;
    public DeviceIdentifier(String backend, String name) { this.backend = backend; this.name = name; }
    public boolean equals(Object other) { return this == other || other != null && getClass() == other.getClass() && backend.equals(((DeviceIdentifier) other).backend) && name.equals(((DeviceIdentifier) other).name); }
    public int hashCode() { return Objects.hash(backend, name); }
    public String toString() { return backend + ":" + name; }
}
