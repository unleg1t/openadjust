package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.*;
import java.awt.Color;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class ItemEspSettings extends EntityEspSettings implements EspBarSettings {
    public final ModeSetting<EspItemFilter> filter;
    public final ItemsSetting selectedItems;
    public final ExpandSetting durability;
    public final BooleanSetting durabilityEnabled;
    public final BooleanSetting durabilityThinner;
    public final ColorSetting durabilityColor;
    public final BooleanSetting validate;
    public ItemEspSettings(SettingContainer parent) {
        super(parent, "Items", true, true);
        nametags.getSettings().remove(nametagColor);
        filter = new ModeSetting<>(target, "Filter", EspItemFilter.BLACKLIST, EspItemFilter.values());
        target.addSetting(filter, 1);
        IntArrayList availableIds = new IntArrayList();
        for (net.minecraft.item.Item item : net.minecraft.item.Item.itemRegistry)
            availableIds.add(net.minecraft.item.Item.itemRegistry.getIDForObject(item));
        selectedItems = new ItemsSetting(target, "Selected items", new IntArrayList(), availableIds, null);
        target.addSetting(selectedItems, 2);
        durability = new ExpandSetting(target, "Durability"); durabilityEnabled = new BooleanSetting(durability, "Enabled", true, "Yes", "No");
        durability.enabledPredicate = () -> durabilityEnabled.isEnabled();
        durabilityThinner = new BooleanSetting(durability, "Thinner", false); durabilityColor = new ColorSetting(durability, "Color", new Color(77, 88, 199, 255)); validate = new BooleanSetting(target, "Validate", true);
    }
    public boolean acceptsItem(int id) { return selectedItems.contains(id) == (filter.getValue() == EspItemFilter.WHITELIST); }
    public int getDurabilityColor() { return durabilityColor.getColor().getRGB(); }
    public boolean isValidating() { return durabilityEnabled.isEnabled(); }
}
