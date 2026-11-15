package com.adjustclient.ui.tabgui.component;

import com.adjustclient.render.NvgRenderer;
import com.adjustclient.setting.StringSetting;
import java.util.List;
import net.minecraft.util.ChatColor;
import com.adjustclient.ui.tabgui.SettingNode;
import com.adjustclient.render.font.Fonts;

public final class StringListComponent extends SettingNode<StringSetting> {

    public StringListComponent(String label, StringSetting setting) {
        super(label, setting);
    }

    @Override
    public void render(NvgRenderer dl, Object context, boolean hovered, float x, float y, float width) {
        StringSetting setting = this.setting;
        List<String> values = setting.getValue();

        boolean allEmpty = true;
        for (String s : values) {
            if (!s.isEmpty()) {
                allEmpty = false;
                break;
            }
        }

        dl.color(this.getTextColor(false, true));

        String prefix = this.label + ChatColor.BOLD;
        int max = setting.getMaxEntries();
        String text;
        if (max < 2) {
            text = prefix + (allEmpty ? " (0)" : " (1)");
        } else {
            text = prefix + " (" + values.size() + "/" + max + ")";
        }
        dl.text(Fonts.TAHOMA_10, text, x + 2.0F, y + 3.0F);
    }
}
