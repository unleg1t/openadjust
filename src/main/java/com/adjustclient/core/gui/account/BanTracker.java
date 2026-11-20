package com.adjustclient.core.gui.account;

import java.util.HashMap;
import java.util.HashSet;

import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;

public class BanTracker {

    final HashMap<Object, Long> bans = new HashMap<>();
    final HashSet<Object> dirty = new HashSet<>();
    Object obf12;

    /** Returns the stored expiry of the given server type (null if unknown). */
    public Long getExpiry(Object serverType) {
        return this.bans.get(serverType);
    }

    /**
     * Stores a new expiry. Nothing happens when the stored value is already equal. Expiry values below -1 are logged
     * ("Ban expiry for {} went under! {}") and replaced by {@code Long.valueOf(-1)}. When {@code markDirty} is set the server type is
     * added to {@link #dirty} so that it is synced to the backend.
     */
    public void setExpiry(Object serverType, Long expiry, boolean markDirty) {
        Long old = this.bans.get(serverType);
        if (old != null) {
            if (old == expiry || old.equals(expiry)) {
                return;
            }
        }
        if (expiry >= -1L) {
            this.bans.put(serverType, expiry);
        } else {
            Logger.log(LogLevel.WARN, true, "Ban expiry for {} went under! {}", new Object[] { serverType, expiry });
            this.bans.put(serverType, Long.valueOf(-1L));
        }
        if (markDirty) {
            this.dirty.add(serverType);
        }
    }

    /** True when the stored expiry means "unbanned": expiry 0, or a finite ban that already ended; null and -1 are not. */
    public static boolean isUnbanned(Long expiry) {
        if (expiry == null) {
            return false;
        }
        long value = expiry;
        if (value == -1L) {
            return false;
        }
        if (value != 0L) {
            return System.currentTimeMillis() > value;
        }
        return true;
    }
}
