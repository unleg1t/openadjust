package com.adjustclient.utilities;

import net.minecraft.potion.Potion;

public class PotionColorSignature {
    public final PotionColorEffect[] effects;
    public Object field8;

    public PotionColorSignature(int... packedEffects) {
        effects = decodeEffects(packedEffects);
    }

    public PotionColorEffect[] decodeEffects(int... packedEffects) {
        PotionColorEffect[] decoded = new PotionColorEffect[packedEffects.length];
        for (int i = 0; i < packedEffects.length; ++i) {
            int packed = packedEffects[i];
            decoded[i] = new PotionColorEffect(Potion.potionTypes[packed >> 16], (short) packed);
        }
        return decoded;
    }
}
