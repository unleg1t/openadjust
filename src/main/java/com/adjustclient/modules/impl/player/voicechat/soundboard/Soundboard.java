package com.adjustclient.modules.impl.player.voicechat.soundboard;

import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
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
        NotificationBuilder builder = new NotificationBuilder("Soundboard");
        builder.lifetimeSteps = Math.max(22, (int) (name.length() * 1.3200001F) + 24);
        builder.addText("Now playing: ", 0xFFBEBCBD).append(name);
        HUDModule hud = HUDModule.INSTANCE_ALT != null ? HUDModule.INSTANCE_ALT : HUDModule.INSTANCE;
        if (hud != null && hud.isEnabled() && hud.notifications != null) {
            hud.notifications.add(builder.build());
        }
    }

    public void stop() {
        if (currentSound == null) return;
        currentSound = null;
        NotificationBuilder builder = new NotificationBuilder("Soundboard");
        builder.addText("Soundboard has been stopped.", 0xFFBEBCBD);
        builder.lifetimeSteps = 43;
        HUDModule hud = HUDModule.INSTANCE_ALT != null ? HUDModule.INSTANCE_ALT : HUDModule.INSTANCE;
        if (hud != null && hud.isEnabled() && hud.notifications != null) {
            hud.notifications.add(builder.build());
        }
    }
}
