package com.adjustclient.managers.party.ui;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.core.rendering.animation.*;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.managers.party.PartyChat;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.Vec2f;
import java.util.ArrayList;
import java.util.List;

public final class PartyPanel implements ContextMenuProvider {
    public List<ContextMenuKey> members = new ArrayList<>();
    public ContextMenuKey leader;
    public TextComponent[] title;
    public List<PartyPanelRow> rows = new ArrayList<>();
    public List<PartyPanelButton> visibleRows = java.util.Collections.emptyList();
    public Movable movable;
    public Animation scrollAnimation;
    public PartyChat chat;
    public Animation closeHoverAnimation;
    public AdjustContextMenu contextMenu;
    public com.adjustclient.utilities.Timer field44 = new com.adjustclient.utilities.Timer();
    public com.adjustclient.utilities.Timer messageTimer;
    public PartyPingRenderer pingRenderer = new PartyPingRenderer();
    public float width;
    public float contentHeight;
    public float scrollTarget;
    public int id;
    public boolean autoScroll = true;
    public boolean hasTypingMembers;

    public PartyPanel(int id, ClientUser leader) {
        this.id = id;
        this.leader = addMember(leader);
        scrollAnimation = new Animation(Easing.EASE_OUT_EXPO, 150L);
        closeHoverAnimation = new Animation(Easing.LINEAR, 100L);
        movable = ((com.adjustclient.managers.party.PartyOverlay)Adjust.INSTANCE.screenStack.field20).movable;
        chat = new PartyChat(this);
    }

    public void addContentHeight(float height) {
        contentHeight += height;
        chat.field40 = true;
        if (autoScroll) scrollTarget = Math.max(0.0F, Math.min(scrollTarget - 99999.0F,
                contentHeight - movable.height + 21.0F));
    }

    public ContextMenuKey addMember(ClientUser user) {
        for (ContextMenuKey key : members) if (key.user == user) return null;
        ContextMenuKey key = new ContextMenuKey(this, user);
        members.add(key);
        title = null;
        return key;
    }

    public boolean containsMemberName(String name) {
        synchronized (members) {
            for (ContextMenuKey key : members) if (key.user.name.equals(name)) return true;
            return false;
        }
    }

    public void previewEntityPing(net.minecraft.entity.Entity entity) {
        double x = (int)entity.posX;
        double y = (int)(entity.posX + Math.round(entity.height));
        EntityPing ping = new EntityPing(entity.getEntityId(), new net.minecraft.util.Vec3(x, y, x), null);
        ping.removed = true;
        ping.visible = true;
        pingRenderer.setPreview(ping);
    }

    private static ClientUser localUser() {
        if (Adjust.INSTANCE == null || !(Adjust.INSTANCE.field40 instanceof com.adjustclient.core.settings.property.UserManager)) {
            return null;
        }
        return (ClientUser) ((com.adjustclient.core.settings.property.UserManager) Adjust.INSTANCE.field40).field16;
    }

    public void sendEntityPing(int dimension, net.minecraft.entity.Entity entity) {
        double x = (int)entity.posX;
        double y = (int)(entity.posX + Math.round(entity.height));
        com.adjustclient.utilities.SoundUtil.play("party.ping", 1.0F, 1.5F);
        ClientUser local = localUser();
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) pingRenderer.receiveEntity(local, entity.getEntityId(), x, y, x);
        else mc.addScheduledTask(() -> pingRenderer.receiveEntity(local, entity.getEntityId(), x, y, x));
    }

    @SuppressWarnings("unused")
    private void vendorSendEntityPing(int dimension, net.minecraft.entity.Entity entity) {
        double x = (int)entity.posX;
        double y = (int)(entity.posX + Math.round(entity.height));
        synchronized (members) {
            java.util.Collection<ClientUser> present = Adjust.INSTANCE.screenStack.serverTracker.users.values();
            ClientUser local = localUser();
            for (ContextMenuKey key : members) {
                ClientUser user = key.user;
                if (user == local || !present.contains(user)) continue;
                com.adjustclient.managers.payload.PartyPingPayload payload = new com.adjustclient.managers.payload.PartyPingPayload();
                payload.dimension = dimension;
                payload.x = x;
                payload.y = y;
                payload.z = x;
                payload.entityId = entity.getEntityId();
                net.minecraft.network.PacketBuffer buffer = new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
                payload.write(buffer);
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                new com.adjustclient.utilities.backend.api.request.client.SendCustomPayloadRequest(user, 2, bytes).toPacket().sendAsync(null);
            }
        }
        com.adjustclient.utilities.SoundUtil.play("party.ping", 1.0F, 1.5F);
        ClientUser local = localUser();
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) pingRenderer.receiveEntity(local, entity.getEntityId(), x, y, x);
        else mc.addScheduledTask(() -> pingRenderer.receiveEntity(local, entity.getEntityId(), x, y, x));
    }

    public void sendBlockPing(int dimension, double x, double y, double z) {
        com.adjustclient.utilities.SoundUtil.play("party.ping", 1.0F, 1.5F);
        ClientUser local = localUser();
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) pingRenderer.receiveBlock(local, x, z, y);
        else mc.addScheduledTask(() -> pingRenderer.receiveBlock(local, x, z, y));
    }

    @SuppressWarnings("unused")
    private void vendorSendBlockPing(int dimension, double x, double y, double z) {
        synchronized (members) {
            java.util.Collection<ClientUser> present = Adjust.INSTANCE.screenStack.serverTracker.users.values();
            ClientUser local = localUser();
            for (ContextMenuKey key : members) {
                ClientUser user = key.user;
                if (user == local || !present.contains(user)) continue;
                com.adjustclient.managers.payload.PartyPingPayload payload = new com.adjustclient.managers.payload.PartyPingPayload();
                payload.dimension = dimension;
                payload.x = x;
                payload.y = y;
                payload.z = z;
                payload.entityId = -1;
                net.minecraft.network.PacketBuffer buffer = new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
                payload.write(buffer);
                byte[] bytes = new byte[buffer.readableBytes()];
                buffer.readBytes(bytes);
                new com.adjustclient.utilities.backend.api.request.client.SendCustomPayloadRequest(user, 2, bytes).toPacket().sendAsync(null);
            }
        }
        com.adjustclient.utilities.SoundUtil.play("party.ping", 1.0F, 1.5F);
        ClientUser local = localUser();
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) pingRenderer.receiveBlock(local, x, z, y);
        else mc.addScheduledTask(() -> pingRenderer.receiveBlock(local, x, z, y));
    }

    public void receiveBlockPing(ClientUser user, double x, double y, double z) {
        com.adjustclient.utilities.SoundUtil.play("party.ping", 1.0F, 1.2F);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) pingRenderer.receiveBlock(user, x, z, y);
        else mc.addScheduledTask(() -> {
            pingRenderer.receiveBlock(user, x, z, y);
        });
    }

    public void receiveEntityPing(int entityId, ClientUser user, double x, double y, double z) {
        com.adjustclient.utilities.SoundUtil.play("party.ping", 1.0F, 1.2F);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) pingRenderer.receiveEntity(user, entityId, x, y, z);
        else mc.addScheduledTask(() -> {
            pingRenderer.receiveEntity(user, entityId, x, y, z);
        });
    }

    public void scroll(boolean manual, float delta) {
        float maximum = contentHeight - movable.height + 21.0F;
        scrollTarget = Math.max(0.0F, Math.min(scrollTarget - delta, maximum));
        if (manual) autoScroll = scrollTarget == maximum;
    }

    public void replaceRow(PartyPanelRow previous, PartyPanelRow replacement) {
        rows.set(rows.indexOf(previous), replacement);
        if (previous.heightCounted) replacement.heightCounted = true;
    }

    public void layoutVisibleRows(float height, float inputHeight, float headerHeight) {
        scrollAnimation.update(scrollTarget);
        float typingOffset = contentHeight < height - inputHeight - headerHeight - 6.0F ? 0.0F
                : (autoScroll ? chat.typingHeight.value : 0.0F);
        List<PartyPanelButton> visible = new ArrayList<>(Math.min(10, rows.size() + 1));
        synchronized (rows) {
            float y = 14.5F;
            for (PartyPanelRow row : rows) {
                float rowY = y - scrollAnimation.value - typingOffset;
                int lines = chat.input.lines.length;
                if (lines >= 2) rowY -= (lines - 1) * 7.5F;
                if (rowY + row.height > 0.0F && rowY <= movable.height + 2.0F) {
                    visible.add(new PartyPanelButton(row, 0.0F, rowY));
                    if (chat.field40 && row == rows.get(rows.size() - 1))
                        chat.field40 = false;
                }
                y += row.height;
            }
        }
        visibleRows = visible;
    }

    public ContextMenuKey findMember(ClientUser user) {
        for (ContextMenuKey key : members) if (key.user == user) return key;
        return null;
    }

    public boolean isCloseButtonHovered(float mouseX, float mouseY) {
        Vec2f pos = movable.updatePosition();
        float right = pos.x + movable.width;
        return mouseX >= right - 9.0F && mouseX < right && mouseY >= pos.y && mouseY <= pos.y + 9.0F;
    }

    public boolean containsMember(ClientUser user) {
        synchronized (members) {
            for (ContextMenuKey key : members) if (key.user == user) return true;
            return false;
        }
    }

    public void updateTypingState() {
        synchronized (members) {
            hasTypingMembers = false;
            for (ContextMenuKey key : members) if (key.isTyping) { hasTypingMembers = true; break; }
        }
    }

    public ContextMenuAction createLeaveAction() {
        return new ContextMenuAction(new TextComponent[]{new TextComponent(
                leader.user.useClientColor ? "Disband Party" : "Leave Party", (int)4293480528L)},
                () -> {
                    Adjust.INSTANCE.screenStack.partyService.leaveParty(this);
                });
    }

    public ContextMenuKey thirdMember() {
        ClientUser local = localUser();
        for (ContextMenuKey key : members)
            if (key.user != local && key != leader) return key;
        return null;
    }

    public ContextMenuKey titleMember() {
        ClientUser local = localUser();
        ContextMenuKey result = leader;
        if (local != null && leader != null && leader.user == local)
            for (ContextMenuKey key : members) if (key.user != local) result = key;
        return result;
    }

    public void updateTitle() {
        List<TextComponent> title = new ArrayList<>();
        title.add(new TextComponent("Partying with ", 0xFFB2B2B2));
        title.add(new AccentText(titleMember().user.name));
        int size = members.size();
        if (size == 3) {
            title.add(new TextComponent(" and ", 0xFFB2B2B2));
            title.add(new AccentText(thirdMember().user.name));
        } else if (size > 3) title.add(new TextComponent(" and " + (size - 2) + " others", 0xFFB2B2B2));
        this.title = title.toArray(new TextComponent[0]);
        width = Math.max(160.0F, TextComponent.getWidth(this.title, Fonts.TAHOMA_10) + 16.0F);
        movable.width = width;
        movable.height = 290.0F;
        movable.headerHeight = 14.0F;
    }
}
