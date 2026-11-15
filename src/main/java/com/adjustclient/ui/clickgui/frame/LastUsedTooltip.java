package com.adjustclient.ui.clickgui.frame;

import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.ui.clickgui.ClickGuiComponent;
import com.adjustclient.util.TimeUtil;
import net.minecraft.util.ChatColor;
public class LastUsedTooltip extends Tooltip {

    protected int unused48;

    public LastUsedTooltip(ClickGuiComponent owner, Object configuration, java.util.function.BooleanSupplier hovered) {
        super(owner, "", hovered);
        this.extended = "";
        this.configurationData = configuration;
        this.delay = 250L;
    }

    @Override
    public String getText() {
        long lastUsed = ((com.adjustclient.service.config.ConfigurationManager) this.configurationData).lastUsed;
        if (lastUsed == 0L) {
            return "Never used";
        }
        String color = ChatColor.ITALIC != null ? ChatColor.ITALIC.toString() : "null";
        return color + "Last used " + TimeUtil.timeAgo(lastUsed, " ago");
    }
}
