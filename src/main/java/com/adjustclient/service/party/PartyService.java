package com.adjustclient.service.party;

import com.adjustclient.Adjust;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.service.BackendMessageHandler;
import com.adjustclient.service.party.ui.*;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.ui.hud.notification.*;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.SoundUtil;
import com.adjustclient.util.Timer;
import java.util.ArrayList;
import java.util.List;

public final class PartyService implements BackendMessageHandler {
    public final List<PartyRequest> requests = new ArrayList<>();
    public final List<PartyChatMessage> invitations = new ArrayList<>();
    public PartyPanel party;
    public final List<Timer> recentMessages = new ArrayList<>();
    public boolean typing;
    public boolean syncingPreferences;

    public void onDisconnect() {
        requests.clear();
        setParty(null);
    }

    public void setParty(PartyPanel next) {
        PartyPanel previous = party;
        party = next;
        recentMessages.clear();
        typing = false;
        if (previous != next) {
            StreamerModule.refreshPlayerNames();
            StreamerModule.refreshWorldNames();
            if (previous != null) {
                previous.contextMenu = null;
                previous.chat.closeChat(false);
            }
        }
    }

    public void sendPartyMessage(PartyChatMessage message) {
        synchronized (invitations) { invitations.add(message); }
        SoundUtil.play("party.message", 0.9F, 0.6F);
    }

    public void sendRaw(String text) {
        typing = false;
    }

    @SuppressWarnings("unused")
    private void vendorSendRaw(String text) {
        if (text.length() > 500) text = text.substring(0, 500);
        if (text.isEmpty()) text = " ";
        byte[] packet = new byte[text.length() * 2 + 6];
        com.adjustclient.backend.transport.ByteCodec.writeIntLE(packet, 0, 30);
        packet[4] = (byte) (text.length() >>> 8);
        packet[5] = (byte) text.length();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            packet[6 + i * 2] = (byte) (chars[i] >>> 8);
            packet[7 + i * 2] = (byte) chars[i];
        }
        ServiceUtil.sendToBackend(packet);
        typing = false;
    }

    public void completeRequest(int requestId, com.adjustclient.server.ServerInfo status) {
        for (PartyRequest request : requests) {
            if (request.requestId == requestId) {
                requests.remove(request);
                request.callback.accept(status);
                return;
            }
        }
    }

    public void addRow(PartyPanel panel, PartyPanelRow row) {
        boolean append = true;
        if (row instanceof PartyMemberRow) {
            PartyMemberRow memberRow = (PartyMemberRow) row;
            ContextMenuKey member = memberRow.member;
            if (member.isTyping && party != null) {
                setMemberTyping(member.user, false);
            }
            if (shouldPlaySounds()) memberRow.playMessageSound();
        } else {
            if (row instanceof PartyTextRowI) {
                synchronized (panel.rows) {
                    if (!panel.rows.isEmpty()) {
                        PartyPanelRow last = panel.rows.get(panel.rows.size() - 1);
                        if (last instanceof PartyTextRowF && ((PartyTextRowF) last).user == ((PartyTextRowI) row).user) {
                            panel.replaceRow(last, row);
                            append = false;
                        }
                    }
                }
            }
            if (shouldPlaySounds()) SoundUtil.play("party.message", 0.5F, 0.6F);
        }
        if (append) {
            synchronized (panel.rows) {
                if (panel.title == null) panel.updateTitle();
                panel.rows.add(row);
            }
        }
    }

    public void updateMembers(PartyPanel panel, List<String> names) {
        synchronized (panel.members) {
            List<ContextMenuKey> removed = new ArrayList<>(panel.members);
            removed.removeIf(key -> names.contains(key.user.name));
            for (ContextMenuKey key : removed) {
                panel.members.remove(key);
                panel.title = null;
            }
            for (String name : names) {
                if (panel.containsMemberName(name)) continue;
                ClientUser user = Adjust.INSTANCE.field40.getOrCreate(name);
                panel.addMember(user);
            }
        }
    }

    public void expireInvite(String name, boolean expired) {
        if (expired) {
            NotificationBuilder builder = new NotificationBuilder();
            builder.title = "Party Invite";
            builder.parts = new ArrayList<>();
            builder.actions = new ArrayList<>();
            builder.lifetimeSteps = 56;
            builder.addText("Your party invitation to ", 0xFFBEBCBD).append(name)
                    .addText(" has expired.", 0xFFBEBCBD);
            HUDModule.INSTANCE_ALT.notifications.add(builder.build());
        } else {
            NotificationBuilder builder = new NotificationBuilder();
            builder.title = "Party Invite";
            builder.parts = new ArrayList<>();
            builder.actions = new ArrayList<>();
            builder.lifetimeSteps = 56;
            builder.append(name).addText(" declined your party invitation.", 0xFFBEBCBD);
            HUDModule.INSTANCE_ALT.notifications.add(builder.build());
        }
    }

    public void createParty(int id, String leaderName, String[] names) {
        ClientUser leader = null;
        List<ClientUser> members = new ArrayList<>();
        for (String name : names) {
            ClientUser user = Adjust.INSTANCE.field40.getOrCreate(name);
            if (name.equals(leaderName)) leader = user;
            else members.add(user);
        }
        PartyPanel panel = new PartyPanel(id, leader);
        for (ClientUser member : members) panel.addMember(member);
        Adjust.INSTANCE.screenStack.partyService.setParty(panel);
    }

    public void onConnect() {
        setParty(null);
    }

    public void leaveParty(PartyPanel panel) {
        if (party != null && party.id == panel.id)
            setParty(null);
    }

    @SuppressWarnings("unused")
    private void vendorLeaveParty(PartyPanel panel) {
        if (party != null && party.id == panel.id)
            ServiceUtil.sendToBackend(new byte[]{31, 0, 0, 0});
    }

    public void setMemberTyping(ClientUser user, boolean value) {
        if (party == null) return;
        for (ContextMenuKey key : party.members) {
            if (key.user == user) {
                if (!user.useClientColor) {
                    key.isTyping = value;
                    party.updateTypingState();
                }
                return;
            }
        }
    }

    public boolean isMessageRateLimited() {
        for (int i = 0; i < recentMessages.size();) {
            if (recentMessages.get(i).hasElapsed(500L)) recentMessages.remove(i);
            else i++;
        }
        if (recentMessages.size() >= 11) return true;
        recentMessages.add(new Timer());
        return false;
    }

    public void startTyping() {
        if (party == null || typing) return;
        typing = true;
    }

    public void stopTyping() {
        if (party == null || !typing) return;
        typing = false;
    }

    @SuppressWarnings("unused")
    private void vendorStartTyping() {
        if (party == null || typing) return;
        typing = true;
        ServiceUtil.sendToBackend(new byte[]{33, 0, 0, 0, 1});
    }

    @SuppressWarnings("unused")
    private void vendorStopTyping() {
        if (party == null || !typing) return;
        typing = false;
        ServiceUtil.sendToBackend(new byte[]{33, 0, 0, 0, 0});
    }

    public boolean shouldPlaySounds() {
        return HUDModule.INSTANCE_ALT.partySounds.isEnabled()
                && (HUDModule.INSTANCE_ALT.partySounds.hideIf == null
                    || !HUDModule.INSTANCE_ALT.partySounds.hideIf.getAsBoolean())
                && party != null && party.messageTimer != null;
    }

    public void syncPreferences() {
        syncingPreferences = true;
        Adjust.INSTANCE.screenStack.preferences.sendChanges();
        syncingPreferences = false;
    }
}
