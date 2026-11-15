package com.adjustclient.util.anticheat.player.suspicion;

import java.util.List;
import java.util.regex.Pattern;

final class DefaultSkinCheck extends NameCheck {
    public Object field4;
    @Override public double score(String name, String skinUrl, double score) {
        if (name.matches("^[A-Za-z]{3}\\d{3}$") && skinUrl.equals(
                "http://textures.minecraft.net/texture/741c5e73265950f2125fc35f4a4b1e733f7838754ca025e9820721e2c54934ff")) {
            return score * 1.1 + 0.9;
        }
        return score;
    }
}
