package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.*;
import java.awt.Color;

public class MobEspSettings extends LivingEspSettings implements EspBarSettings {
    public final ExpandSetting armor;
    public final BooleanSetting armorEnabled;
    public final BooleanSetting armorThinner;
    public final ModeSetting<EspBarPosition> armorPosition;
    public final ColorSetting armorColor;
    public final ExpandSetting icons;
    public final BooleanSetting equipment;
    public final BooleanSetting potions;
    public final ExpandSetting itemName;
    public final BooleanSetting itemNameEnabled;
    public final BooleanSetting customItemNamesOnly;
    public final NumberSetting itemNameScale;
    public final BooleanSetting fovAlpha;
    public MobEspSettings(SettingContainer parent, String name, boolean enabled, boolean dormant) {
        super(parent, name, enabled, dormant);
        armor = new ExpandSetting(target, "Armor");
        armorEnabled = new BooleanSetting(armor, "Enabled", true, "Yes", "No");
        armor.enabledPredicate = () -> armorEnabled.isEnabled();
        armorThinner = new BooleanSetting(armor, "Thinner", false);
        armorPosition = new ModeSetting<>(armor, "Position", EspBarPosition.BOTTOM, EspBarPosition.values());
        armorColor = new ColorSetting(armor, "Color", new Color(77, 88, 199, 255));
        itemName = new ExpandSetting(information, "Item name");
        itemNameEnabled = new BooleanSetting(itemName, "Enabled", true, "Yes", "No");
        itemName.enabledPredicate = () -> itemNameEnabled.isEnabled();
        customItemNamesOnly = new BooleanSetting(itemName, "Custom names only", false);
        itemNameScale = new NumberSetting(itemName, "Scale", 1.0F, 1.0F, 1.5F, 0.05F);
        java.util.function.BooleanSupplier previousInformationEnabled = information.enabledPredicate;
        information.enabledPredicate = () -> previousInformationEnabled.getAsBoolean() || itemNameEnabled.isEnabled();
        information.getSettings().remove(background);
        information.getSettings().add(background);
        icons = new ExpandSetting(target, "Icons"); equipment = new BooleanSetting(icons, "Equipment", true); potions = new BooleanSetting(icons, "Potions", true);
        icons.enabledPredicate = () -> equipment.isEnabled() || potions.isEnabled();
        fovAlpha = new BooleanSetting(icons, "FOV alpha", true);
    }
    public EspBarPosition getArmorPosition() { return armorPosition.getValue(); }
    public int getArmorColor() { return armorColor.getColor().getRGB(); }
    public boolean equipmentEnabled() { return armorEnabled.isEnabled(); }
    public boolean potionsEnabled() { return armorThinner.isEnabled(); }
}
