package com.adjustclient.core.rendering.font;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.managers.user.ClientUser;
import java.util.concurrent.atomic.AtomicInteger;

public class UserColorText extends TextComponent {
    public ClientUser user;
    public UserColorText(String text, ClientUser user) { super(text, 0); this.user = user; }
    public int getColor() { return user.getColor(); }
}
