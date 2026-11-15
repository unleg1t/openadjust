package com.adjustclient.module.impl.player.voicechat;

import com.adjustclient.Adjust;
import com.adjustclient.audio.device.DeviceType;
import com.adjustclient.bind.Bind;
import com.adjustclient.module.impl.player.voicechat.soundboard.ResourceSound;
import com.adjustclient.module.impl.player.voicechat.soundboard.Soundboard;
import com.adjustclient.module.impl.player.voicechat.soundboard.SineWaveSound;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.property.KeybindManager;
import com.adjustclient.render.font.AccentText;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.setting.*;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import net.minecraft.util.ResourceLocation;

public final class VoiceChatSettings {
    public final VoiceChatModule module;
    public final NumberSetting speakerVolume;
    public final BooleanSetting stereoSound;
    public final NumberSetting proximity;
    public final BooleanSetting volumeCompressor;
    public final BooleanSetting noiseSuppression;
    public BooleanSetting realisticEnabled;
    public final BooleanSetting echo;
    public final BooleanSetting muffle;
    public final NumberSetting microphoneVolume;
    public final BooleanSetting micTester;
    public final ModeSetting<InputActivationMode> inputActivation;
    public final Bind pushToTalk;
    public BooleanSetting hudEnabled;
    public final BooleanSetting displayAllPlayers;
    public final BooleanSetting useBackupServer;

    public VoiceChatSettings(VoiceChatModule module) {
        this.module = module;
        ExpandSetting microphone = new ExpandSetting(module, "Microphone");
        new ButtonSetting(microphone, "Set Microphone", () -> module.openDeviceSelection(DeviceType.INPUT));
        microphoneVolume = new NumberSetting(microphone, "Volume", 100, 0, 200, 5)
                .formatter((NumberSetting.Formatter) com.adjustclient.setting.NumberSetting.INTEGER_SUFFIX_B);
        micTester = new BooleanSetting(microphone, "Mic tester", false);
        inputActivation = new ModeSetting<>(microphone, "Input activation", InputActivationMode.PUSH, InputActivationMode.values());
        KeybindManager binds = Adjust.INSTANCE.bindManager;
        pushToTalk = binds.register("module.voicechat.push2talk", mode -> { }, null);
        new KeybindSetting(microphone, "Push to talk", pushToTalk, () -> {
            return inputActivation.getValue() != InputActivationMode.PUSH;
        });
        ExpandSetting speaker = new ExpandSetting(module, "Speaker");
        new ButtonSetting(speaker, "Set Speaker", () -> module.openDeviceSelection(DeviceType.OUTPUT));
        speakerVolume = new NumberSetting(speaker, "Volume", 100, 0, 200, 5)
                .formatter((NumberSetting.Formatter) com.adjustclient.setting.NumberSetting.INTEGER_SUFFIX_B);
        stereoSound = new BooleanSetting(speaker, "Stereo sound", true);
        proximity = new NumberSetting(speaker, "Proximity", 24, 8, 32, 1)
                .formatter((NumberSetting.Formatter) com.adjustclient.setting.NumberSetting.METERS);
        volumeCompressor = new BooleanSetting(speaker, "Volume compressor", true);
        noiseSuppression = new BooleanSetting(speaker, "Noise suppression", true);
        ExpandSetting realistic = new ExpandSetting(speaker, "Realistic Sound", () -> {
            return false;
        });
        realisticEnabled = new BooleanSetting(realistic, "Enabled", true, "Yes", "No");
        realistic.enabledPredicate = () -> {
            return realisticEnabled.isEnabled() && !realisticEnabled.isHidden();
        };
        echo = new BooleanSetting(realistic, "Echo", true);
        muffle = new BooleanSetting(realistic, "Muffle", true);
        ExpandSetting hud = new ExpandSetting(module, "HUD", () -> {
            return false;
        });
        hudEnabled = new BooleanSetting(hud, "Enabled", true, "Yes", "No");
        hud.enabledPredicate = () -> {
            return hudEnabled.isEnabled() && !hudEnabled.isHidden();
        };
        displayAllPlayers = new BooleanSetting(hud, "Display All Players", false);
        ExpandSetting soundboard = new ExpandSetting(module, "Soundboard");
        new ButtonSetting(soundboard, "Stop", () -> {
            if (module.soundboard == null) module.soundboard = new Soundboard();
            if (module.soundboard.currentSound != null) {
                module.soundboard.currentSound = null;
                NotificationBuilder notification = new NotificationBuilder();
                notification.title = "Soundboard";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.addText("Soundboard has been stopped.", 0xFFBEBCBD).show();
            }
        });
        new ButtonSetting(soundboard, "Thomas", () -> {
            try {
                if (module.soundboard == null) module.soundboard = new Soundboard();
                module.soundboard.play(ResourceSound.load(new ResourceLocation("adjust", "sounds/soundboard/Thomas.ogg")));
            } catch (Exception exception) {
                NotificationBuilder notification = new NotificationBuilder();
                notification.title = "Soundboard";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.addText("Failed to start soundboard: ", 0xFFBEBCBD).append(exception.getMessage()).show();
            }
        });
        new ButtonSetting(soundboard, "Sign Wave", () -> {
            if (module.soundboard == null) module.soundboard = new Soundboard();
            module.soundboard.currentSound = SineWaveSound.INSTANCE;
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = "Soundboard";
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Now playing: ", 0xFFBEBCBD).append("Sign Wave").show();
        });
        useBackupServer = new BooleanSetting(module, "Use backup server", false);
    }

    public int c() { return proximity.getValue().intValue(); }

    public float i() { return microphoneVolume.getValue().floatValue() / 100.0f; }
}
