package com.adjustclient.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class WoolGamesStats {
    @SerializedName("progression")
    public com.adjustclient.hypixel.api.model.player.stats.WoolGamesProgression progression;
    @SerializedName("wool_wars")
    public com.adjustclient.hypixel.api.model.player.stats.WoolWarsStats woolWars;
    @SerializedName("sheep_wars")
    public com.adjustclient.hypixel.api.model.player.stats.SheepWarsStats sheepWars;
    @SerializedName("capture_the_wool")
    public com.adjustclient.hypixel.api.model.player.stats.CaptureTheWoolStats captureTheWool;

    public WoolGamesStats() {
        this.progression = new WoolGamesProgression();
    }

    public static int getLevel(double experience) {
        int prestige = (int) (experience / 490000.0);
                int level = prestige * 100;
                double rest = experience - (double) (prestige * 490000);
                if (rest < 0.0) {
                } else if (rest < 1000.0) {
                    level += 1;
                } else {
                    rest -= 1000.0;
                    if (rest < 2000.0) level += 2;
                    else {
                        rest -= 2000.0;
                        if (rest < 3000.0) level += 3;
                        else {
                            rest -= 3000.0;
                            if (rest < 4000.0) level += 4;
                            else { rest -= 4000.0; level += 5; }
                        }
                    }
                }
                int next = level + 1;
                double cost = switch (next % 100) {
                    case 0 -> next < 100 ? 0 : 5000;
                    case 1 -> 1000;
                    case 2 -> 2000;
                    case 3 -> 3000;
                    case 4 -> 4000;
                    default -> 5000;
                };
                return (int) (level + rest / cost);
    }
}
