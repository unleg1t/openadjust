package com.adjustclient.core.gui.clickgui;

import com.adjustclient.core.settings.BlocksSetting;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ButtonSetting;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.HotbarSetting;
import com.adjustclient.core.settings.ItemsSetting;
import com.adjustclient.core.settings.KeybindSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.settings.Setting;
import com.adjustclient.core.settings.StringSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.adjustclient.modules.impl.combat.builder.feature.beddefense.BedDefenseSetting;
import com.adjustclient.modules.impl.render.esp.EspPreviewSetting;
import com.adjustclient.core.gui.clickgui.setting.BedDefenseHowToUse;
import com.adjustclient.core.gui.clickgui.setting.BedDefenseSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.BooleanSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.ButtonSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.ExpandSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.KeybindSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.ModeSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.NumberSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.SettingSeparatorComponent;
import com.adjustclient.core.gui.clickgui.setting.color.ColorModeComponent;
import com.adjustclient.core.gui.clickgui.setting.color.ColorPicker;
import com.adjustclient.core.gui.clickgui.setting.color.ColorSettingComponent;
import com.adjustclient.core.gui.clickgui.setting.items.ItemListComponent;
import com.adjustclient.core.gui.clickgui.setting.string.StringEditorComponent;
import com.adjustclient.core.gui.clickgui.setting.string.StringSettingComponent;

public final class ComponentFactory {
    private ComponentFactory() {
    }

    /** Setting -> row component (null when the setting type has no row). The switch is over the exact runtime class of the setting. */
    public static ClickGuiComponent createSettingComponent(Setting<?> setting) {
        if (setting == null) {
            return null;
        }
        Class<?> type = setting.getClass();
        if (type == BooleanSetting.class) {
            return new BooleanSettingComponent((BooleanSetting) setting);
        }
        if (type == NumberSetting.class) {
            return new NumberSettingComponent((NumberSetting) setting);
        }
        if (type == KeybindSetting.class) {
            return new KeybindSettingComponent((KeybindSetting) setting);  // keybind row ("None", "+", "-")
        }
        if (type == ExpandSetting.class) {
            return new ExpandSettingComponent((ExpandSetting) setting);   // group header row
        }
        if (type == ModeSetting.class) {
            return new ModeSettingComponent((ModeSetting<?>) setting);
        }
        if (type == ColorSetting.class) {
            return new ColorSettingComponent((ColorSetting) setting);
        }
        if (type == StringSetting.class) {
            return new StringSettingComponent((StringSetting) setting);
        }
        // default branch
        if (setting instanceof ItemsSetting) {
            Supplier<String> label = () -> ((Setting<?>) setting).getLabel();
            return new com.adjustclient.core.gui.clickgui.setting.group.CollapsibleHeaderComponent((ItemsSetting) setting, label);
        }
        if (type == EspPreviewSetting.class) {        // class literal; no readable name known (module-specific setting)
            Supplier<String> label = () -> ((Setting<?>) setting).getLabel();
            return new com.adjustclient.core.gui.clickgui.setting.group.CollapsibleHeaderComponent((Setting<?>) setting, label);
        }
        if (type == BedDefenseSetting.class) {
            return new BedDefenseHowToUse(setting);
        }
        if (type == HotbarSetting.class) {
            Supplier<String> label = () -> ((Setting<?>) setting).getLabel();
            return new com.adjustclient.core.gui.clickgui.setting.group.CollapsibleHeaderComponent((Setting<?>) setting, label);
        }
        if (type == ButtonSetting.class) {
            return new ButtonSettingComponent((ButtonSetting) setting);
        }
        return null;
    }

    public static void build(ClickGuiTab tab, ClickGuiComponent parent, List<Setting<?>> settings) {
        if (settings == null) {
            return;
        }
        for (Setting<?> setting : settings) {
            ClickGuiComponent row = createSettingComponent(setting);
            if (row == null) {
                continue;
            }
            row.setParent(parent);
            Class<?> type = setting.getClass();
            ArrayList<ClickGuiComponent> created = new ArrayList<>();   // rows that belong to this setting, registered at the end
            created.add(row);
            if (type == ColorSetting.class) {
                created.add(attachChild(row, ColorModeComponent.create((ColorSettingComponent) row, (ColorSetting) setting)));
                created.add(attachChild(row, new ColorPicker((ColorSetting) setting)));
            } else if (setting instanceof ItemsSetting) {
                created.add(attachChild(row, new ItemListComponent((ItemsSetting) setting)));   // list editor (ItemListComponent + e_d)
            } else if (type == BedDefenseSetting.class) {
                created.add(attachChild(row, new BedDefenseSettingComponent((BedDefenseSetting) setting)));   // BedDefenseSettingComponent
            } else if (type == HotbarSetting.class) {
                created.add(attachChild(row, new com.adjustclient.core.gui.clickgui.setting.hotbar.HotbarSettingComponent((HotbarSetting) setting)));
            } else if (type == EspPreviewSetting.class) {
                created.add(attachChild(row, new com.adjustclient.core.gui.clickgui.setting.preview.EspPreviewComponent((EspPreviewSetting) setting)));
            } else if (type == StringSetting.class) {
                StringEditorComponent editor = new StringEditorComponent((StringSetting) setting);   // StringEditorComponent
                ((StringSettingComponent) row).attachEditor(editor);
                created.add(attachChild(row, editor));
            }
            for (ClickGuiComponent c : created) {
                c.owner = tab;
                tab.entries.add(c);
                tab.screen.componentMap.put(setting, c);
            }
            if (type == ExpandSetting.class) {
                build(tab, row, ((ExpandSetting) setting).getSettings());
                SettingSeparatorComponent footer = new SettingSeparatorComponent();                    // ModuleEntry footer / "end of group" row
                row.children.add(footer);
                footer.parent = row;
                footer.owner = tab;
                tab.entries.add(footer);
            }
        }
    }

    private static ClickGuiComponent attachChild(ClickGuiComponent parent, ClickGuiComponent child) {
        child.setParent(parent);
        return child;
    }

}
