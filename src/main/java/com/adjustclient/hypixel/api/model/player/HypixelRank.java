package com.adjustclient.hypixel.api.model.player;

import com.adjustclient.Adjust;
import net.minecraft.util.ChatColor;
import static net.minecraft.util.ChatColor.*;
import java.util.function.BooleanSupplier;

public enum HypixelRank implements com.adjustclient.util.Displayable {
    STAFF("STAFF", RED,
        () -> false),
    YOUTUBER("YOUTUBE", RED,
        () -> false),
    SUPERSTAR("MVP++", GOLD,
        () -> true),
    MVP_PLUS("MVP+", AQUA,
        () -> false),
    MVP("MVP", AQUA,
        () -> false),
    VIP_PLUS("VIP+", GREEN,
        () -> true),
    VIP("VIP", GREEN,
        () -> false),
    NONE("None", GRAY,
        () -> Adjust.gameStateTracker.state.lobby != null && !Adjust.gameStateTracker.state.lobby);

    public final String displayName;
    public final ChatColor color;
    public final BooleanSupplier displayPredicate;

    HypixelRank(String displayName, ChatColor color, BooleanSupplier displayPredicate) {
        this.displayName = displayName;
        this.color = color;
        this.displayPredicate = displayPredicate;
    }
}
