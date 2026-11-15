package com.adjustclient.module.impl.render.hud.playerlist;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import java.awt.Color;
import com.adjustclient.util.BiFunction2;
import net.minecraft.client.Minecraft;

public enum RowTint {
    NONE, SUSPICIOUS, HACKING, BLACKLISTED, WHITELISTED, TARGETED, VANISHED, STAFF;

    public BiFunction2<Object, ? super PlayerEntry> tint;
    public BiFunction2<Object, ? super PlayerEntry> secondaryTint;

    static final BiFunction2<Object, Object> LAMBDA_373A = (base, entry) -> 0;

    static final BiFunction2<Object, Object> LAMBDA_5AA8 = (base, entry) -> 0x5F9A56DE;

    static final BiFunction2<Object, Object> LAMBDA_6E79 = (base, entry) -> 0x5FC84D4D;

    static final BiFunction2<Object, Object> LAMBDA_6F3F = (base, entry) -> 0x5FFF8484;

    static final BiFunction2<Object, Object> LAMBDA_9AFA = (base, entry) -> 0x5F8484FF;

    static final BiFunction2<Object, PlayerEntry> LAMBDA_C1AC = (base, entry) -> {
        net.minecraft.entity.player.EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        com.mojang.authlib.GameProfile ownProfile = self.getGameProfile();
        com.mojang.authlib.GameProfile profile = entry.player.info.getGameProfile();
        if (profile == ownProfile || profile.equals(ownProfile)) {
            return (Integer) base;
        }
        double v = entry.progress.doubleValue() / 5.0;
        int alpha = (int) (v * 45.0 + 35.0);
        if (v > 1.0) {
            alpha = 80;
        }
        return new Color(255, 217, 99, alpha).getRGB();                      // boxed Integer
    };

    static final BiFunction2<Object, Object> LAMBDA_D023 = (base, entry) -> 0x5FDD52E7;

    static final BiFunction2<Object, Object> LAMBDA_D33C = (base, entry) -> 0x32FF6363;

    static {
        NONE.tint = LAMBDA_373A;
        SUSPICIOUS.tint = LAMBDA_C1AC;
        HACKING.tint = LAMBDA_D33C;
        BLACKLISTED.tint = LAMBDA_6F3F;
        WHITELISTED.tint = LAMBDA_9AFA;
        TARGETED.tint = LAMBDA_6E79;
        VANISHED.tint = LAMBDA_D023;
        STAFF.tint = LAMBDA_5AA8;
    }

}
