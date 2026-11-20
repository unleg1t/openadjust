package com.adjustclient.core.gui.hud.information;

import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.ModeSetting;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class InformationSettings {

    public BooleanSetting coordinatesEnabled;
    public BooleanSetting netherCoordinates;
    public BooleanSetting angles;
    public BooleanSetting direction;
    public BooleanSetting serverside;
    public BooleanSetting speedEnabled;
    public ModeSetting<SpeedUnit> speedFormat;
    public BooleanSetting tps;
    public BooleanSetting fps;
    public Object field40;

    public InformationSettings(ExpandSetting information) {
        // --- Coordinates group (header supplier = coordinatesEnabled) ---
        ExpandSetting coordinates = new ExpandSetting(information, "Coordinates");
        coordinates.enabledPredicate = (BooleanSupplier) () -> this.coordinatesEnabled.isEnabled();
        this.coordinatesEnabled = new BooleanSetting(coordinates, "Enabled", false, "Yes", "No");
        this.netherCoordinates = new BooleanSetting(coordinates, "Show nether coordinates", true, "On", "Off");

        // --- Angles group (header supplier = angles) ---
        ExpandSetting anglesGroup = new ExpandSetting(information, "Angles");
        anglesGroup.enabledPredicate = (BooleanSupplier) () -> this.angles.isEnabled();
        this.angles = new BooleanSetting(anglesGroup, "Angles", false, "On", "Off");
        this.direction = new BooleanSetting(anglesGroup, "Direction", true, "On", "Off");
        this.serverside = new BooleanSetting(anglesGroup, "Serverside", false, "On", "Off");
        this.serverside.description = "Displays your serverside angles.";

        // --- Speed group (header supplier = speedEnabled) ---
        ExpandSetting speed = new ExpandSetting(information, "Speed");
        speed.enabledPredicate = (BooleanSupplier) () -> this.speedEnabled.isEnabled();
        this.speedEnabled = new BooleanSetting(speed, "Enabled", true, "Yes", "No");
        this.speedFormat = new ModeSetting<>(speed, "Format", SpeedUnit.BPS, List.of(SpeedUnit.BPS, SpeedUnit.KMPH));

        // --- plain settings directly in the Information group ---
        this.tps = new BooleanSetting(information, "TPS", false, "On", "Off");
        this.fps = new BooleanSetting(information, "FPS", false, "On", "Off");
    }
}
