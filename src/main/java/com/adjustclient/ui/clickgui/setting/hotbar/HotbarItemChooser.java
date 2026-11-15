package com.adjustclient.ui.clickgui.setting.hotbar;

import com.adjustclient.setting.ItemsSetting;
import com.adjustclient.ui.clickgui.setting.items.ItemListComponent;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class HotbarItemChooser extends ItemListComponent {

    HotbarItemChooser(ItemsSetting setting) {
        super(setting, false);
    }

    @Override
    public ItemStack getStack(int id) {
        Item item = Item.getItemById(id);
        Integer meta = item == null ? null : HotbarSettingComponent.POTIONS.get(item);
        if (meta == null) {
            return new ItemStack(item, 1, 0);
        }
        return new ItemStack(Items.potionitem, 1, Math.max(meta, 0));
    }

    @Override
    public int getAccentColor() {
        return 0xFFC8C8C8; // 4291348680
    }
}
