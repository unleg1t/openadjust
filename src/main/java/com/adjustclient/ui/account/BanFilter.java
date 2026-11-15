package com.adjustclient.ui.account;

import java.util.function.Predicate;
import com.adjustclient.server.ServerType;
import net.minecraft.util.ChatColor;

public enum BanFilter {

    ALL(ChatColor.GRAY + "All", e -> false),

    UNBANNED(ChatColor.GREEN + "Unbanned", BanFilter::isBanned),

    BANNED(ChatColor.RED + "Banned", BanFilter::isUnbanned);

    final String displayName;
    final Predicate<AccountEntry> hidePredicate;
    private Object obf24;

    BanFilter(String displayName, Predicate<AccountEntry> hidePredicate) {
        this.displayName = displayName;
        this.hidePredicate = hidePredicate;
    }

    /** true when the entry has an active ban: expiry -1 = permanent, 0 = none, otherwise the ban end must lie in the future. */
    private static boolean isBanned(AccountEntry entry) {
        Long expiry = entry.bans.bans.get(ServerType.HYPIXEL);
        if (expiry == null) {
            return false;
        }
        long value = expiry;
        if (value == -1L) {
            return true;
        }
        boolean expired = value == 0L || System.currentTimeMillis() > value;
        return !expired;
    }

    /** true when the entry is known to be unbanned: expiry 0, or a finite ban that has already expired (same as BanTracker.isUnbanned). */
    private static boolean isUnbanned(AccountEntry entry) {
        Long expiry = entry.bans.bans.get(ServerType.HYPIXEL);
        if (expiry == null) {
            return false;
        }
        long value = expiry;
        if (value == -1L) {
            return false;
        }
        if (value == 0L) {
            return true;
        }
        return System.currentTimeMillis() > value;
    }

}
