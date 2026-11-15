package com.adjustclient.service.party.ui;
import com.adjustclient.render.font.*;
import com.adjustclient.user.ClientUser;
public class PartyTextRowH extends PartyTextRow {
    public PartyTextRowH(ClientUser user) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(" left", 0xFF9B9B9B)});
    }
}
