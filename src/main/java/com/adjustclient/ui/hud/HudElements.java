package com.adjustclient.ui.hud;

import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.ui.hud.information.Information;
import com.adjustclient.ui.hud.information.InformationSettings;
import com.adjustclient.ui.hud.potion.PotionEffects;
import com.adjustclient.ui.hud.tabgui.TabGuiSettings;
import com.adjustclient.ui.hud.toggled.ToggledList;
import com.adjustclient.ui.hud.toggled.ToggledSettings;
import com.adjustclient.ui.hud.watermark.Watermark;
import com.adjustclient.ui.tabgui.TabGui;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.util.function.BooleanSupplier;

public final class HudElements {

    public HudElement[] elements;
    public Object field8;
    public ToggledList toggled;
    public Object field16;

    public HudElements(HUDModule hud) {
        TabGuiSettings tabGuiSettings = new TabGuiSettings(new Color(19, 19, 19, 255), hud);
        ExpandSetting toggledGroup = new ExpandSetting(hud, "Toggled");
        ToggledSettings toggledSettings = new ToggledSettings(toggledGroup, this.toggledEnabledSupplier());
        ExpandSetting infoGroup = new ExpandSetting(hud, "Information");
        InformationSettings infoSettings = new InformationSettings(infoGroup);
        ToggledList toggledList = new ToggledList(toggledSettings);
        this.toggled = toggledList;
        Information information = new Information(infoSettings);
        TabGui tabGui = new TabGui(tabGuiSettings);
        this.field8 = tabGui;
        PotionEffects potionEffects = new PotionEffects();
        Watermark watermark = new Watermark();
        this.elements = new HudElement[] { watermark, toggledList, information, tabGui, potionEffects };
    }

    public void render(ScaledResolution sr) {
        for (HudElement element : this.elements) {
            element.render(sr);
        }
    }

    BooleanSupplier toggledEnabledSupplier() {
        return () -> this.toggled.settings.enabled.isEnabled();
    }
}
