package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.user.ClientUser;
public final class PartyTextRowC extends PartyTextRow {
    public PartyTextRowC(ClientUser user, boolean declined) {
        super(new TextComponent[]{new UserColorText(user.name, user), new TextComponent(declined ? " declined the invite" : " didn't accept in time", 0xFF9B9B9B)});
    }
}
