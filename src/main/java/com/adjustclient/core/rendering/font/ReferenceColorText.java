package com.adjustclient.core.rendering.font;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.managers.user.ClientUser;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceColorText extends TextComponent {
    public AtomicInteger colorReference;
    public ReferenceColorText(String text, AtomicInteger colorReference) {
        super(text, 0); this.colorReference = colorReference;
    }
    public int getColor() { return colorReference.get(); }
}
