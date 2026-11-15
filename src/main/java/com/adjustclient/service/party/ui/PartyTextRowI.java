package com.adjustclient.service.party.ui;
import com.adjustclient.render.font.*;
import com.adjustclient.user.ClientUser;
public class PartyTextRowI extends PartyTextRow {
    public ClientUser user;
    public PartyTextRowI(ClientUser user) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(" reconnected", 0xFF9B9B9B)});
        this.user = user;
    }
}
