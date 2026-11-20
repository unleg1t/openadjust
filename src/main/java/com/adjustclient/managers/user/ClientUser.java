package com.adjustclient.managers.user;

import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.managers.friends.Friend;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.managers.friends.BlockListService;

public final class ClientUser implements MinecraftInstance {
    public String name;
    public Friend friend;
    public Cape cape;
    public CustomCape customCape;
    public String displayName;
    public int color;
    public boolean useClientColor;
    public Object field28;

    public ClientUser(String name) {
        this.name = name;
        cape = Cape.ADJUST;
        color = -11431976;
        useClientColor = name.equalsIgnoreCase(Adjust.username);
    }

    public void setFriend(boolean value) {
        if ((friend != null) == value) return;
        if (value) {
            friend = new Friend(this);
            if (BlockListService.INSTANCE != null) BlockListService.INSTANCE.friends.add(friend);
        } else {
            if (BlockListService.INSTANCE != null) BlockListService.INSTANCE.friends.remove(friend);
            friend = null;
        }
    }

    public int getColor() {
        if (!useClientColor) return color;
        if (HUDModule.animatedColor != null) return HUDModule.animatedColor.getRGB();
        return HUDModule.colorSetting.getColor().getRGB();
    }
}
