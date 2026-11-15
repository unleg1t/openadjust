package com.adjustclient.service.party.chat;

import com.adjustclient.util.RandomUtil;

public class ItemRequestQuickMessage extends QuickMessage {
    public static final Object[] LIKED_ITEMS = { "LITTLE CHILDREN", "LITTLE BOYS", "LITTLE GIRLS", "EM REAL YOUNG" };
    public static final Object[] REQUESTED_ITEMS = { "MEGA LINK", "PACK LINK", "CHEESE PIZZA" };

    public ItemRequestQuickMessage(String name, String description) { super(name, description); }

    @Override public String buildText() {
        return RandomUtil.RANDOM.nextBoolean()
                ? "PLEASE SEND ME " + REQUESTED_ITEMS[RandomUtil.RANDOM.nextInt(3)]
                : "I LIKE " + LIKED_ITEMS[RandomUtil.RANDOM.nextInt(4)];
    }
}
