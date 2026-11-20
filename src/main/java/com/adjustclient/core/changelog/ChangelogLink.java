package com.adjustclient.core.changelog;

import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;

public final class ChangelogLink {
    public String title;
    public String shortTitle;
    public ChangelogVersionBuilder version;
    public Animation animation;
    public int id;
    public boolean announcement;
    public Object field28;

    public ChangelogLink(String title, int id, ChangelogVersionBuilder version) {
        long now = System.currentTimeMillis();
        this.title = title;
        this.shortTitle = abbreviateTitle(title);
        this.version = version;
        this.animation = new Animation(Easing.LINEAR, 110L, now);
        this.id = id;
    }

    public ChangelogLink(String title, String... messages) {
        long now = System.currentTimeMillis();
        this.title = title;
        this.shortTitle = "";
        this.version = new ChangelogVersionBuilder();
        for (String message : messages) this.version.add(message);
        this.animation = new Animation(Easing.LINEAR, 110L, now);
        this.id = -1;
        this.announcement = true;
    }

    public float animate(boolean selected) {
        animation.update(selected ? 1.0F : 0.0F);
        float value = animation.value;
        if (!(value >= 0.0F)) return 0.0F;
        return value <= 1.0F ? value + 0.0F : 1.0F;
    }

    public static String abbreviateTitle(String title) {
        if (title.startsWith("June") || title.startsWith("July")) return title;
        return title.substring(0, 3) + " " + title.substring(title.indexOf(' ') + 1);
    }
}
