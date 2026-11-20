package com.adjustclient.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public final class SoundUtil {
    public static void play(String name, float volume, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(
                new ResourceLocation("adjust", name), volume, pitch, false, 0,
                ISound.AttenuationType.NONE, 0, 0, 0));
    }
}
