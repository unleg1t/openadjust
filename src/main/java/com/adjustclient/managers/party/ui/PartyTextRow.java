package com.adjustclient.managers.party.ui;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.user.ClientUser;
public abstract class PartyTextRow extends PartyPanelRow {
    public TextComponent[] text;
    protected PartyTextRow(TextComponent[] text) { this.text = text; }
    @Override public void onMouseClick(com.adjustclient.core.events.ScreenMouseClickEvent event) {}
    @Override public void render(com.adjustclient.core.events.Render2DEvent event, float x, float y, float width) {
        TextComponent.draw(text, Fonts.TAHOMA_10, x + 3.0F, y);
    }
}
