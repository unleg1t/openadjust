package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.util.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ChatColor;

public class GiveCommand extends CommandExecutor {
    public Object field24;
    public GiveCommand(String name) {
        super(name);
        field8 = "Gives you any item (requires creative mode).";
        aliases = new String[] {"give"};
    }

    @Override public String[] tabComplete(String input, String[] ignored) {
        String[] args = input.split(" ", -1);
        if (args.length != 1) return null;
        if (Item.itemRegistry.getKeys().contains(args[0])) return new String[0];
        List<String> matches = new ArrayList<>();
        for (ResourceLocation key : Item.itemRegistry.getKeys()) {
            if (CommandBase.doesStringStartWith(args[0], key.toString())) matches.add(key.toString());
        }
        if (matches.isEmpty()) {
            for (ResourceLocation key : Item.itemRegistry.getKeys()) {
                if (CommandBase.doesStringStartWith(args[0], key.getResourcePath())) matches.add(key.toString());
            }
        }
        return matches.toArray(String[]::new);
    }

    @Override protected void run(String label, String[] args) {
        if (!mc.thePlayer.capabilities.isCreativeMode) {
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append("You must be in creative mode to use this command.");
            sendPersistentMessage(message);
            return;
        }
        if (args.length == 0) { sendTooLittleArguments(); return; }
        try {
            ResourceLocation id = new ResourceLocation(args[0]);
            Item item = Item.itemRegistry.getObject(id);
            if (item == null) throw new NumberInvalidException("commands.give.item.notFound", id);
            int count = args.length >= 2 ? CommandBase.parseInt(args[1], 1, 64) : 1;
            int damage = args.length >= 3 ? CommandBase.parseInt(args[2]) : 0;
            damage = Math.max(0, damage);
            ItemStack stack = new ItemStack(item, count, damage);
            if (args.length >= 4) {
                try {
                    stack.setTagCompound(JsonToNBT.getTagFromJson(
                            CommandBase.getChatComponentFromNthArg(mc.thePlayer, args, 3, false).getUnformattedText()));
                } catch (NBTException e) {
                    throw new CommandException("commands.give.tagError", e.getMessage());
                }
            }
            for (int slot = 0; slot < 36; slot++) {
                if (mc.thePlayer.inventory.getStackInSlot(slot) != null) continue;
                mc.thePlayer.inventory.setInventorySlotContents(slot, stack.copy());
                mc.thePlayer.sendQueue.addToSendQueue(new C10PacketCreativeInventoryAction(slot < 9 ? slot + 36 : slot, stack));
                sendPersistentMessage(createMessage().format("Added {} to inventory.", stack.getDisplayName()));
                return;
            }
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append("No inventory slot is available.");
            sendPersistentMessage(message);
        } catch (CommandException e) {
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append(I18n.format(e.getMessage(), e.getErrorObjects()));
            sendPersistentMessage(message);
        }
    }
}
