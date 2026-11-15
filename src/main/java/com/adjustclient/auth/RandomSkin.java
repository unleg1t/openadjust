package com.adjustclient.auth;

public record RandomSkin(String skinUrl, String variant) {
    @Override public String toString() { return "e[skinUrl=" + skinUrl + ", variant=" + variant + "]"; }
}
