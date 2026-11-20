package com.adjustclient.core.commands.impl;

import com.adjustclient.core.commands.CommandExecutor;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.*;
import com.adjustclient.core.settings.property.TagManager;
import com.adjustclient.core.tag.*;
import com.adjustclient.modules.impl.render.streamer.StreamerModule;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import net.minecraft.util.ChatColor;

public abstract class PlayerListCommand extends CommandExecutor {
    protected final PlayerTag tag;
    protected Object field28;
    protected PlayerListCommand(String name, PlayerTag tag) { super(name); this.tag = tag; }
    protected abstract String getListName();

    @Override public String[] tabComplete(String input, String[] args) { return PlayerNameCompleter.complete(input, true); }

    public void updatePlayer(boolean temporary, boolean remove, String player) {
        TagManager manager = Adjust.INSTANCE.tagManager;
        boolean present = manager.getTag(player) == tag;
        ChatMessage message = createMessage();
        if (remove != present) {
            message.append(present ? "You already have " : "You don't have ");
            message.color = ChatColor.WHITE;
            message.append(StreamerModule.lookupMaskedName(player));
            message.color = ChatColor.GRAY;
            message.append(" in your " + getListName() + ".");
            sendPersistentMessage(message);
            return;
        }
        boolean markTemporary = temporary && !remove;
        message.append(StringUtils.capitalize((markTemporary ? "temporarily " : "") + (remove ? "removed " : "added ")));
        message.color = ChatColor.WHITE;
        message.append(StreamerModule.lookupMaskedName(player));
        message.color = ChatColor.GRAY;
        message.append((remove ? " from" : " to") + " your " + getListName() + ".");
        sendPersistentMessage(message);
        if (remove) manager.remove(player); else manager.setTag(player, tag);
        if (markTemporary) {
            PlayerTagData data = manager.getData(player);
            if (data != null) data.field12 = true;
        }
    }

    @Override protected void run(String label, String[] args) {
        if (args.length == 0) { sendTooLittleArguments("<username> [-t/-temporary]", "clear", "list"); return; }
        if (args.length >= 4) { sendTooManyArguments("<username> [-t/-temporary]", "clear", "list"); return; }
        TagManager manager = Adjust.INSTANCE.tagManager;
        String player = args[0];
        boolean temporary = StringUtil.containsAny(args, new String[]{"-t", "-temporary", "-temp"});
        if (args.length >= 2 && (player.equalsIgnoreCase("remove") || player.equalsIgnoreCase("add"))) {
            updatePlayer(temporary, player.equalsIgnoreCase("remove"), args[1]);
        } else if (player.equalsIgnoreCase("clear")) {
            int count = manager.removeTag(tag);
            sendPersistentMessage(createMessage().format("Removed {} player" + (count == 1 ? "" : "s") + " from your " + getListName() + ".", count));
        } else if (player.equalsIgnoreCase("list")) {
            List<PlayerTagData> players = manager.getByTag(tag);
            ChatMessage message = new ChatMessage();
            for (PlayerTagData data : players) {
                message.color = ChatColor.WHITE;
                message.append(data.name);
                message.color = ChatColor.GRAY;
                message.append(" " + TimeUtil.timeAgo(data.time, " ago"));
                message.append("\n");
            }
            message.format("Listed {} player" + (players.size() == 1 ? "" : "s") + " in your " + getListName() + ".", players.size());
            sendMessage(message);
        } else {
            if (player.startsWith("\\")) player = player.substring(1);
            updatePlayer(temporary, manager.getTag(player) == tag, player);
        }
    }

    @Override public int getExecutionDelay() { return name.length() + 28; }
}
