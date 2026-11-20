package com.adjustclient.utilities.hypixel.api.model;

import static net.minecraft.util.ChatColor.*;

public record ApiResult<T extends ApiResponse>(T response, boolean success) {
    @Override public String toString() { return "b[response=" + response + ", success=" + success + "]"; }
}
