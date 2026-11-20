package com.adjustclient.core.gui.hud;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.gui.hud.information.Information;
import com.adjustclient.core.gui.hud.information.InformationSettings;
import com.adjustclient.core.gui.hud.potion.PotionEffects;
import com.adjustclient.core.gui.hud.tabgui.TabGuiSettings;
import com.adjustclient.core.gui.hud.toggled.ToggledList;
import com.adjustclient.core.gui.hud.toggled.ToggledSettings;
import com.adjustclient.core.gui.hud.watermark.Watermark;
import com.adjustclient.core.gui.tabgui.TabGui;
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
