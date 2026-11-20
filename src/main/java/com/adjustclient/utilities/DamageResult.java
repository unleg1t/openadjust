package com.adjustclient.utilities;

public class DamageResult {
    public java.util.List<String> modifiers;
    public float damage;
    public Object field12;
    public DamageResult(String[] modifiers, float damage) { this(java.util.List.of(modifiers), damage); }
    public DamageResult(java.util.List<String> modifiers, float damage) { this.modifiers = modifiers; this.damage = damage; }
}
