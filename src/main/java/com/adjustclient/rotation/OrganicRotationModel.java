package com.adjustclient.rotation;

import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.Vec2f;
import java.util.Random;
import net.minecraft.util.MathHelper;

public final class OrganicRotationModel implements RotationModel {
    private final Random random;
    private final double speed;
    private final double drift;
    private final double jitter;
    private double yawFrequency1;
    private double yawFrequency2;
    private double pitchFrequency1;
    private double pitchFrequency2;
    private double yawPhase1;
    private double yawPhase2;
    private double pitchPhase1;
    private double pitchPhase2;
    private double time;
    private Object field104;

    public OrganicRotationModel(double speed, double drift, double jitter) {
        this.speed = speed; this.drift = drift; this.jitter = jitter;
        Random rng = new Random();
        this.yawFrequency1 = rng.nextDouble() * 0.3D + 0.1D;
        this.yawFrequency2 = rng.nextDouble() * 0.5D + 0.5D;
        this.pitchFrequency1 = rng.nextDouble() * 0.3D + 0.1D;
        this.pitchFrequency2 = rng.nextDouble() * 0.5D + 0.5D;
        this.yawPhase1 = rng.nextDouble() * Math.PI * 2.0D;
        this.yawPhase2 = rng.nextDouble() * Math.PI * 2.0D;
        this.pitchPhase1 = rng.nextDouble() * Math.PI * 2.0D;
        this.pitchPhase2 = rng.nextDouble() * Math.PI * 2.0D;
        this.random = rng;
    }

    @Override public Vec2f apply(Vec2f from, Vec2f to, float factor) {
        float dx = MathHelper.wrapAngleTo180_float(to.x - from.x) * factor;
        float dy = (to.y - from.y) * factor;
        double distance = Math.hypot(dx, dy);
        if (drift > distance) return new Vec2f(from.x + dx, from.y + dy);
        if (distance > 0.0D) {
            float yawLimit = (float) (Math.abs(dx) / distance * speed * factor);
            float pitchLimit = (float) (Math.abs(dy) / distance * speed * factor);
            dx = Math.clamp(dx, -yawLimit, yawLimit);
            dy = Math.clamp(dy, -pitchLimit, pitchLimit);
        }
        time += factor;
        double yawWave = Math.sin(time * yawFrequency1 + yawPhase1);
        double yawWeight = RandomUtil.nextDouble(0.45D, 0.55D);
        double pitchWave = Math.sin(time * pitchFrequency1 + pitchPhase1);
        double yawSecondary = Math.sin(time * yawFrequency2 + yawPhase2);
        yawWave += yawWeight * yawSecondary;
        double pitchWeight = RandomUtil.nextDouble(0.45D, 0.55D);
        double pitchSecondary = Math.sin(time * pitchFrequency2 + pitchPhase2);
        pitchWave += pitchWeight * pitchSecondary;
        float yawDrift = (float) (yawWave * drift * factor);
        float pitchDrift = (float) (pitchWave * drift * factor);
        float yawJitter = (float) ((random.nextDouble() * 2.0D - 1.0D) * jitter * factor);
        float pitchJitter = (float) ((random.nextDouble() * 2.0D - 1.0D) * jitter * factor);
        float yaw = yawDrift + dx + yawJitter + from.x;
        float pitch = pitchDrift + dy + pitchJitter + from.y;
        return RotationUtil.quantizeToMouseSensitivity(new Vec2f(yaw, pitch), from);
    }
}
