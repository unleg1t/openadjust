package com.adjustclient.modules.impl.render;

import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.NumberSetting;
import net.minecraft.entity.player.EntityPlayer;

public final class TrueSightModule extends Module {
    public static TrueSightModule INSTANCE;
    public final NumberSetting opacity;
    public final BooleanSetting ignoreTeammates;
    public TrueSightModule() {
        super("True Sight", Category.RENDER);
        INSTANCE = this;
        opacity = new NumberSetting(this, "Opacity", 50, 10, 100, 1)
                .formatter(NumberSetting.INTEGER_SUFFIX_B);
        ignoreTeammates = new BooleanSetting(this, "Ignore teammates", false);
    }
    @com.adjustclient.core.events.EventHandler
    public void apply(com.adjustclient.core.events.EntityOpacityEvent event) {
        if (!isEnabled()) return;
        if (com.adjustclient.utilities.MinecraftInstance.mc.thePlayer == null) return;
        if (!(event.entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entity;
        if (com.adjustclient.modules.impl.combat.antibot.AntiBotModule.isBot(player.getEntityId())) return;
        if (ignoreTeammates.isEnabled() && com.adjustclient.utilities.WorldUtil.isTeammate(player)) return;
        if (!event.invisible) return;
        event.invisible = false;
        event.bodyAlpha = opacity.getValue().floatValue() / 100.0F;
    }
    @Override public String getDescription() { return "Allows you to see invisible players."; }
}
