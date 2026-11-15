package com.adjustclient.module.impl.player.inventory.quickbuy;

import com.adjustclient.util.Timer;

public final class QuickBuyRequest {
    public final ShopCategory category;
    public final Timer timer = new Timer();
    public QuickBuyRequest(ShopCategory category) { this.category = category; }
}
