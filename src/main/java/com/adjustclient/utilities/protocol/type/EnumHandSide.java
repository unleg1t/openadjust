package com.adjustclient.utilities.protocol.type;

public enum EnumHandSide {
    LEFT(new net.minecraft.util.ChatComponentTranslation("options.mainHand.left")),
    RIGHT(new net.minecraft.util.ChatComponentTranslation("options.mainHand.right"));

    private final net.minecraft.util.IChatComponent name;

    EnumHandSide(net.minecraft.util.IChatComponent name) { this.name = name; }

    @Override public String toString() { return name.getUnformattedText(); }

}
