package com.adjustclient.module.impl.player.voicechat.soundboard;

import com.adjustclient.ui.hud.notification.NotificationBuilder;
import java.util.ArrayList;
import java.util.List;

public class Soundboard {
    public SoundboardSound currentSound;
    public final List<SoundboardSound> sounds = new ArrayList<>();
    public short[] nextFrame() {
        if (currentSound == null) return null;
        short[] frame = currentSound.nextFrame();
        if (!currentSound.hasRemaining()) currentSound = null;
        return frame;
    }
    public void play(SoundboardSound sound) {
        currentSound = sound;
        String name = sound.getName();
        NotificationBuilder builder = new NotificationBuilder();
        builder.title = "Soundboard";
        builder.parts = new ArrayList<>();
        builder.actions = new ArrayList<>();
        builder.lifetimeSteps = Math.max(22, (int) (name.length() * 1.3200001F) + 24);
        builder.addText("Now playing: ", 0xFFBEBCBD).append(name).show();
    }
}
