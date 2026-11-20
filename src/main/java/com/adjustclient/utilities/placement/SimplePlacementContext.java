package com.adjustclient.utilities.placement;

public final class SimplePlacementContext extends PlacementContext {
    public PlacementVerifier owner;
    public java.util.List<PlacementCheck<?>> checks = new java.util.ArrayList<>();
    public java.util.Map<Object, PlacementCheck<?>> checkMap = new java.util.HashMap<>();
    public Object field16;

}
