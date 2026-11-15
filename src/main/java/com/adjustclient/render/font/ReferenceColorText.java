package com.adjustclient.render.font;

import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.user.ClientUser;
import java.util.concurrent.atomic.AtomicInteger;

public class ReferenceColorText extends TextComponent {
    public AtomicInteger colorReference;
    public ReferenceColorText(String text, AtomicInteger colorReference) {
        super(text, 0); this.colorReference = colorReference;
    }
    public int getColor() { return colorReference.get(); }
}
