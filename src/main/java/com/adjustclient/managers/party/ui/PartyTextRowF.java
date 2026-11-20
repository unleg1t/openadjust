package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.user.ClientUser;
public class PartyTextRowF extends PartyTextRow {
    public ClientUser user;
    public PartyTextRowF(ClientUser user) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(" disconnected", 0xFF9B9B9B)});
        this.user = user;
    }
}
