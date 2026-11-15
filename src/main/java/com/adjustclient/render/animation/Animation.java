package com.adjustclient.render.animation;

public final class Animation {
    public Easing easing;
    public long duration;
    public long lastCall;
    public long startedAt;
    public float from;
    public float target;
    public float value;
    public boolean finished;

    public Animation(Easing easing, long duration) {
        this(easing, duration, System.currentTimeMillis());
    }

    public Animation(Easing easing, long duration, long startedAt) {
        this.easing = easing;
        this.duration = duration;
        this.startedAt = startedAt;
    }

    public void restart() {
        startedAt = System.currentTimeMillis();
        from = value;
        finished = false;
    }

    public float getValue() {
        return value;
    }

    public boolean isFinished() {
        return finished;
    }

    public float update(float requested) {
        lastCall = System.currentTimeMillis();
        if (requested != target) {
            target = requested;
            startedAt = System.currentTimeMillis();
            from = value;
            finished = false;
        } else if (lastCall - duration > startedAt) {
            finished = true;
            return value = requested;
        } else {
            finished = value == requested;
            if (finished) return value = requested;
        }
        float progress = easing.get((float) (System.currentTimeMillis() - startedAt) / (float) duration);
        if (duration == 0L) value = requested;
        else if (value > requested) value = from - (from - requested) * progress;
        else value = (requested - from) * progress + from;
        if (!Float.isFinite(value)) value = requested;
        return value;
    }
}
