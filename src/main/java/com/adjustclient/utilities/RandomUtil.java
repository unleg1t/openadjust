package com.adjustclient.utilities;

import java.util.Random;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RandomUtil {
    public static final Random RANDOM = new Random();

    public static float jitter(float value, float spread) {
        return spread == 0.0F ? value : value + spread * (RANDOM.nextFloat() - 0.5F);
    }
    public static double jitter(double value, double spread) {
        return spread == 0.0 ? value : value + spread * (RANDOM.nextDouble() - 0.5);
    }
    public static int nextInt(Random random, int min, int max) {
        return min == max ? min : min + random.nextInt(max - min);
    }
    public static float nextFloat(float min, float max) {
        return min == max ? min : min + RANDOM.nextFloat() * (max - min);
    }
    public static double nextGaussian(double min, double max) {
        double range = max - min;
        return min + range / 2.0 + RANDOM.nextGaussian() * (range / 2.0) + 0.5;
    }
    public static double nextDouble(double min, double max) {
        return min == max ? min : min + RANDOM.nextDouble() * (max - min);
    }
    public static int nextInt(int bound) { return bound == 0 ? 0 : RANDOM.nextInt(bound); }
    public static float round(float value, int places) {
        if (Float.isInfinite(value)) return 0.0F;
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).floatValue();
    }
    public static String format(double value) {
        return (int) value == value ? Integer.toString((int) value) : Double.toString(round(value, 1));
    }
    public static double round(double value, int places) {
        if (Double.isInfinite(value)) return 0.0;
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
