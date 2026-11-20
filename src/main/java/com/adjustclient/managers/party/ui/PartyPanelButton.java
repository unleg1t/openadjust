package com.adjustclient.managers.party.ui;
public class PartyPanelButton extends com.adjustclient.core.gui.screen.ClickableRegion<PartyPanelRow> {
    public PartyPanelButton(PartyPanelRow row, float x, float y) {
        super(row);
        this.x = x;
        this.y = y;
    }
}
