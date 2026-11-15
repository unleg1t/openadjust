package com.adjustclient.service.party.ui;
import com.adjustclient.render.font.*;
import com.adjustclient.user.ClientUser;
public class PartyTextRowF extends PartyTextRow {
    public ClientUser user;
    public PartyTextRowF(ClientUser user) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(" disconnected", 0xFF9B9B9B)});
        this.user = user;
    }
}
