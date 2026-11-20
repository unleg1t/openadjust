package com.adjustclient.modules.impl.render.trajectories;
public enum ProjectileType {
    FIREBALL("Fireball"), ARROW("Arrow"), ENDER_PEARL("Ender Pearl"), SNOWBALL("Snowball"), EGG("Egg"), SPLASH_POTION("Splash Potion");
    public final String displayName;
    ProjectileType(String displayName) { this.displayName = displayName; }
}
