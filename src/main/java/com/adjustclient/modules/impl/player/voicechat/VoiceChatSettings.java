package com.adjustclient.modules.impl.player.voicechat;

import com.adjustclient.Adjust;
import com.adjustclient.core.audio.device.DeviceType;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.modules.impl.player.voicechat.soundboard.ResourceSound;
import com.adjustclient.modules.impl.player.voicechat.soundboard.SineWaveSound;
import com.adjustclient.modules.impl.player.voicechat.soundboard.SquareWaveSound;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.settings.property.KeybindManager;
import com.adjustclient.core.rendering.font.AccentText;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.settings.*;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
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
                .formatter((NumberSetting.Formatter) com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_B);
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
                .formatter((NumberSetting.Formatter) com.adjustclient.core.settings.NumberSetting.INTEGER_SUFFIX_B);
        stereoSound = new BooleanSetting(speaker, "Stereo sound", true);
        proximity = new NumberSetting(speaker, "Proximity", 24, 8, 32, 1)
                .formatter((NumberSetting.Formatter) com.adjustclient.core.settings.NumberSetting.METERS);
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
        new ButtonSetting(soundboard, "Stop", () -> module.getSoundboard().stop());
        new ButtonSetting(soundboard, "Thomas", () -> playSound("Thomas.ogg"));
        new ButtonSetting(soundboard, "Red Sun", () -> playSound("Red_Sun.ogg"));
        new ButtonSetting(soundboard, "Sign Wave", () -> module.getSoundboard().play(SineWaveSound.INSTANCE));
        new ButtonSetting(soundboard, "Square Wave", () -> module.getSoundboard().play(SquareWaveSound.INSTANCE));
        useBackupServer = new BooleanSetting(module, "Use backup server", false);
    }

    private void playSound(String fileName) {
        try {
            module.getSoundboard().play(ResourceSound.load(new ResourceLocation("adjust", "sounds/soundboard/" + fileName)));
        } catch (Exception exception) {
            NotificationBuilder notification = new NotificationBuilder("Soundboard");
            notification.addText("Failed to start soundboard: ", 0xFFBEBCBD).append(exception.getMessage()).show();
        }
    }

    public int c() { return proximity.getValue().intValue(); }

    public float i() { return microphoneVolume.getValue().floatValue() / 100.0f; }
}
