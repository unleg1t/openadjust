package com.adjustclient.module.impl.movement;

import com.adjustclient.setting.ModeOption;

public enum PearlSimulationIntensity implements ModeOption {
    EASY("Easy", 25),
    MEDIUM("Medium", 65),
    INTENSE("Intense", 130),
    EXTREME("Extreme", 280);

    private final String displayName;
    public final int simulationSteps;

    PearlSimulationIntensity(String displayName, int simulationSteps) {
        this.displayName = displayName;
        this.simulationSteps = simulationSteps;
    }

    @Override public String getName() { return displayName; }

}
