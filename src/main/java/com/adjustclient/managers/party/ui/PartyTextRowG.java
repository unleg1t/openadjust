package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.user.ClientUser;
public class PartyTextRowG extends PartyTextRow {
    public PartyTextRowG(ClientUser user) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(" joined", 0xFF9B9B9B)});
    }
}
