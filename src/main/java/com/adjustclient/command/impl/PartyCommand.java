package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.Adjust;
import com.adjustclient.util.*;
import com.adjustclient.property.UserManager;
import com.adjustclient.service.party.PartyService;
import com.adjustclient.service.party.PartyRequest;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.server.ServerInfo;
import com.adjustclient.user.ClientUser;
import net.minecraft.util.ChatColor;

public final class PartyCommand extends CommandExecutor {
    public static int nextInviteRequestId;
    public Object field24;

    public PartyCommand(String name) {
        super(name);
        field8 = "Invite an Adjust user to a party.";
        aliases = new String[] {"party"};
    }

    @Override public String[] tabComplete(String input, String[] args) {
        return input.contains(" ") ? null : CommandCompleter.complete(input, CommandCompleter.USERS);
    }

    public void handleInviteResult(ClientUser user, String[] args, ServerInfo status) {
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        if (status == ServerInfo.INVITED) {
            message.append("Invited ");
            message.append(user.name);
            message.color = ChatColor.GRAY;
            message.append(" to your party! They have ");
            message.append("30 seconds");
            message.color = ChatColor.GRAY;
            message.append(" to accept.");
        } else if (status == ServerInfo.UNKNOWN_USER) {
            sendUnknownUser(args[0]);
            return;
        } else if (status == ServerInfo.ALREADY_PENDING) {
            message.append("An invite is already pending! Wait for ");
            message.append(user.name);
            message.color = ChatColor.GRAY;
            message.append(" to accept.");
        } else {
            String reason;
            if (status == ServerInfo.SOCIAL_SETTINGS) reason = " because of their social settings!";
            else if (status == ServerInfo.INVITE_SETTINGS) reason = " because of their invite settings!";
            else if (status == ServerInfo.BLOCKED) reason = " because they blocked you!";
            else if (status == ServerInfo.OFFLINE) reason = " because they aren't online!";
            else if (status == ServerInfo.ALREADY_IN_PARTY) reason = " because they are already in a party!";
            else return;
            message.append("Cannot invite ");
            message.append(user.name);
            message.color = ChatColor.GRAY;
            message.append(reason);
        }
        ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    public void inviteUser(String[] args, PartyService service, ClientUser user) {
        if (user == null) { sendUnknownUser(args[0]); return; }
        if (user.useClientColor) {
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append("Cannot invite yourself!");
            ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
            return;
        }
        ChatMessage message = createMessage();
        message.color = ChatColor.GRAY;
        message.append("Party invitations are unavailable in offline mode.");
        ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    @SuppressWarnings("unused")
    private void vendorInviteUser(String[] args, PartyService service, ClientUser user) {
        if (user == null) { sendUnknownUser(args[0]); return; }
        if (user.useClientColor) {
            ChatMessage message = createMessage();
            message.color = ChatColor.GRAY;
            message.append("Cannot invite yourself!");
            ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
            return;
        }
        service.syncPreferences();
        int id = nextInviteRequestId++;
        PartyRequest invite = new PartyRequest(id, status -> handleInviteResult(user, args, status));
        service.requests.add(invite);
        byte[] packet = com.adjustclient.backend.transport.HydraPacketEncoder.encodePartyInvite(user.name, id);
        if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated) Adjust.hydraConnection.send(packet);
    }

    @Override protected void run(String label, String[] args) {
        // Framework output: "Too little arguments." / "Too many arguments."
        if (args.length == 0) { sendTooLittleArguments(); return; }
        if (args.length >= 2) { sendTooManyArguments(); return; }
        PartyService service = Adjust.INSTANCE.screenStack.partyService;
        UserManager users = (UserManager) Adjust.INSTANCE.field40;
        String name = args[0];
        if (!ServiceUtil.isValidName(name)) { inviteUser(args, service, null); return; }
        ClientUser user = users.get(name);
        inviteUser(args, service, user);
    }

    @SuppressWarnings("unused")
    private void vendorLookupAndInvite(String[] args, PartyService service, UserManager users, String name) {
        users.pending.put(users.nextRequestId, found -> vendorInviteUser(args, service, found));
        ServiceUtil.sendToBackend(com.adjustclient.backend.transport.HydraPacketEncoder.encodeUserLookup(users.nextRequestId, name));
        ++users.nextRequestId;
    }
}
