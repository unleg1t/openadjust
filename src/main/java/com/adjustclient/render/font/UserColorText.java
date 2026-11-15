package com.adjustclient.render.font;

import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.user.ClientUser;
import java.util.concurrent.atomic.AtomicInteger;

public class UserColorText extends TextComponent {
    public ClientUser user;
    public UserColorText(String text, ClientUser user) { super(text, 0); this.user = user; }
    public int getColor() { return user.getColor(); }
}
