package com.adjustclient.core.audio.filter.impl;

record FilterParameters(int frequency, int duration) {
    public boolean equals(Object other) {
        return this == other || other instanceof FilterParameters p && frequency == p.frequency && duration == p.duration;
    }
    public int hashCode() { return duration + 31 * frequency; }
    public String toString() {
        return "i[frequency=" + frequency + ", duration=" + duration + "]";
    }
}
