package com.adjustclient.core.gui.clickgui.frame;

import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.gui.clickgui.ClickGuiComponent;
import com.adjustclient.utilities.TimeUtil;
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
        long lastUsed = ((com.adjustclient.managers.config.ConfigurationManager) this.configurationData).lastUsed;
        if (lastUsed == 0L) {
            return "Never used";
        }
        String color = ChatColor.ITALIC != null ? ChatColor.ITALIC.toString() : "null";
        return color + "Last used " + TimeUtil.timeAgo(lastUsed, " ago");
    }
}
