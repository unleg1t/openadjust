package com.adjustclient.service.party.ui;
import com.adjustclient.render.font.*;
import com.adjustclient.user.ClientUser;
public abstract class PartyTextRow extends PartyPanelRow {
    public TextComponent[] text;
    protected PartyTextRow(TextComponent[] text) { this.text = text; }
    @Override public void onMouseClick(com.adjustclient.event.ScreenMouseClickEvent event) {}
    @Override public void render(com.adjustclient.event.Render2DEvent event, float x, float y, float width) {
        TextComponent.draw(text, Fonts.TAHOMA_10, x + 3.0F, y);
    }
}
