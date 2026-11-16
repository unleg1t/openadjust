package com.adjustclient.backend.transport;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.charset.StandardCharsets;
import com.adjustclient.Adjust;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import com.adjustclient.backend.auth.AuthTokenData;
import com.adjustclient.backend.auth.Authenticator;
import com.adjustclient.backend.auth.EnvironmentInfo;
import com.adjustclient.backend.hwid.HardwareId;
import com.adjustclient.command.impl.debug.DebugEpsteinSubCommand;
import com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule;
import com.adjustclient.service.config.ConfigurationService;
import com.adjustclient.service.friends.BlockListService;
import com.adjustclient.service.util.ClientInfoCollector;
import com.adjustclient.service.util.ClientInfoEntry;
import com.adjustclient.ui.registration.BlacklistedScreen;
import com.adjustclient.ui.registration.MigrateHwidScreen;
import com.adjustclient.ui.registration.RecoverAccountScreen;
import com.adjustclient.ui.registration.RegisterScreen;
import com.adjustclient.user.Cape;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.command.impl.debug.DebugCommand;
import com.adjustclient.service.credits.CreditsRenderer;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.RandomUtil;

public final class HydraMessageDispatcher {
    public static int selectedRepositoryId;
    public static int selectedAccountId;
    public static boolean repositoriesReady;
    public static boolean accountActionRequested;
    public static int accountFlagId;
    public static int accountFlagValue;
    public static int blacklistAddressByte;
    public static int sessionFlagValue;
    public static int accountLoginMask;
    public static int accountLoginKey;
    public static int challengeXorResult;
    public static int challengeStageSlot;
    public static int repoFlagXor;
    public static int repoFlagRaw;
    public static int handshakeSeed;
    public static int envHash1;
    public static int envHash2;
    public static int receivedPacketCount;
    public static int accountLoginCounter;
    public static int challengeCounter;
    public static int handshakeCounter;
    public static short partyFlagCount;
    public static boolean partyFlagSet;
    public static boolean accountLoginHandled;
    public static boolean handshakeStarted;
    public static boolean recoveryPending;
    public static Integer remoteBuildId;
    public static AtomicBoolean pingAcked;
    public static boolean offlineChallengeReset;

    public static AuthTokenData mcAuthTokenData;
    public static int mcSessionCounter316;
    public static Object mcField80;

    public static com.adjustclient.event.FriendEventPayload inputKeyPayload;
    public static final com.adjustclient.service.util.ClientInfoSource clientInfoSourceToken =
            new com.adjustclient.service.util.ClientInfoSource() { };
    private int field4;
    private Object field8;

    public static void expirePartyInvite(byte[] packet) {
        Adjust.INSTANCE.screenStack.partyService.expireInvite(ByteCodec.readString(packet, 5), packet[4] != 0);
    }

    public static void updateUsers(byte[] packet) {
        String name = ByteCodec.readString(packet, 4);
        String display = ByteCodec.readString(packet, 6 + 2 * name.length());
        Adjust.INSTANCE.field40.getOrCreate(name).displayName = display;
    }

    public static void handleUserColor(byte[] packet) {
        if (Adjust.INSTANCE.field40 == null) return;
        Adjust.INSTANCE.field40.getOrCreate(ByteCodec.readString(packet, 8)).color = ByteCodec.readInt(packet, 4);
    }

    public static void removeRepository(byte[] packet) {
        var screen = Adjust.INSTANCE.accountManager;
        int id = ByteCodec.readInt(packet, 4);
        for (var repository : screen.repositories) {
            if (repository.id != id) continue;
            if (screen.currentRepository == repository) {
                screen.currentRepository = null;
                screen.selection.clear();
                screen.selectedAccount = null;
            }
            screen.repositories.remove(repository);
            break;
        }
    }

    public static void refreshRepositoryAccounts(byte[] packet) {
        var repository = Adjust.INSTANCE.accountManager.getRepository(ByteCodec.readInt(packet, 4));
        if (repository != null) Minecraft.getMinecraft().addScheduledTask(() -> lambda_80811bd95a948f94e7378fe90f75bd7c0(repository));
    }

    public static void renameRepository(byte[] packet) {
        int id = ByteCodec.readInt(packet, 4);
        String name = ByteCodec.readString(packet, 8);
        for (var repository : Adjust.INSTANCE.accountManager.repositories) {
            if (repository.id == id) { repository.name = name; return; }
        }
    }

    public static void handleRepositoryMembers(byte[] packet) {
        var repository = Adjust.INSTANCE.accountManager.getRepository(ByteCodec.readInt(packet, 4));
        if (repository == null) return;
        int count = ((packet[8] & 255) << 8) | (packet[9] & 255);
        String[] names = new String[count];
        com.adjustclient.server.ServerRegion[] owners = new com.adjustclient.server.ServerRegion[count];
        int offset = 10;
        for (int i = 0; i < count; i++) {
            names[i] = ByteCodec.readString(packet, offset);
            owners[i] = i == 0 ? com.adjustclient.server.ServerRegion.OWNER : com.adjustclient.server.ServerRegion.GUEST;
            offset += 2 + names[i].length() * 2;
        }
        repository.setMembers(names, owners);
    }

    public static void removePartyMember(byte[] packet) {
        var party = Adjust.INSTANCE.screenStack.partyService;
        if (party.party == null || party.party.id != ByteCodec.readInt(packet, 4)) return;
        var user = Adjust.INSTANCE.field40.getOrCreate(ByteCodec.readString(packet, 9));
        party.setMemberTyping(user, packet[8] == 1);
    }

    public static void addPartyMember(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            int id = input.readInt();
            int count = input.readInt();
            List<String> names = new ArrayList<>(count);
            for (int i = 0; i < count; i++) names.add(input.readUTF());
            var service = Adjust.INSTANCE.screenStack.partyService;
            var panel = service.party;
            if (panel != null && panel.id == id) service.updateMembers(panel, names);
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Failed to receive add party member: {}", new Object[]{exception});
        }
    }

    public static void closeParty(byte[] packet) {
        int id = ByteCodec.readInt(packet, 4);
        var service = Adjust.INSTANCE.screenStack.partyService;
        var party = service.party;
        if (party == null || party.id != id) return;
        service.setParty(null);
    }

    public static void updatePartyMember(byte[] packet) {
        var service = Adjust.INSTANCE.screenStack.partyService;
        var panel = service.party;
        if (panel == null || panel.id != ByteCodec.readInt(packet, 4)) return;
        int count = ByteCodec.readInt(packet, 8);
        int offset = 12;
        for (int i = 0; i < count; i++) {
            int type = packet[offset++];
            boolean flag = false;
            if (type == 44) flag = packet[offset++] != 0;
            String name = ByteCodec.readString(packet, offset);
            offset += 2 + name.length() * 2;
            var user = Adjust.INSTANCE.field40.getOrCreate(name);
            com.adjustclient.service.party.ui.PartyPanelRow row;
            switch (type) {
                case 13: {
                    String text = ByteCodec.readString(packet, offset);
                    offset += 2 + text.length() * 2;
                    var member = panel.findMember(user);
                    if (member == null) continue;
                    row = new com.adjustclient.service.party.ui.PartyMemberRow(member, text);
                    break;
                }
                case 33: row = new com.adjustclient.service.party.ui.PartyTextRowI(user); break;
                case 44: row = new com.adjustclient.service.party.ui.PartyTextRowC(user, flag); break;
                case 55: row = new com.adjustclient.service.party.ui.PartyTextRowF(user); break;
                case 65: row = new com.adjustclient.service.party.ui.PartyTextRowH(user); break;
                case 67: row = new com.adjustclient.service.party.ui.PartyTextRowG(user); break;
                case 83:
                case 85: {
                    String otherName = ByteCodec.readString(packet, offset);
                    offset += 2 + otherName.length() * 2;
                    var other = Adjust.INSTANCE.field40.getOrCreate(otherName);
                    row = type == 83 ? new com.adjustclient.service.party.ui.PartyTextRowD(user, other)
                            : new com.adjustclient.service.party.ui.PartyTextRowE(user, other);
                    break;
                }
                default: throw new RuntimeException("Unknown type[0] " + type);
            }
            service.addRow(panel, row);
        }
        panel.messageTimer = new com.adjustclient.util.Timer();
    }

    public static void updatePartyPreferences(byte[] packet) {
        int count = ByteCodec.readInt(packet, 4);
        int selected = ByteCodec.readInt(packet, 8);
        int id = ByteCodec.readInt(packet, 12);
        String[] names = new String[count];
        int offset = 16;
        for (int i = 0; i < count; i++) {
            names[i] = ByteCodec.readString(packet, offset);
            offset += 2 + 2 * names[i].length();
        }
        if (selected < 0 || selected >= count) throw new RuntimeException("OOB[0]: " + count + ", " + selected);
        Adjust.INSTANCE.screenStack.partyService.createParty(id, names[selected], names);
    }

    public static void deleteAccount(byte[] packet) {
        Adjust.INSTANCE.accountManager.accountDeleted(ByteCodec.readInt(packet, 4), ByteCodec.readInt(packet, 8));
    }

    public static void updateAccountBan(byte[] packet) {
        int accountId = ByteCodec.readInt(packet, 4);
        int repositoryId = ByteCodec.readInt(packet, 8);
        long expiry = ByteCodec.readLong(packet, 12);
        var repository = Adjust.INSTANCE.accountManager.getRepository(repositoryId);
        if (repository == null) return;
        var account = repository.getAccount(accountId);
        if (account != null) account.bans.setExpiry(com.adjustclient.server.ServerType.HYPIXEL, expiry, false);
    }

    public static void handlePartyFlag(byte[] packet) {
        partyFlagSet = true;
        AuthTokenData token = mcAuthTokenData;
        if (packet.length < 26 || token == null) {
            return;
        }
        if (ByteCodec.readInt(packet, 22) - 1 != token.expectedSeq) {
            return;
        }
        int key = token.randomKey;
        token.field4 = key ^ ByteCodec.readInt(packet, 4);
        token.field24 = key ^ ByteCodec.readInt(packet, 8);
        partyFlagCount++;
        token.field8 = token.seed ^ ByteCodec.readLong(packet, 12);
        token.active = (packet[20] == 0);
        token.field36 = (byte) (packet[21] ^ key);
    }

    public static void handleAccountLastLogin(byte[] packet) {
        int repositoryId = ByteCodec.readInt(packet, 4);
        int accountId = ByteCodec.readInt(packet, 8);
        String username = ByteCodec.readString(packet, 12);
        long time = ByteCodec.readLong(packet, 14 + 2 * username.length());
        var repository = Adjust.INSTANCE.accountManager.getRepository(repositoryId);
        if (repository == null) return;
        for (var account : repository.accounts) {
            if (account.id == accountId) {
                account.lastLogin = new com.adjustclient.ui.account.LastLogin(username, time, null);
                return;
            }
        }
    }

    public static void addAccounts(byte[] packet) {
        int repositoryId = ByteCodec.readInt(packet, 4);
        int count = ByteCodec.readInt(packet, 8);
        var screen = Adjust.INSTANCE.accountManager;
        var repository = screen.getRepository(repositoryId);
        if (repository == null) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Attempted to add {} accounts to unknown repository '{}'!", new Object[]{count, repositoryId});
            return;
        }
        repository.loading = false;
        var accounts = new com.adjustclient.ui.account.AccountEntry[count];
        int offset = 12;
        try {
            for (int i = 0; i < count; i++) {
                offset = readAccount(packet, offset, accounts, i);
                if (ServiceUtil.repositorySelectionPending && repository.id == selectedRepositoryId
                        && accounts[i].id == selectedAccountId) {
                    try {
                        screen.select(accounts[i]);
                        Minecraft.getMinecraft().session = new net.minecraft.util.Session(accounts[i].username,
                                accounts[i].uuid, accounts[i].token, "mojang");
                        Adjust.INSTANCE.accountManager.scrollToSelection = true;
                    } catch (Exception exception) { exception.printStackTrace(); }
                    ServiceUtil.repositorySelectionPending = false;
                }
            }
        } catch (IOException exception) { exception.printStackTrace(); return; }
        for (var account : accounts) account.repository = repository;
        Minecraft.getMinecraft().addScheduledTask(() -> lambda_137c02122a16f472a46c54cf59e522c30(repository, accounts));
    
    }

    public static void handleSessionFlag(byte[] packet) {
        int mask = ByteCodec.readInt(packet, 4);
        sessionFlagValue = ByteCodec.readInt(packet, 8) ^ mask;
        int counter = mcSessionCounter316;
        if (counter != 0) {
            mcSessionCounter316 = counter - (ByteCodec.readInt(packet, 12) ^ mask);
        }
    }

    public static void handleUserLookupReply(byte[] packet) {
        int id = ByteCodec.readInt(packet, 4);
        boolean found = packet[8] != 0;
        String name = found ? ByteCodec.readString(packet, 9) : null;
        Adjust.INSTANCE.field40.handleLookup(id, found, name);
    }

    public static void handleNoOp(byte[] packet) {
    }

    public static void handleConnectionFlag(byte[] packet) {
        selectedAccountId = ByteCodec.readInt(packet, 8);
        selectedRepositoryId = ByteCodec.readInt(packet, 4);
    }

    public static void handleSaveReply(byte[] packet) {
        int status = ByteCodec.readInt(packet, 4);
        int id = ByteCodec.readInt(packet, 8);
        var configurations = Adjust.INSTANCE.screenStack.configurations.pending;
        for (var configuration : configurations) {
            if (configuration.requestId != id) continue;
            int type = configuration.kind;
            if (type == 0 || type == 1 || type == 3) {
                if (configuration.callback != null) ((com.adjustclient.service.config.ConfigurationListener) configuration.callback).onReply(status);
                configurations.remove(configuration);
                return;
            }
        }
    }

    public void handleUserRename(byte[] packet) {
        String oldName = ByteCodec.readString(packet, 4);
        String newName = ByteCodec.readString(packet, 6 + oldName.length() * 2);
        var manager = Adjust.INSTANCE.field40;
        for (var user : manager.objects) {
            if (manager.matches(user, oldName)) { user.name = newName; return; }
        }
    }

    public static void handleServerTracker(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            boolean scope = input.readUnsignedByte() != 0;
            int count = com.adjustclient.util.ByteUtil.readVarInt(input);
            List<com.adjustclient.service.servertracker.TrackedFriend> players = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                String name = input.readUTF();
                String server = input.readUTF();
                boolean flag = scope && input.readBoolean();
                players.add(new com.adjustclient.service.servertracker.TrackedFriend(name, server, flag));
            }
            Adjust.INSTANCE.screenStack.serverTracker.handleTrackedFriends(players);
        } catch (IOException exception) {
            new com.adjustclient.ui.hud.notification.NotificationBuilder("Server Tracker")
                    .format("An unexpected exception occurred. Please report this with the message in console!", new Object[0]).show();
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.WARN, true, "Failed to receive known in-game players: {}", new Object[]{exception});
        }
    }

    public static void handlePartyStatus(byte[] packet) {
        int partyId = ByteCodec.readInt(packet, 4);
        int status = ByteCodec.readInt(packet, 8);
        com.adjustclient.server.ServerInfo result = null;
        for (var value : com.adjustclient.server.ServerInfo.VALUES) {
            if (value.status == status) { result = value; break; }
        }
        if (result != null) Adjust.INSTANCE.screenStack.partyService.completeRequest(partyId, result);
        else com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Bad status '{}'", new Object[]{status});
    }

    public static void handlePartyInvite(byte[] packet) {
        int id = ByteCodec.readInt(packet, 4);
        String name = ByteCodec.readString(packet, 8);
        var user = Adjust.INSTANCE.field40.getOrCreate(name);
        String format = user.friend != null ? "Your friend {} sent you a party invitation!"
                : "You received a party invitation from {}.";
        com.adjustclient.render.font.TextComponent[] text = com.adjustclient.service.party.PartyChatMessage.format(new com.adjustclient.util.ErrorReport(format, new Object[]{user.name}));
        var message = new com.adjustclient.service.party.PartyChatMessage("Party Invite", text, user, id);
        message.expiresAt = System.currentTimeMillis() + 30000L;
        message.animation = new com.adjustclient.render.animation.Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 280L);
        message.accept = new com.adjustclient.ui.dialog.ActionLabel("Accept", com.adjustclient.render.font.Fonts.TAHOMA_10);
        message.decline = new com.adjustclient.ui.dialog.ActionLabel("Decline", com.adjustclient.render.font.Fonts.TAHOMA_10);
        message.accept.action = new com.adjustclient.render.animation.Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 300L);
        message.decline.action = new com.adjustclient.render.animation.Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 300L);
        message.onDecision = accepted -> lambda_03cbf3b214ed4caef34acba51e8ed8680(user, accepted);
        Minecraft.getMinecraft().addScheduledTask(() -> lambda_963a52597a775b66de4e1e5d41b924020(message));
    
    }

    public void dispatch(byte[] packet) {
        receivedPacketCount++;
        int message = ByteCodec.readIntLE(packet, 0);
        if (Adjust.hydraConnection == null) {
            return;
        }
        switch (message) {
            case 0: handleHandshake(packet); break;
            case 1: handleAuthenticationFailure(); break;
            case 2: handleRegistration(packet); break;
            case 3: handleChallenge(packet); break;
            case 4: handleRepositories(packet); break;
            case 5: handleRecovery(packet); break;
            case 6: handleBlacklist(packet); break;
            case 7: addAccounts(packet); break;
            case 8: throw new RuntimeException();
            case 9: handleUserLookupReply(packet); break;
            case 10: handleAccountLastLogin(packet); break;
            case 11: updateAccountBan(packet); break;
            case 12: handleAccountLogin(packet); break;
            case 13: handleRepositoryFlag(packet); break;
            case 14: handleAccountFlag(packet); break;
            case 15: handleRepositoryMembers(packet); break;
            case 16: handlePing(packet); break;
            case 17: handlePartyInvite(packet); break;
            case 18: handlePartyStatus(packet); break;
            case 19: handleConnectionFlag(packet); break;
            case 20: handleRepositoriesChanged(packet); break;
            case 21: deleteAccount(packet); break;
            case 22: handleAuthenticationResult(packet); break;
            case 23: handleSaveReply(packet); break;
            case 24: handleConfigurations(packet); break;
            case 25: handleSessionFlag(packet); break;
            case 26: handleConfigurationReply(packet); break;
            case 27: handleServerTracker(packet); break;
            case 28: handleNoOp(packet); break;
            case 29: handlePartyFlag(packet); break;
            case 30: updatePartyPreferences(packet); break;
            case 31: updatePartyMember(packet); break;
            case 32: closeParty(packet); break;
            case 33: addPartyMember(packet); break;
            case 34: removePartyMember(packet); break;
            case 35: renameRepository(packet); break;
            case 37: refreshRepositoryAccounts(packet); break;
            case 38: removeRepository(packet); break;
            case 39: handleUserColor(packet); break;
            case 40: updateUsers(packet); break;
            case 41: expirePartyInvite(packet); break;
            case 42: removePartyInvite(packet); break;
            case 43: handleApiResponse(packet); break;
            case 44: handleInvite(packet); break;
            case 45: handleAccountData(packet); break;
            case 46: handleGlobalChatMessage(packet); break;
            case 47: handlePayload(packet); break;
            case 48: handleGlobalChatState(packet); break;
            case 49: handleDirectMessage(packet); break;
            case 50: handleCredits(packet); break;
            case 51: accountActionRequested = true; break;
            case 52: handleScreenReset(packet); break;
            case 53: handleUserRename(packet); break;
            case 54: com.adjustclient.service.util.ServiceUtil.runWhenInitialized(() -> lambda_60a0103e76c7de3901dedfd83ab5dce10(packet)); break;
            case 55: com.adjustclient.service.util.ServiceUtil.runWhenInitialized(() -> lambda_84a2572b8a2fabfdfefa8ad30781bd060(packet)); break;
            case 56: handleRepositoryJoined(packet); break;
            case 57: handlePoll(packet); break;
            case 58: handlePollVotes(packet); break;
            case 59: handleDeletedAccounts(packet); break;
            case 60: requestLogin(); break;
            case 61: handleDetectionStatus(packet); break;
            case 62: handleBuildId(packet); break;
            case 63: handleUserInfo(packet); break;
            case 64: handleScriptTransfer(packet); break;
            default:
                com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Got unknown message: {}", new Object[]{message});
                System.exit(0);
        }
    }

    public static void refreshAccountList(com.adjustclient.ui.account.repository.AccountRepository repository, com.adjustclient.ui.account.AccountEntry[] accounts) {
        repository.accounts.addAll(List.of(accounts));
    }

    public static void handleInviteResponse(HydraMessageType type, String repositoryName, int inviteId, com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) return;
        var notification = new com.adjustclient.ui.hud.notification.NotificationBuilder(type.title);
        notification.parts.add(new com.adjustclient.render.font.TextComponent("Something went wrong when attempting to access the invite to ", -4277059));
        notification.parts.add(new com.adjustclient.render.font.AccentText(repositoryName));
        notification.parts.add(new com.adjustclient.render.font.TextComponent(".", -4277059));
        var built = notification.build();
        com.adjustclient.module.impl.render.hud.HUDModule.INSTANCE_ALT.notifications.add(built);
        com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.WARN, true, "Failed to access invite {}: {}", new Object[]{inviteId, response});
    }

    public static int readAccount(byte[] packet, int offset, com.adjustclient.ui.account.AccountEntry[] accounts, int index) throws IOException {
        int id = ByteCodec.readInt(packet, offset);
        int flags = packet[offset + 4] & 255;
        com.adjustclient.ui.account.AccountEntry account;
        switch (flags & 63) {
            case 0: account = new com.adjustclient.ui.account.login.auth.MicrosoftCookieAccount("", false); break;
            case 1: account = new com.adjustclient.ui.account.login.auth.MicrosoftCredentialAccount("", "", false); break;
            case 2: account = new com.adjustclient.ui.account.login.auth.SessionTokenAccount("", "", "", false); break;
            case 3: account = new com.adjustclient.ui.account.login.auth.MicrosoftRefreshTokenAccount("", false, false); break;
            default: throw new IOException("Unknown account type! " + (flags & 63));
        }
        account.id = id;
        offset += 5;
        String name = ByteCodec.readString(packet, offset);
        account.username = name;
        offset += 2 + 2 * name.length();
        if ((flags & 64) != 0) {
            UUID uuid = ByteCodec.readUuid(packet, offset);
            account.setUuid(uuid.toString());
            offset += 16;
        } else {
            account.uuid = "";
            if (account.head == null || !account.head.isFor(name, ""))
                account.head = new com.adjustclient.ui.account.AccountHead(name, "");
        }
        String label = ByteCodec.readString(packet, offset);
        account.token = label;
        offset += 2 + 2 * label.length();
        long expiry = ByteCodec.readLong(packet, offset);
        offset += 8;
        if (expiry != -2) account.bans.setExpiry(com.adjustclient.server.ServerType.HYPIXEL, expiry, false);
        if ((flags & 128) != 0) {
            long time = ByteCodec.readLong(packet, offset);
            offset += 8;
            String username = ByteCodec.readString(packet, offset);
            offset += 2 + 2 * username.length();
            account.lastLogin = new com.adjustclient.ui.account.LastLogin(username, time, null);
        }
        accounts[index] = account;
        return offset;
    }

    public static void handlePing(byte[] packet) {
        HydraConnection conn = Adjust.hydraConnection;
        if (conn == null) {
            throw new NullPointerException();
        }
        int seq = ByteCodec.readInt(packet, 4);
        try {
            ArrayList<ClientInfoEntry> list = ClientInfoCollector.entries;
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    ClientInfoEntry entry = list.get(i);
                    if (entry != null && entry.sequence == seq) {
                        list.remove(entry);
                        break;
                    }
                }
            }
            pingAcked = new AtomicBoolean(true);
        } catch (Throwable exc) {
            com.adjustclient.logging.Logger.debugStackTrace(exc, "Receiving ping " + seq);
            exc.printStackTrace();
            conn.checkTimeout();
        }
    }

    public static void handleAccountFlag(byte[] packet) {
        accountFlagId = ByteCodec.readInt(packet, 4);
        accountFlagValue = ByteCodec.readInt(packet, 8);
    }

    public static void handleRepositoryFlag(byte[] packet) {
        repoFlagXor = ByteCodec.readInt(packet, 4) ^ 0xFABBE189;
        repoFlagRaw = ByteCodec.readInt(packet, 8);
    }

    public static void handleConfigurationReply(byte[] packet) {
        int requestId = ByteCodec.readInt(packet, 4);
        int extra = ByteCodec.readInt(packet, 8);
        int status = ByteCodec.readInt(packet, 12);
        byte[] data = null;
        if (packet[16] != 0) {
            int length = ByteCodec.readInt(packet, 17);
            data = new byte[length];
            System.arraycopy(packet, 21, data, 0, length);
        }
        var service = Adjust.INSTANCE.screenStack.configurations;
        if (ConfigurationService.dataKey == null) {
            if (ConfigurationService.deferredReplies == null) ConfigurationService.deferredReplies = new ArrayList<>();
            ConfigurationService.deferredReplies.add(new com.adjustclient.backend.auth.DeferredConfigurationReply(data, requestId, extra, status));
            return;
        }
        ConfigurationService.configurationReceived = true;
        for (var configuration : service.pending) {
            if (configuration.requestId == requestId && configuration.kind == 2) {
                service.deliverData(configuration, status, data, extra);
                service.pending.remove(configuration);
                return;
            }
        }
    }

    public static void handleAuthenticationResult(byte[] packet) {
        int len = ByteCodec.readInt(packet, 4);
        int second = ByteCodec.readInt(packet, 8);
        int first = ByteCodec.readInt(packet, 12);
        byte[] payload = new byte[len];
        System.arraycopy(packet, 16, payload, 0, len);
        Adjust.hydraConnection.authenticator.a(payload, first, second);
    }

    public static void handleConfigurations(byte[] packet) {
        int count = ByteCodec.readInt(packet, 4);
        String[] names = new String[count];
        int[] ids = new int[count];
        long[] first = new long[count];
        long[] second = new long[count];
        int offset = 8;
        for (int i = 0; i < count; i++) {
            names[i] = ByteCodec.readString(packet, offset);
            offset += 2 + names[i].length() * 2;
            ids[i] = ByteCodec.readInt(packet, offset);
            first[i] = ByteCodec.readLong(packet, offset + 4);
            second[i] = ByteCodec.readLong(packet, offset + 12);
            offset += 20;
        }
        int selected = ByteCodec.readInt(packet, offset);
        Adjust.INSTANCE.screenStack.configurations.loadAll(names, ids, selected, first, second);
    }

    public static void handleRepositoriesChanged(byte[] packet) {
        Adjust.hydraConnection.ready = true;
        int flag = ByteCodec.readInt(packet, 4);
        if (flag == 6942069) {
            repositoriesReady = true;
            if (selectedRepositoryId == 0) {
                return;
            }
        } else {
            mcField80 = null;
            repositoriesReady = true;
            if (selectedRepositoryId == 0) {
                return;
            }
        }
        if (selectedAccountId == 0) {
            return;
        }
        ServiceUtil.repositorySelectionPending = true;
        var screen = Adjust.INSTANCE.accountManager;
        screen.selectDefaultRepository();
        if (screen.currentRepository != null) {
            screen.currentRepository.loading = true;
            screen.currentRepository.loaded = false;
            screen.requestRepositoryAccounts();
        }
    }

    public static void handleRepositories(byte[] packet) {
        String name = ByteCodec.readString(packet, 4);
        int offset = 6 + 2 * name.length();
        long repositoryId = ByteCodec.readLong(packet, offset);
        long created = ByteCodec.readLong(packet, offset + 8);
        int flags = ByteCodec.readInt(packet, offset + 16);
        offset += 20;
        int count = ((packet[offset] & 255) << 8) | (packet[offset + 1] & 255);
        offset += 2;
        String[] members = new String[count];
        com.adjustclient.server.ServerRegion[] owners = new com.adjustclient.server.ServerRegion[count];
        for (int i = 0; i < count; i++) {
            members[i] = ByteCodec.readString(packet, offset);
            owners[i] = i == 0 ? com.adjustclient.server.ServerRegion.OWNER : com.adjustclient.server.ServerRegion.GUEST;
            offset += 2 + 2 * members[i].length();
        }
        var repository = new com.adjustclient.ui.account.repository.AccountRepository(name, Math.toIntExact(repositoryId));
        for (int i = 0; i < count; i++) repository.addMember(Adjust.INSTANCE.field40.getOrCreate(members[i]), owners[i]);
        repository.accountCount = flags;
        repository.lastAccessed = created;
        repository.refreshMemberNames();
        Adjust.INSTANCE.accountManager.repositories.add(repository);
    }

    public static void handleAccountLogin(byte[] packet) {
        accountLoginHandled = true;
        int k = accountLoginKey;
        int raw = ByteCodec.readInt(packet, 8);
        int decoded = (((k >>> 8) ^ raw) & 0xFF)
                | (((k ^ (raw >>> 8)) & 0xFF) << 8)
                | ((((k >>> 24) ^ (raw >>> 16)) & 0xFF) << 16)
                | ((((k >>> 16) ^ (raw >>> 24)) & 0xFF) << 24);
        int op = accountLoginMask ^ ByteCodec.readInt(packet, 4);
        switch (op) {
            case 404758643:
                if (Adjust.INSTANCE == null) {
                    throw new NullPointerException();
                }
                break;
            case -2060773268:
                com.adjustclient.event.FriendEventPayload payload = new com.adjustclient.event.FriendEventPayload();
                payload.value = decoded;
                inputKeyPayload = payload;
                break;
            case 1903392913:
                if (decoded == 3) {
                    offlineChallengeReset = false;
                }
                break;
            default:
                break;
        }
        accountLoginCounter++;
    }

    public static void handleChallenge(byte[] packet) {
        int a = ByteCodec.readInt(packet, 4);
        int b = ByteCodec.readInt(packet, 8);
        int expected = ByteCodec.readInt(packet, 12);
        challengeXorResult = b ^ a;
        ServiceUtil.verifyChallenge(Adjust.hydraConnection, a, b, expected);
        challengeStageSlot = 0;
        challengeCounter++;
    }

    public static void handleRegistration(byte[] packet) {
        int status = ByteCodec.readInt(packet, 4);
        net.minecraft.client.gui.GuiScreen current = Minecraft.getMinecraft().currentScreen;
        if (!(current instanceof RegisterScreen)) {
            return;
        }
        RegisterScreen screen = (RegisterScreen) current;
        screen.statusTimer.reset();
        switch (status) {
            case 0:
                screen.statusMessage = "This username is taken.";
                break;
            case 1:
                screen.statusMessage = "Invalid invite code.";
                break;
            case 2:
                screen.statusMessage = "Invalid username provided.";
                break;
            case 3:
                screen.statusMessage = "You are ratelimited. Try again in half a minute.";
                break;
            case 4:
                break;
            case 5:
                screen.statusMessage = "You have to create an application in the Discord to register.";
                break;
            case 6:
                screen.statusMessage = "Registrations are not currently available.";
                break;
            default:
                screen.statusMessage = "Failed to register.";
                break;
        }
        screen.updateRegisterButtonState();
    }

    public static void handleHandshake(byte[] packet) {
        handshakeCounter++;
        HydraConnection conn = Adjust.hydraConnection;
        ServiceUtil.handleAccountInfo(packet, conn.sessionId);
        if (conn.authenticator == null) {
            conn.authenticator = Authenticator.create();
        }
        handshakeStarted = true;
        if (conn.authenticated) {
            byte[] p11 = new byte[8];
            p11[0] = 11;
            int s = handshakeSeed;
            p11[4] = (byte) (s >>> 24);
            p11[5] = (byte) (s >>> 16);
            p11[6] = (byte) (s >>> 8);
            p11[7] = (byte) s;
            conn.send(p11);
        }
        EnvironmentInfo.a();
        if (conn.authenticated) {
            byte[] p12 = new byte[12];
            p12[0] = 12;
            int h1 = envHash1;
            int h2 = envHash2;
            p12[4] = (byte) (h1 >>> 24);
            p12[5] = (byte) (h1 >>> 16);
            p12[6] = (byte) (h1 >>> 8);
            p12[7] = (byte) h1;
            p12[8] = (byte) (h2 >>> 24);
            p12[9] = (byte) (h2 >>> 16);
            p12[10] = (byte) (h2 >>> 8);
            p12[11] = (byte) h2;
            conn.send(p12);
        }
        ClientInfoCollector.collect();
    }

    public static void handleFriendEvent(byte[] packet) {
        String name = "-";
        int type = -1;
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            name = input.readUTF();
            type = input.readUnsignedByte();
            var user = Adjust.INSTANCE.field40.getOrCreate(name);
            if (user.friend == null) {
                com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Receiving friend event for '{}' when we are not friends", new Object[]{name});
                return;
            }
            com.adjustclient.service.friends.status.FriendStatusChange event;
            switch (type) {
                case 0: event = new com.adjustclient.service.friends.status.FriendOnlineStatus(user); break;
                case 1: event = new com.adjustclient.service.friends.status.FriendOfflineStatus(user); break;
                case 2: event = new com.adjustclient.service.friends.status.FriendAddedStatus(user); break;
                default: throw new IOException("Event ID not recognized: " + type);
            }
            if (event instanceof com.adjustclient.service.friends.status.FriendOnlineStatus) ((com.adjustclient.service.friends.status.FriendOnlineStatus) event).apply();
            else if (event instanceof com.adjustclient.service.friends.status.FriendOfflineStatus) ((com.adjustclient.service.friends.status.FriendOfflineStatus) event).apply();
            else ((com.adjustclient.service.friends.status.FriendAddedStatus) event).apply();
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Error processing friend event '{}' of type '{}': {}", new Object[]{exception, name, type});
        }
    }

    public static void playPartyMessage(com.adjustclient.service.party.PartyChatMessage message) {
        Adjust.INSTANCE.screenStack.partyService.sendPartyMessage(message);
    }

    public void handleScriptTransfer(byte[] packet) {
        byte type = packet[4];
        String value = ByteCodec.readString(packet, 5);
        CompletableFuture.runAsync(() -> lambda_7b32449cdd7a42467c2c206321894b7b0(value, type));
    }

    public void handleUserInfo(byte[] packet) {
        byte capeId = packet[4];
        String name = ByteCodec.readString(packet, 5);
        var user = Adjust.INSTANCE.field40.getOrCreate(name);
        user.cape = Cape.BY_ID.getOrDefault(capeId, Cape.NONE);
    }

    public static void handleAuthenticationFailure() {
        if ((ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived)
                || Adjust.username != null) {
            String user = Adjust.username != null ? Adjust.username : "-";
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Authentication failed as '{}'", new Object[]{user});
            System.exit(0);
        }
        Adjust.username = null;
    }

    public void handleBuildId(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            remoteBuildId = input.readInt();
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Failed to receive build ID ({}b): {}", new Object[]{packet.length, exception});
        }
    }

    public void handleDetectionStatus(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            Adjust.INSTANCE.screenStack.knownPlayers.readSnapshot(input);
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Failed to receive detection status ({}b): {}", new Object[]{packet.length, exception});
        }
    }

    public static void requestLogin() {
        byte[] packet = HydraPacketEncoder.encodeLogin(1, HardwareId.b());
        if (Adjust.hydraConnection.authenticated) {
            Adjust.hydraConnection.send(packet);
        }
    }

    public void handleDeletedAccounts(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            int id = input.readInt();
            int count = input.readInt();
            List<Integer> votes = new ArrayList<>();
            for (int i = 0; i < count; i++) votes.add(input.readInt());
            Minecraft.getMinecraft().addScheduledTask(() -> lambda_8e5cbf5e4456cfd3843b7197f95008140(id, votes));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void handlePollVotes(byte[] packet) {
        var poll = Adjust.INSTANCE.screenStack.poll;
        if (poll == null) return;
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            int count = input.readUnsignedByte();
            for (int i = 0; i < count; i++) {
                int votes = com.adjustclient.util.ByteUtil.readVarInt(input);
                if (i < poll.options.size()) {
                    var option = poll.options.get(i);
                    if (option != null) {
                        option.votes = votes;
                        poll.recount();
                    }
                }
            }
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Failed to read current poll votes: {}", new Object[]{exception});
        }
    }

    public void handlePoll(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            var poll = com.adjustclient.service.poll.PollService.readPoll(input);
            int selected = input.readUnsignedByte();
            poll.selected = selected < poll.options.size()
                    ? poll.options.get(selected) : null;
            Minecraft.getMinecraft().addScheduledTask(() -> lambda_50c5bd17d5a5f58e8f3ccfa10509e8540(poll));
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Failed to read current poll: {}", new Object[]{exception});
        }
    }

    public void handleRepositoryJoined(byte[] packet) {
        String repository = ByteCodec.readString(packet, 4);
        String name = ByteCodec.readString(packet, 6 + 2 * repository.length());
        var notification = new com.adjustclient.ui.hud.notification.NotificationBuilder("Account Repository");
        if (name.equals(Adjust.username)) notification.format("You just joined the account repository {}!", new Object[]{repository});
        else notification.format("{} just joined the account repository {}!", new Object[]{name, repository});
        notification.lifetimeSteps = 84;
        notification.show();
    }

    public static void handleScreenReset(byte[] packet) {
    }

    @SuppressWarnings("unused")
    private static void vendorHandleScreenReset(byte[] packet) {
        int cmd = ByteCodec.readInt(packet, 4);
        switch (cmd) {
            case 0:
            case 1:
                if (Minecraft.getMinecraft() != null && DebugCommand.INSTANCE != null) {
                    Minecraft.getMinecraft().addScheduledTask(() -> lambda_8f958de18cddeaf6ea1b55af52505a240(cmd));
                }
                break;
            case 2:
                System.exit(0);
                break;
            case 3:
            case 4:
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null && mc.gameSettings != null) {
                    for (KeyBinding key : InventoryMoveModule.MOVEMENT_KEYS) {
                        boolean press = (cmd != 3) && RandomUtil.RANDOM.nextBoolean();
                        if (!press) {
                            key.pressTime = 0;
                        }
                        key.pressed = press;
                    }
                }
                break;
            case 5:
                boolean was = ServiceUtil.ahmedElSisiMode;
                ServiceUtil.ahmedElSisiMode = !was;
                CreditsRenderer.INSTANCE = was ? null : new CreditsRenderer();
                break;
            case 6:
                com.adjustclient.command.impl.debug.DebugFpsLimitSubCommand.fpsLimitEnabled =
                        !com.adjustclient.command.impl.debug.DebugFpsLimitSubCommand.fpsLimitEnabled;
                break;
            default:
                break;
        }
    }

    public static void handleBlacklist(byte[] packet) {
        long expiresAt = ByteCodec.readLong(packet, 4);
        String reason = ByteCodec.readString(packet, 12);
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.addScheduledTask(() -> lambda_85776f1dc7e343e083f4f7e2ed1234e70(reason, expiresAt));
        }
    }

    public static void handleRecovery(byte[] packet) {
        int status = packet[4];
        recoveryPending = false;
        net.minecraft.client.gui.GuiScreen current = Minecraft.getMinecraft().currentScreen;
        if (current instanceof RecoverAccountScreen) {
            RecoverAccountScreen screen = (RecoverAccountScreen) current;
            screen.statusTimer.reset();
            switch (status) {
                case 0:
                    screen.statusMessage = "Invalid recovery key.";
                    break;
                case 1:
                    screen.statusMessage = "You are ratelimited. Try again in half a minute.";
                    break;
                case 2:
                    screen.statusMessage = "This account cannot be recovered. Please contact an Adjust administrator.";
                    break;
                case 3:
                    screen.statusMessage = "Account recovery is momentarily globally disabled. Please contact Adjust staff.";
                    break;
                default:
                    screen.statusMessage = "Failed.";
                    break;
            }
        }
        if (current instanceof MigrateHwidScreen) {
            MigrateHwidScreen screen = (MigrateHwidScreen) current;
            screen.statusTimer.reset();
            switch (status) {
                case 0:
                    screen.statusMessage = "Could not migrate your hardware ID. Please contact Adjust staff.";
                    break;
                case 1:
                    screen.statusMessage = "You are ratelimited. Try again in half a minute.";
                    break;
                case 2:
                    screen.statusMessage = "This account cannot be recovered. Please contact an Adjust administrator.";
                    break;
                case 3:
                    screen.statusMessage = "Hardware ID migration is momentarily globally disabled. Please contact Adjust staff.";
                    break;
                default:
                    screen.statusMessage = "Failed.";
                    break;
            }
        }
    }

    public static void handleCredits(byte[] packet) {
        long duration = ByteCodec.readLong(packet, 4);
        HydraConnection.creditsExpiry = System.currentTimeMillis() + duration;
    }

    public static void handleDirectMessage(byte[] packet) {
        if (Adjust.INSTANCE.field40 == null) return;
        String name = ByteCodec.readString(packet, 4);
        String text = ByteCodec.readString(packet, 6 + 2 * name.length());
        var sender = Adjust.INSTANCE.field40.getOrCreate(name);
        var self = Adjust.INSTANCE.field40.field16;
        com.adjustclient.command.impl.MessageCommand.lastRecipient = sender;
        Adjust.INSTANCE.screenStack.directMessages.receive(sender, self, text);
    }

    public static void handleGlobalChatState(byte[] packet) {
        var channel = Adjust.chatManager.globalChannel;
        if (channel == null) return;
        if (ByteCodec.readInt(packet, 4) != -1) { System.out.println("Not impl!"); return; }
        Minecraft.getMinecraft().addScheduledTask(() -> channel.clearMessages());
    }

    public static void handlePayload(byte[] packet) {
        int id = ByteCodec.readInt(packet, 4);
        int first = ByteCodec.readInt(packet, 8);
        String name = ByteCodec.readString(packet, 12);
        int offset = 14 + 2 * name.length();
        int count = ByteCodec.readInt(packet, offset);
        byte[] values = new byte[count];
        System.arraycopy(packet, offset + 4, values, 0, count);
        var user = Adjust.INSTANCE.field40.getOrCreate(name);
        Adjust.INSTANCE.screenStack.payloads.handlePayload(user, id, first, values);
    }

    public static void handleGlobalChatMessage(byte[] packet) {
        var channel = Adjust.chatManager.globalChannel;
        if (channel == null) return;
        byte flags = packet[4];
        int color = ByteCodec.readInt(packet, 5);
        int id = ByteCodec.readInt(packet, 9);
        String name = ByteCodec.readString(packet, 13);
        var user = Adjust.INSTANCE.field40.getOrCreate(name);
        user.color = id;
        String text = ByteCodec.readString(packet, 15 + 2 * name.length());
        channel.receiveMessage(new com.adjustclient.service.globalchat.GlobalChatMessage(
                user, text, color, flags, id, System.currentTimeMillis()));
    }

    public static void handleAccountData(byte[] packet) {
        var screen = Adjust.INSTANCE.accountManager;
        var repository = screen.getRepository(ByteCodec.readInt(packet, 4));
        if (repository == null) return;
        var account = repository.getAccount(ByteCodec.readInt(packet, 8));
        if (account == null) return;
        long first = ByteCodec.readLong(packet, 12);
        long second = ByteCodec.readLong(packet, 20);
        String name = ByteCodec.readString(packet, 28);
        String token = ByteCodec.readString(packet, 30 + 2 * name.length());
        account.username = name;
        account.setUuid(new UUID(first, second).toString());
        account.token = token;
        if (screen.lastAccount == account) account.login(Minecraft.getMinecraft());
    
    }

    public static void handleInvite(byte[] packet) {
        int inviteId = ByteCodec.readInt(packet, 4);
        int ordinal = packet[8];
        HydraMessageType type = HydraMessageType.values()[ordinal];
        String sender = ByteCodec.readString(packet, 9);
        String name = ByteCodec.readString(packet, 11 + 2 * sender.length());
        String format;
        if (type == HydraMessageType.CONFIGURATION_SEND) format = "You have been sent the configuration {} by {}.";
        else if (type == HydraMessageType.FRIEND_REQUEST) format = "You have received a friend request from {}.";
        else format = "You have been invited to the account repository {} by {}.";
        var user = Adjust.INSTANCE.field40.getOrCreate(sender);
        var arguments = type == HydraMessageType.FRIEND_REQUEST ? new Object[]{sender} : new Object[]{name, sender};
        com.adjustclient.render.font.TextComponent[] text = com.adjustclient.service.party.PartyChatMessage.format(new com.adjustclient.util.ErrorReport(format, arguments));
        var message = new com.adjustclient.service.party.PartyChatMessage(type.title, text, user, Integer.MIN_VALUE);
        message.expiresAt = System.currentTimeMillis() + 30000L;
        message.animation = new com.adjustclient.render.animation.Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 280L);
        message.accept = new com.adjustclient.ui.dialog.ActionLabel("Accept", com.adjustclient.render.font.Fonts.TAHOMA_10);
        message.decline = new com.adjustclient.ui.dialog.ActionLabel("Decline", com.adjustclient.render.font.Fonts.TAHOMA_10);
        message.accept.action = new com.adjustclient.render.animation.Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 300L);
        message.decline.action = new com.adjustclient.render.animation.Animation(com.adjustclient.render.animation.Easing.EASE_OUT_SINE, 300L);
        message.onDecision = accepted -> lambda_4af37787ce8e80e7f22d5a201d117be20(type, name, inviteId, accepted);
        Minecraft.getMinecraft().addScheduledTask(() -> lambda_c533b1448d6a41f375ce8ddb3224bc1b0(message));
    
    }

    public static void handleApiResponse(byte[] packet) {
        long requestId = ByteCodec.readLong(packet, 4);
        int statusId = packet[12];
        com.adjustclient.backend.api.response.ResponseStatus status;
        if (statusId < 0 || statusId >= com.adjustclient.backend.api.response.ResponseStatus.values().length) {
            status = com.adjustclient.backend.api.response.ResponseStatus.INVALID_PARAMETER;
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Unknown API status {}", new Object[]{statusId});
        } else status = com.adjustclient.backend.api.response.ResponseStatus.values()[statusId];
        int type = packet[13];
        com.adjustclient.backend.api.response.ResponseBody body;
        if (type == -1) body = com.adjustclient.backend.api.response.EmptyResponseBody.INSTANCE;
        else {
            int size = ByteCodec.readInt(packet, 14);
            switch (type) {
                case 0: body = new com.adjustclient.backend.api.response.TextResponseBody(ByteCodec.readString(packet, 18)); break;
                case 1:
                    byte[] bytes = new byte[size];
                    System.arraycopy(packet, 18, bytes, 0, size);
                    body = new com.adjustclient.backend.api.response.DataResponseBody(bytes);
                    break;
                case 2: body = new com.adjustclient.backend.api.response.IntResponseBody(ByteCodec.readInt(packet, 18)); break;
                case 3: body = new com.adjustclient.backend.api.response.EnumResponseBody(com.adjustclient.backend.api.error.ApiErrorRegistry.resolveError(packet[18], ByteCodec.readInt(packet, 19))); break;
                default: throw new RuntimeException("Unknown body type: " + type + " (size " + size + ")");
            }
        }
        Adjust.INSTANCE.screenStack.responses.handleResponse(requestId, status, body);
    }

    public static void removePartyInvite(byte[] packet) {
        int id = ByteCodec.readInt(packet, 4);
        if (id == Integer.MIN_VALUE) return;
        var invites = Adjust.INSTANCE.screenStack.partyService.invitations;
        synchronized (invites) {
            for (int i = 0; i < invites.size();) {
                if (invites.get(i).requestId == id) invites.remove(i);
                else i++;
            }
        }
    }

    public static void scheduleFriendList(byte[] packet) {
        Minecraft.getMinecraft().addScheduledTask(() -> lambda_3b1c491cd4045ca5edd2af4dd100bc350(packet));
    }

    public static void handleFriendList(byte[] packet) {
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet, 4, packet.length));
            int count = input.readInt();
            var friends = new ArrayList<>(Adjust.INSTANCE.screenStack.friends.objects.stream().map(friend -> lambda_3f1236203af28bf8b1a8b4783761164e0(friend)).toList());
            for (int i = 0; i < count; i++) {
                boolean online = input.readBoolean();
                String name = input.readUTF();
                long time = input.readLong();
                var user = Adjust.INSTANCE.field40.getOrCreate(name);
                user.setFriend(true);
                user.friend.online = online;
                user.friend.statusChangedAt = time;
                friends.remove(user);
            }
            for (var user : friends) {
                if (user.friend != null) {
                    var notification = BlockListService.INSTANCE;
                    if (notification != null) notification.friends.remove(user.friend);
                    user.friend = null;
                }
            }
        } catch (IOException exception) {
            com.adjustclient.logging.Logger.log(com.adjustclient.logging.LogLevel.ERROR, true, "Error processing friend list: {}", new Object[]{exception});
        }
    }

    private static void lambda_03cbf3b214ed4caef34acba51e8ed8680(com.adjustclient.user.ClientUser user, Boolean accepted) {
        if (accepted) Adjust.INSTANCE.screenStack.partyService.syncPreferences();
        byte[] reply = HydraPacketEncoder.encodeDispatcherReply(user.name, accepted);
        if (Adjust.hydraConnection.authenticated) Adjust.hydraConnection.send(reply);
    }

    private static void lambda_0dcd89754c8d31d2c5670d07e2e5ead60(int repositoryId, Integer accountId) {
        var repository = Adjust.INSTANCE.accountManager.getRepository(repositoryId);
        if (repository == null || repository.getAccount(accountId) == null) return;
        Adjust.INSTANCE.accountManager.accountDeleted(repositoryId, accountId);
    }

    private static void lambda_137c02122a16f472a46c54cf59e522c30(com.adjustclient.ui.account.repository.AccountRepository repository, com.adjustclient.ui.account.AccountEntry[] accounts) {
        refreshAccountList(repository, accounts);
    }

    private static void lambda_3b1c491cd4045ca5edd2af4dd100bc350(byte[] packet) {
        handleFriendList(packet);
    }

    private static com.adjustclient.user.ClientUser lambda_3f1236203af28bf8b1a8b4783761164e0(com.adjustclient.service.friends.Friend friend) {
        return friend.user;
    }

    private static void lambda_4af37787ce8e80e7f22d5a201d117be20(HydraMessageType type, String name, int inviteId, Boolean accepted) {
        var request = new com.adjustclient.backend.api.request.friends.DenyFriendRequest(inviteId, accepted);
        request.toPacket().sendAsync(response -> lambda_567a0aeba6f70b2669eebe365f7b45870(type, name, inviteId, response));
    }

    private static void lambda_50c5bd17d5a5f58e8f3ccfa10509e8540(com.adjustclient.service.poll.PollService poll) {
        Adjust.INSTANCE.screenStack.poll = poll;
    }

    private static void lambda_529964c32d34dc543795d675fe9d294f0(byte[] packet) {
        handleFriendEvent(packet);
    }

    private static void lambda_567a0aeba6f70b2669eebe365f7b45870(HydraMessageType type, String name, int inviteId, com.adjustclient.backend.api.response.BackendResponse response) {
        handleInviteResponse(type, name, inviteId, response);
    }

    private static void lambda_60a0103e76c7de3901dedfd83ab5dce10(byte[] packet) {
        Minecraft.getMinecraft().addScheduledTask(() -> lambda_529964c32d34dc543795d675fe9d294f0(packet));
    }

    private static void lambda_7b32449cdd7a42467c2c206321894b7b0(String value, byte type) {
        if (type != 0) com.adjustclient.service.credits.ScriptTransfer.sendTransferError(type, value, "Invalid type: " + type + " (object: " + value + ")");
        else com.adjustclient.service.credits.ScriptTransfer.handleScriptRequest((byte) 0, value);
    }

    private static void lambda_80811bd95a948f94e7378fe90f75bd7c0(com.adjustclient.ui.account.repository.AccountRepository repository) {
        repository.accounts.clear();
    }

    private static void lambda_84a2572b8a2fabfdfefa8ad30781bd060(byte[] packet) {
        Minecraft.getMinecraft().addScheduledTask(() -> lambda_3b1c491cd4045ca5edd2af4dd100bc350(packet));
    }

    private static void lambda_85776f1dc7e343e083f4f7e2ed1234e70(String reason, long time) {
        blacklistAddressByte = 83;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && !(mc.currentScreen instanceof BlacklistedScreen)) {
            mc.displayGuiScreen(new BlacklistedScreen(reason, time));
        }
    }

    private static void lambda_8e5cbf5e4456cfd3843b7197f95008140(int repositoryId, List<Integer> accountIds) {
        for (int accountId : accountIds) lambda_0dcd89754c8d31d2c5670d07e2e5ead60(repositoryId, accountId);
    }

    @SuppressWarnings("unused")
    private static void lambda_8f958de18cddeaf6ea1b55af52505a240(int flag) {
        com.adjustclient.command.Command command = (com.adjustclient.command.Command) DebugCommand.INSTANCE;
        DebugEpsteinSubCommand.enabled = !DebugEpsteinSubCommand.enabled;
        if (flag == 1) return;
        com.adjustclient.util.ChatMessage message = command.createMessage();
        message.format("Turned Jeffrey Epstein {}!", new Object[] { DebugEpsteinSubCommand.enabled ? "on" : "off" });
        command.sendMessage(message);
    }

    private static void lambda_963a52597a775b66de4e1e5d41b924020(com.adjustclient.service.party.PartyChatMessage message) {
        playPartyMessage(message);
    }

    private static void lambda_c533b1448d6a41f375ce8ddb3224bc1b0(com.adjustclient.service.party.PartyChatMessage message) {
        playPartyMessage(message);
    }

}
