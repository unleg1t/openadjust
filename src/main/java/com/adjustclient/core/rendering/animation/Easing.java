package com.adjustclient.core.rendering.animation;

import net.minecraft.util.MathHelper;

public enum Easing {
    LINEAR,
    EASE_IN_QUAD,
    EASE_OUT_QUAD,
    EASE_IN_OUT_QUAD,
    EASE_IN_CUBIC,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    EASE_IN_QUART,
    EASE_OUT_QUART,
    EASE_IN_OUT_QUART,
    EASE_IN_QUINT,
    EASE_OUT_QUINT,
    EASE_IN_OUT_QUINT,
    EASE_IN_SINE,
    EASE_OUT_SINE,
    EASE_IN_OUT_SINE,
    EASE_IN_EXPO,
    EASE_OUT_EXPO,
    EASE_IN_OUT_EXPO,
    EASE_IN_CIRC,
    EASE_OUT_CIRC,
    EASE_IN_OUT_CIRC,
    SIGMOID,
    EASE_OUT_ELASTIC,
    EASE_IN_BACK,
    PARABOLIC;

    private final it.unimi.dsi.fastutil.floats.Float2FloatFunction curve = this::get;

    public float get(float x) {
        switch (this) {
            case LINEAR: return x;
            case EASE_IN_QUAD: return x * x;
            case EASE_OUT_QUAD: return (2.0F - x) * x;
            case EASE_IN_OUT_QUAD:
                return x < 0.5F ? (x * 2.0F) * x : (4.0F - x * 2.0F) * x + -1.0F;
            case EASE_IN_CUBIC: return (x * x) * x;
            case EASE_OUT_CUBIC: return ((x - 1.0F) * (x - 1.0F)) * (x - 1.0F) + 1.0F;
            case EASE_IN_OUT_CUBIC:
                return x >= 0.5F
                    ? ((x - 1.0F) * (x * 2.0F - 2.0F)) * (x * 2.0F - 2.0F) + 1.0F
                    : ((x * 4.0F) * x) * x;
            case EASE_IN_QUART: return ((x * x) * x) * x;
            case EASE_OUT_QUART: return 1.0F - (((x - 1.0F) * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F);
            case EASE_IN_OUT_QUART:
                return x >= 0.5F
                    ? 1.0F - (((8.0F * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F)
                    : (((8.0F * x) * x) * x) * x;
            case EASE_IN_QUINT: return (((x * x) * x) * x) * x;
            case EASE_OUT_QUINT: return ((((x - 1.0F) * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F) + 1.0F;
            case EASE_IN_OUT_QUINT:
                return x >= 0.5F
                    ? ((((16.0F * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F)) * (x - 1.0F) + 1.0F
                    : ((((16.0F * x) * x) * x) * x) * x;
            case EASE_IN_SINE:
            case EASE_IN_OUT_SINE:
                return 1.0F - MathHelper.cos((float) ((double) x * Math.PI * 0.5));
            case EASE_OUT_SINE:
                return MathHelper.sin((float) ((double) x * Math.PI * 0.5));
            case EASE_IN_EXPO:
                return x == 0.0F ? 0.0F : (float) Math.pow(2.0, (double) (x * 10.0F - 10.0F));
            case EASE_OUT_EXPO:
                return x == 1.0F ? 1.0F : 1.0F - (float) Math.pow(2.0, (double) (x * -10.0F));
            case EASE_IN_OUT_EXPO:
                if (x == 0.0F) return 0.0F;
                if (x == 1.0F) return 1.0F;
                return x < 0.5F
                    ? (float) Math.pow(2.0, (double) (x * 20.0F - 10.0F)) * 0.5F
                    : (2.0F - (float) Math.pow(2.0, (double) (x * -20.0F + 10.0F))) * 0.5F;
            case EASE_IN_CIRC: return 1.0F - (float) Math.sqrt((double) (1.0F - x * x));
            case EASE_OUT_CIRC: return (float) Math.sqrt((double) (1.0F - (x - 1.0F) * (x - 1.0F)));
            case EASE_IN_OUT_CIRC:
                return x < 0.5F
                    ? (1.0F - (float) Math.sqrt((double) (1.0F - (4.0F * x) * x))) * 0.5F
                    : ((float) Math.sqrt((double) (1.0F - ((x - 1.0F) * 4.0F) * x)) + 1.0F) * 0.5F;
            case SIGMOID: return 1.0F / ((float) Math.exp((double) -x) + 1.0F);
            case EASE_OUT_ELASTIC:
                if (x == 0.0F) return 0.0F;
                if (x == 1.0F) return 1.0F;
                return (float) (Math.sin(((double) (x * 10.0F) - 0.75) * 2.0943951023931953)
                    * Math.pow(2.0, (double) (x * -10.0F)) * 0.5 + 1.0);
            case EASE_IN_BACK: return ((x * 2.70158F) * x) * x - (x * 1.70158F) * x;
            case PARABOLIC: return 1.0F - (x * 2.0F - 1.0F) * (x * 2.0F - 1.0F);
            default: throw new AssertionError(this);
        }
    }

}
