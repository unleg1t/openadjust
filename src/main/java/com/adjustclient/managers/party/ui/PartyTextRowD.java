package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.user.ClientUser;
public final class PartyTextRowD extends PartyTextRow {
    public PartyTextRowD(ClientUser user, ClientUser other) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(" invited ", 0xFF9B9B9B), new UserColorText(other.name, other)});
    }
}
