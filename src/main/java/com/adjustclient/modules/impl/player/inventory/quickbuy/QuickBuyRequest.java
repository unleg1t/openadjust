package com.adjustclient.modules.impl.player.inventory.quickbuy;

import com.adjustclient.utilities.Timer;

public final class QuickBuyRequest {
    public final ShopCategory category;
    public final Timer timer = new Timer();
    public QuickBuyRequest(ShopCategory category) { this.category = category; }
}
