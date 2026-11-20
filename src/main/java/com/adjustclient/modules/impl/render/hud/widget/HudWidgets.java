package com.adjustclient.modules.impl.render.hud.widget;

import com.adjustclient.modules.impl.render.hud.widget.radar.RadarWidget;
import com.adjustclient.core.settings.ExpandSetting;

import java.util.List;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.utilities.game.movable.MovableManager;

public final class HudWidgets {

    public final List<HudWidget> widgets;

    public HudWidgets() {
        this.widgets = List.of(
                new RadarWidget(createMovable("widgetRadar")),
                new SessionInformationWidget(createMovable("widgetSessionInformation")),
                new ClickSpeedWidget(createMovable("widgetClickSpeed")),
                new MemoryUsageWidget(createMovable("widgetMemoryUsage")));
    }

    private static Movable createMovable(String id) {
        com.adjustclient.utilities.Vec2f position = switch (id) {
            case "widgetRadar" -> new com.adjustclient.utilities.Vec2f(2.0F, 150.0F);
            case "widgetSessionInformation" -> new com.adjustclient.utilities.Vec2f(2.0F, 250.0F);
            case "widgetClickSpeed" -> new com.adjustclient.utilities.Vec2f(350.0F, 250.0F);
            case "widgetMemoryUsage" -> new com.adjustclient.utilities.Vec2f(350.0F, 280.0F);
            default -> throw new IllegalArgumentException(id);
        };
        return com.adjustclient.Adjust.movableManager.registerMovable(id, position);
    }

    /** Lets every widget create its "Enabled" setting (and, for the Radar, its whole group) in HUD > Widgets. */
    public void createSettings(ExpandSetting widgetsGroup) {
        this.widgets.forEach(widget -> widget.createSettings(widgetsGroup));
    }
}
