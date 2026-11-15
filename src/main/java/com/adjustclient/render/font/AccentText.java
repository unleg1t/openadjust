package com.adjustclient.render.font;

import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.user.ClientUser;
import java.util.concurrent.atomic.AtomicInteger;

public final class AccentText extends TextComponent {
    public AccentText(String text) { super(text, 0); }
    public int getColor() { return (HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor()).getRGB(); }
}
