package com.adjustclient.module.impl.render;

import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.NumberSetting;
import net.minecraft.entity.player.EntityPlayer;

public final class TrueSightModule extends Module {
    public static TrueSightModule INSTANCE;
    public final NumberSetting opacity;
    public final BooleanSetting ignoreTeammates;
    public TrueSightModule() {
        super("True Sight", Category.RENDER);
        INSTANCE = this;
        opacity = new NumberSetting(this, "Opacity", 50, 10, 100, 1);
        ignoreTeammates = new BooleanSetting(this, "Ignore teammates", false);
    }
    @com.adjustclient.event.EventHandler
    public void apply(com.adjustclient.event.EntityOpacityEvent event) {
        if (!isEnabled()) return;
        if (com.adjustclient.util.MinecraftInstance.mc.thePlayer == null) return;
        if (!(event.entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entity;
        if (com.adjustclient.module.impl.combat.antibot.AntiBotModule.isBot(player.getEntityId())) return;
        if (ignoreTeammates.isEnabled() && com.adjustclient.util.WorldUtil.isTeammate(player)) return;
        if (!event.invisible) return;
        event.invisible = false;
        event.bodyAlpha = opacity.getValue().floatValue() / 100.0F;
    }
    @Override public String getDescription() { return "Allows you to see invisible players."; }
}
