package com.adjustclient.modules.impl.render;

import com.adjustclient.core.events.*;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import java.awt.Color;

public final class AtmosphereModule extends Module {
    public static AtmosphereModule INSTANCE;
    public final ExpandSetting time;
    public final BooleanSetting timeEnabled;
    public final NumberSetting timeValue;
    public final ModeSetting<LightingMode> lighting;
    public final ColorSetting lightingColor;
    public final NumberSetting brightness;
    public final BooleanSetting noNightVision;
    public final ExpandSetting fog;
    public final BooleanSetting fogEnabled;
    public final BooleanSetting customFogColor;
    public final ColorSetting fogColor;
    public final NumberSetting fogStrength;
    public final BooleanSetting customDensity;
    public final NumberSetting density;
    public final ModeSetting<FogMode> fogMode;

    public AtmosphereModule() {
        super("Atmosphere", Category.RENDER);
        INSTANCE = this;
        time = new ExpandSetting(this, "Time");
        time.enabledPredicate = new AtmosphereTimeCondition(this);
        timeEnabled = new BooleanSetting(time, "Enabled", true, "Yes", "No");
        timeValue = new NumberSetting(time, "Time", 12000, 0, 24000, 100);
        timeValue.formatter(setting -> formatTime(setting));
        timeValue.wrap = true;
        lighting = new ModeSetting<>(this, "Lighting", LightingMode.DEFAULT, LightingMode.values());
        lightingColor = new ColorSetting(this, "Lighting color", new Color(166, 166, 166), false, false,
                ColorSetting.Mode.PLAIN, () -> this.lighting.getValue() != LightingMode.CUSTOM);
        brightness = new NumberSetting(this, "Brightness", 1.0F, 0.0F, 1.0F, 0.01F,
                () -> this.lighting.getValue() != LightingMode.BRIGHT);
        noNightVision = new BooleanSetting(this, "No Night Vision", false);
        fog = new ExpandSetting(this, "Fog");
        fog.enabledPredicate = new AtmosphereFogCondition(this);
        fogEnabled = new BooleanSetting(fog, "Enabled", false, "Yes", "No");
        customFogColor = new BooleanSetting(fog, "Custom color", true);
        fogColor = new ColorSetting(fog, "Color", new Color(77, 6, 6), false, false,
                ColorSetting.Mode.PLAIN, () -> !this.customFogColor.isEnabled());
        fogStrength = new NumberSetting(fog, "Color Strength", 25.0F, 0.0F, 100.0F, 1.0F,
                () -> !this.customFogColor.isEnabled());
        customDensity = new BooleanSetting(fog, "Custom Density", true);
        density = new NumberSetting(fog, "Density", 1.8F, 0.0F, 20.0F, 0.05F,
                () -> !this.customDensity.isEnabled());
        fogMode = new ModeSetting<>(fog, "Mode", FogMode.VANILLA,
                () -> !this.customDensity.isEnabled(), FogMode.values());
        setHidden(false);
    }

    @EventHandler public void onLighting(LightmapStateEvent event) {
        if (!isEnabled()) return;
        if (lighting.getValue() != LightingMode.DEFAULT) event.customColors = false;
        if (noNightVision.isEnabled()) event.nightVision = false;
    }

    @EventHandler public void onFogColor(FogRgbEvent event) {
        if (!isEnabled() || !fogEnabled.isEnabled() || !customFogColor.isEnabled()) return;
        float strength = fogStrength.getFloat() / 100.0F;
        Color color = fogColor.getColor();
        event.red += (color.getRed() / 255.0F - event.red) * strength;
        event.green += (color.getGreen() / 255.0F - event.green) * strength;
        event.blue += (color.getBlue() / 255.0F - event.blue) * strength;
    }

    @EventHandler public void onFog(FogParametersEvent event) {
        if (!isEnabled() || !fogEnabled.isEnabled()) return;
        if (!customFogColor.isEnabled() && !customDensity.isEnabled()) return;
        event.fogStandard = true;
        if (!customDensity.isEnabled()) return;
        float value = density.getFloat();
        float range = Math.max(1.0F, event.farPlaneDistance);
        if (value <= 0.0F) {
            event.mode = 9729;
            event.start = range * 4.0F;
            event.end = event.start + 1.0F;
        } else if (event.radial) {
            event.density *= value;
        } else {
            switch (fogMode.getValue()) {
                case VANILLA:
                    event.start /= value;
                    event.end /= value;
                    break;
                case SOFT:
                    float scale = range * 1024.0F;
                    float end = scale * (1.0F - value / 20.0F);
                    event.start = end - scale;
                    event.end = end;
                    break;
                case CONSTANT:
                    event.mode = 2048;
                    event.density = value * 3.0F / range;
                    break;
            }
        }
    }

    @EventHandler public void onBrightness(GammaEvent event) {
        if (!isEnabled() || lighting.getValue() != LightingMode.BRIGHT) return;
        float value = brightness.getFloat();
        event.gamma = value < 0.005F ? 0.0375F : value * 7.5F;
    }

    public static String formatTime(NumberSetting setting) {
        int ticks = setting.getInt();
        String minutes = Integer.toString((int) ((ticks % 1000) * 0.06F));
        if (minutes.length() == 1) minutes = "0" + minutes;
        return ticks / 1000 + ":" + minutes;
    }

    public boolean isNightVisionDisabled() { return isEnabled() && noNightVision.isEnabled(); }
}
