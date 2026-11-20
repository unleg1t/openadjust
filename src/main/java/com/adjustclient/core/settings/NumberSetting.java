package com.adjustclient.core.settings;

import com.adjustclient.utilities.ByteUtil;
import com.adjustclient.utilities.TimeUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.function.BooleanSupplier;
import org.lwjgl.input.Keyboard;

public final class NumberSetting extends Setting<Number> implements NumberValue {
    public boolean wrap;
    private Formatter formatter;
    private final Number min;
    private final Number max;
    private final Number step;
    private Object field64;
    public static final DecimalFormat FORMAT;
    public static final Formatter DEGREES;
    public static final Formatter MILLISECONDS;
    public static final Formatter SECONDS;
    public static final Formatter TICKS;
    public static final Formatter INTEGER_SUFFIX_A;
    public static final Formatter INTEGER_SUFFIX_B;
    public static final Formatter MULTIPLIER;
    public static final Formatter METERS;
    public static final Formatter DURATION;

    static {
        FORMAT = new DecimalFormat("#.##");
        DEGREES = setting -> FORMAT.format(setting.getValue()) + "°";
        MILLISECONDS = setting -> setting.getValue().intValue() + "ms";
        SECONDS = NumberSetting::formatSeconds;
        TICKS = setting -> {
            int value = setting.getValue().intValue();
            return value + " tick" + (value == 1 ? "" : "s");
        };
        INTEGER_SUFFIX_A = setting -> setting.getValue().intValue() + "°";
        INTEGER_SUFFIX_B = setting -> setting.getValue().intValue() + "%";
        MULTIPLIER = NumberSetting::formatMultiplier;
        METERS = NumberSetting::formatMeters;
        DURATION = setting -> TimeUtil.formatDuration(setting.getValue().longValue(), true);
    }

    public NumberSetting(SettingContainer parent, String name, Number value, Number min, Number max, Number step) {
        this(parent, name, value, min, max, step, null);
    }
    public NumberSetting(SettingContainer parent, String name, Number value, Number min, Number max, Number step, BooleanSupplier hideIf) {
        super(parent, name, value, hideIf);
        this.min = min;
        this.max = max;
        this.step = step;
    }
    public Number getMin() { return min; }
    public Number getMax() { return max; }
    public Number getStep() { return step; }
    public Formatter getFormatter() { return formatter; }
    public NumberSetting formatter(Formatter formatter) { this.formatter = formatter; return this; }
    public double getDouble() { return getValue().doubleValue(); }
    public float getFloat() { return getValue().floatValue(); }
    public int getInt() { return getValue().intValue(); }
    public long getLong() { return getValue().longValue(); }

    public static String formatMultiplier(NumberSetting setting) { return FORMAT.format(setting.getValue().doubleValue()) + "x"; }

    public void read(DataInputStream input) throws IOException {
        byte type = input.readByte();
        switch (type) {
            case 0: setValue(Float.intBitsToFloat(ByteUtil.readVarInt(input))); break;
            case 1: setValue(Double.longBitsToDouble(ByteUtil.readVarLong(input))); break;
            case 2: setValue(Integer.valueOf(ByteUtil.readVarInt(input))); break;
            case 3: setValue(Long.valueOf(ByteUtil.readVarLong(input))); break;
            default: throw new IOException("Unknown type: " + type);
        }
        fireChanged();
    }

    public void write(DataOutputStream output) throws IOException {
        Number value = getValue();
        if (value instanceof Double) {
            output.writeByte(1);
            ByteUtil.writeVarLong(output, Double.doubleToRawLongBits(value.doubleValue()));
        } else if (value instanceof Float) {
            output.writeByte(0);
            ByteUtil.writeVarInt(output, Float.floatToRawIntBits(value.floatValue()));
        } else if (value instanceof Integer) {
            output.writeByte(2);
            ByteUtil.writeVarInt(output, value.intValue());
        } else {
            output.writeByte(3);
            ByteUtil.writeVarLong(output, value.longValue());
        }
    }

    @Override public void setValue(Number value) {
        Number next;
        if (value instanceof Double) {
            double rounded = (int) Math.round(value.doubleValue() / step.doubleValue()) * step.doubleValue();
            if (rounded >= max.doubleValue()) rounded = max.doubleValue();
            next = Double.valueOf(min.doubleValue() < rounded ? rounded : min.doubleValue());
        } else if (value instanceof Float) {
            float rounded = Math.round(value.floatValue() / step.floatValue()) * step.floatValue();
            if (!(rounded < max.floatValue())) rounded = max.floatValue();
            next = Float.valueOf(min.floatValue() < rounded ? rounded : min.floatValue());
        } else if (value instanceof Long) {
            long bounded = value.longValue();
            next = value;
            if (bounded >= max.longValue()) next = Long.valueOf(max.longValue());
            if (next.longValue() <= min.longValue()) next = Long.valueOf(min.longValue());
        } else {
            int integer = ((Integer) value).intValue();
            int rounded = Math.round((float) integer / step.intValue()) * step.intValue();
            int bounded = rounded < max.intValue() ? rounded : max.intValue();
            next = Integer.valueOf(bounded > min.intValue() ? bounded : min.intValue());
        }
        Number old = getValue();
        if (setValueSilently(next)) notifyListeners(old, next);
    }

    public static String formatDegrees(NumberSetting setting) { return FORMAT.format(setting.getValue()) + "°"; }
    public static String formatMeters(NumberSetting setting) { return FORMAT.format(setting.getValue().doubleValue()) + "m"; }
    public static String formatSeconds(NumberSetting setting) { return FORMAT.format(setting.getValue().doubleValue()) + "s"; }

    public void increment() {
        Number old = getValue();
        double previous = old.doubleValue();
        double next = Math.min(previous + getEffectiveStep(), max.doubleValue());
        double rounded = (int) Math.round(next / step.doubleValue()) * step.doubleValue();
        if (rounded >= max.doubleValue()) rounded = max.doubleValue();
        if (!(min.doubleValue() < rounded)) rounded = min.doubleValue();
        Double boxed = Double.valueOf(rounded);
        if (setValueSilently(boxed)) {
            com.adjustclient.core.events.EventBus.post(new com.adjustclient.core.events.SettingChangeEvent(this, boxed));
            notifyListenersExcept(old, boxed);
            previousValue = null;
        }
        if (wrap && old.doubleValue() == getValue().doubleValue()) setValue(Double.valueOf(min.doubleValue()));
    }

    public void decrement() {
        Number old = getValue();
        double previous = old.doubleValue();
        double next = Math.max(previous - getEffectiveStep(), min.doubleValue());
        double rounded = (int) Math.round(next / step.doubleValue()) * step.doubleValue();
        if (rounded >= max.doubleValue()) rounded = max.doubleValue();
        if (!(min.doubleValue() < rounded)) rounded = min.doubleValue();
        Double boxed = Double.valueOf(rounded);
        if (setValueSilently(boxed)) {
            com.adjustclient.core.events.EventBus.post(new com.adjustclient.core.events.SettingChangeEvent(this, boxed));
            notifyListenersExcept(old, boxed);
            previousValue = null;
        }
        if (wrap && old.doubleValue() == getValue().doubleValue()) setValue(Double.valueOf(max.doubleValue()));
    }

    public float getEffectiveStep() {
        double amount = step.doubleValue();
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) amount *= 10.0;
        return (float) amount;
    }

    public String getFormattedValue() {
        return formatter == null ? FORMAT.format(getValue().doubleValue()) : formatter.transform(this);
    }

    @FunctionalInterface public interface Formatter {
        String transform(NumberSetting setting);
    }
}
