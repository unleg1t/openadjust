package com.adjustclient.util;

import java.util.function.Supplier;
import net.minecraft.util.*;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.module.impl.render.hud.HUDModule;

public final class ChatUtil implements MinecraftInstance {
    public static final Supplier<Integer> DEFAULT_COLOR = () -> {
        return HUDModule.getHudColor().getRGB();
    };
    public static final Supplier<Integer> ALTERNATE_COLOR = () -> {
        return HUDModule.getHudColor().getRGB();
    };
    public static void addChatMessage(IChatComponent component) {
        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(component);
        else Logger.log(LogLevel.INFO, true, "{}", component.getUnformattedText());
    }
    public static IChatComponent setColorSupplier(IChatComponent component, Supplier<Integer> color) {
        if (component.getChatStyle() == null) component.setChatStyle(new ChatStyle());
        component.getChatStyle().colorSupplier = color;
        return component;
    }
    public static void addChatMessage(IChatComponent component, int chatLineId, boolean persistent, Supplier<Integer> color) {
        if (mc.thePlayer == null) Logger.log(LogLevel.INFO, true, "{}", component.getUnformattedText());
        if (mc.ingameGUI == null || mc.ingameGUI.getChatGUI() == null) return;
        setColorSupplier(component, color);
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(component, chatLineId, persistent);
    }
    public static void sendClientMessage(Object message) {
        Logger.log(LogLevel.DEBUG, true, "{}", message);
        if (mc.thePlayer == null) return;
        IChatComponent prefix = new ChatComponentText("> ").setChatStyle(new ChatStyle().setBold(true));
        ChatMessage builder = new ChatMessage(new ChatComponentText("").appendSibling(prefix));
        builder.append(message);
        addChatMessage(builder.getComponent(), 0, false, DEFAULT_COLOR);
    }
}
