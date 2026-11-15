package com.adjustclient.service.party.ui;
public class PartyPanelButton extends com.adjustclient.ui.screen.ClickableRegion<PartyPanelRow> {
    public PartyPanelButton(PartyPanelRow row, float x, float y) {
        super(row);
        this.x = x;
        this.y = y;
    }
}
