package com.adjustclient.module.impl.player.inventory.quickbuy;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindKey;
import com.adjustclient.bind.KeyboardKey;
import com.adjustclient.bind.ScrollKey;
import com.adjustclient.setting.*;
import java.util.EnumMap;
import java.util.Map;

public final class QuickBuySettings {
    public BooleanSetting enabled;
    public final EnumMap<QuickBuyItem, KeybindSetting> keybinds = new EnumMap<>(QuickBuyItem.class);
    public final EnumMap<QuickBuyItem, BooleanSetting> quickSettings = new EnumMap<>(QuickBuyItem.class);
    public final EnumMap<QuickBuyItem, NumberSetting> quickslots = new EnumMap<>(QuickBuyItem.class);

    public QuickBuySettings(ExpandSetting parent) {
        ExpandSetting group = new ExpandSetting(parent, "Quick Buy");
        group.enabledPredicate = () -> enabled.isEnabled();
        enabled = new BooleanSetting(group, "Enabled", false, "Yes", "No");
        for (QuickBuyItem item : QuickBuyItem.values()) {
            Bind bind = Adjust.INSTANCE.bindManager.register("inventory.quickbuy." + item.id, null, "QuickBuy " + item.displayName);
            ExpandSetting entry = new ExpandSetting(group, item.displayName);
            entry.enabledPredicate = () -> !bind.keys.isEmpty();
            keybinds.put(item, new KeybindSetting(entry, "Keybind", bind));
            quickSettings.put(item, new BooleanSetting(entry, "Quick", false));
            if (item.category.slot >= 0) {
                NumberSetting slot = new NumberSetting(entry, "Quickslot", 0, 0, 9, 1);
                slot.formatter(setting -> setting.getInt() == 0 ? "None" : Integer.toString(setting.getInt()));
                quickslots.put(item, slot);
            }
        }
    }

    public QuickBuyItem findKeyboard(int code, ShopCategory page) {
        for (Map.Entry<QuickBuyItem, KeybindSetting> entry : keybinds.entrySet()) {
            QuickBuyItem item = entry.getKey();
            if (page == ShopCategory.QUICK_BUY ? item.category.slot < 0 : item.category != page) continue;
            for (BindKey key : entry.getValue().getValue().keys)
                if (key instanceof KeyboardKey && key.equals(new KeyboardKey(code))) return item;
        }
        return null;
    }

    public int getQuickslot(QuickBuyItem item) {
        NumberSetting setting = quickslots.get(item);
        return setting == null || setting.getInt() == 0 ? -1 : setting.getInt() - 1;
    }

    public QuickBuyItem findScroll(int delta, ShopCategory page) {
        if (delta == 0) return null;
        ScrollKey key = ScrollKey.fromWheel(delta);
        for (Map.Entry<QuickBuyItem, KeybindSetting> entry : keybinds.entrySet()) {
            QuickBuyItem item = entry.getKey();
            if (page == ShopCategory.QUICK_BUY ? item.category.slot < 0 : item.category != page) continue;
            if (entry.getValue().getValue().keys.contains(key)) return item;
        }
        return null;
    }
}
