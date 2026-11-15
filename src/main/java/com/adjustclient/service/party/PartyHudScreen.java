package com.adjustclient.service.party;

import com.adjustclient.ui.screen.AdjustScreen;

public class PartyHudScreen extends AdjustScreen {
    public static final PartyHudScreen INSTANCE = new PartyHudScreen();

    public boolean keyTyped(char character, int keyCode) {
        if (keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) super.close();
        return false;
    }

    public void draw(float mouseX, float mouseY) { }

    public void mouseClicked(float mouseX, float mouseY, int button) { }

    public void onClose() { }
}
