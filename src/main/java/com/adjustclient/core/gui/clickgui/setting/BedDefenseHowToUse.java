package com.adjustclient.core.gui.clickgui.setting;

import java.util.function.Supplier;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;

public final class BedDefenseHowToUse extends com.adjustclient.core.gui.clickgui.setting.group.CollapsibleHeaderComponent {

    public BedDefenseHowToUse(com.adjustclient.core.settings.Setting<?> setting) {
        super(setting, labelSupplier(setting));
    }

    static Supplier<String> labelSupplier(com.adjustclient.core.settings.Setting<?> setting) {
        return setting::getLabel;
    }

    @Override
    public void setExpanded(boolean expanded) {
        if (!expanded) {
            super.setExpanded(false);
            return;
        }
        boolean wasExpanded = this.expanded;
        super.setExpanded(true);
        if (wasExpanded) {
            return;
        }
        UserConfiguration tips = UserConfiguration.INSTANCE;
        if ((tips.timestamp & 2L) != 0L) {
            return;
        }
        tips.timestamp |= 2L;
        NotificationBuilder notification = new NotificationBuilder("How to Use",
                java.util.List.of(new TextComponent(
                        "You can sort and prioritize defenses by dragging them. Top defenses are prioritized first.", 0xFFBEBCBD)),
                Integer.valueOf(200));
        notification.key = "beddefenseshowtouse";
        HUDModule.INSTANCE_ALT.notifications.add(notification.build());
    }
}
