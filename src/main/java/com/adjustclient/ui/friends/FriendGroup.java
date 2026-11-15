package com.adjustclient.ui.friends;

import com.adjustclient.Adjust;
import com.adjustclient.bind.BindMode;
import com.adjustclient.render.animation.*;
import com.adjustclient.render.font.*;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.service.friends.Friend;
import com.adjustclient.service.party.PartyRequest;
import com.adjustclient.service.party.ui.*;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.*;
import net.minecraft.util.ChatColor;
import com.adjustclient.command.impl.PartyCommand;

public final class FriendGroup implements ContextMenuProvider {
    public FriendsScreen screen;
    public Animation scroll;
    public Friend selected;
    public Friend hovered;
    public AdjustContextMenu contextMenu;
    public float scrollTarget;
    public Object field28;

    public FriendGroup(FriendsScreen screen) {
        this.screen = screen;
        scroll = new Animation(Easing.EASE_OUT_EXPO, 150L);
    }

    public void drawContextMenu(Vec2f mouse) {
        if (contextMenu != null) {
            contextMenu.updateHover(mouse.x, mouse.y);
            contextMenu.render();
        }
    }

    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (!pressed) return false;
        if (contextMenu != null
                && contextMenu.handleMouse(button, BindMode.PRESSED, mouse.x, mouse.y)) return true;
        if (button == 0) {
            if (contextMenu != null) {
                contextMenu = null;
                return true;
            }
            if (hovered != null) {
                selected = hovered;
                return true;
            }
        } else if (button == 1) {
            if (hovered != null) {
                final Friend friend = hovered;
                List<ContextMenuEntry> actions = new ArrayList<>();
                actions.add(new ContextMenuAction(new TextComponent[] {new TextComponent("Remove Friend", new Color(233, 80, 80).getRGB())}, () -> {
                    screen.removeFriend(friend);
                }));
                contextMenu = new AdjustContextMenu(this, mouse.x, mouse.y);
                contextMenu.entries = actions;
                return true;
            }
            contextMenu = null;
        }
        return false;
    }

    public float draw(Vec2f mouse, float x, float y) {
        com.adjustclient.service.friends.BlockListService manager = Adjust.INSTANCE.screenStack.blockList;
        long online = manager.friends.stream().filter(friend -> friend.online)
                .count();
        int count = manager.friends.size();
        String title = String.valueOf(ChatColor.BOLD) + count + String.valueOf(ChatColor.RESET) + " friend" + (count == 1 ? "" : "s");
        if (online > 0) title += " (" + String.valueOf(ChatColor.BOLD) + online + String.valueOf(ChatColor.RESET) + " online)";
        if (Fonts.TAHOMA_12.getStringWidth(title) > 70.0F)
            title = Fonts.TAHOMA_12.trimStringToWidth(title, 70.0F - Fonts.TAHOMA_12.getStringWidth("..."), false) + "...";
        Fonts.TAHOMA_12.drawString(title, x + 35.0F - Fonts.TAHOMA_12.getStringWidth(title) * 0.5F,
                y + 15.0F, 0xe4e1e4e4);
        float left = x + 2.0F, top = y + 23.5F;
        float listHeight = screen.height - 23.5F - 2.0F;
        NvgContext.pushEvenScale();
        org.joml.Vector4f bounds = NvgContext.toPixels(left, top, left + 70.0F, top + listHeight);
        NvgDraw.fillRect(1428431908, bounds.x, bounds.y, bounds.z, bounds.w);
        NvgDraw.strokeRect(false, (int) 2183406628L, bounds.x, bounds.y, bounds.z, bounds.w, 0.5F);
        org.joml.Vector4f divider = NvgContext.toPixels(x, top - 2.5F, left + 72.0F, top - 2.0F);
        NvgDraw.fillRect(1681800766, divider.x, divider.y, divider.z, divider.w);
        org.joml.Vector4f vertical = NvgContext.toPixels(left + 72.0F, y + 11.0F, left + 72.5F, y + screen.height);
        NvgDraw.fillRect(1681800766, vertical.x, vertical.y, vertical.z, vertical.w);
        NvgContext.popEvenScale();
        drawRows(manager, mouse, left, top, 70.0F, listHeight);
        return vertical.z;
    }

    public void drawRows(com.adjustclient.service.friends.BlockListService manager, Vec2f mouse, float x, float y, float width, float height) {
        java.util.function.ToLongFunction<Friend> changedAt = friend -> friend.statusChangedAt;
        java.util.function.ToIntFunction<Friend> presence = friend -> friend.online ? -1 : 1;
        List<Friend> rows = manager.friends.stream()
                .sorted(Comparator.comparingLong(changedAt).reversed())
                .sorted(Comparator.comparingInt(presence))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        int recentIndex = 0;
        for (com.adjustclient.user.ClientUser user : Adjust.INSTANCE.screenStack.directMessages.recentRecipients) {
            if (user.friend != null && user.friend.online) {
                rows.remove(user.friend);
                rows.add(recentIndex, user.friend);
            }
            recentIndex++;
        }
        scrollTarget = Math.min(scrollTarget, Math.max(0.0F, rows.size() * 10.0F - height));
        scroll.update(scrollTarget);
        hovered = null;
        boolean selectionPresent = false;
        NvgDraw.pushScissor(x, y, width, height);
        float rowY = y - scroll.value;
        for (Friend friend : rows) {
            if (selected == null) selected = friend;
            boolean isSelected = friend.equals(selected);
            selectionPresent |= isSelected;
            boolean hit = mouse.x >= x && mouse.x < x + width && mouse.y >= y && mouse.y < y + height
                    && mouse.y >= rowY && mouse.y < rowY + 10.0F;
            if (hit) hovered = friend;
            if (friend.field12 == null) friend.field12 = new Animation(Easing.LINEAR, 100L);
            Animation hover = (Animation) friend.field12;
            hover.update(hit ? 1.0F : 0.0F);
            NvgDraw.fillRect(new Color(53, 53, 53, (int) (hover.value * 69.0F)).getRGB(), x, rowY, x + width, rowY + 10.0F);
            if (rowY + 10.0F >= y) {
                float emphasis = friend.online ? Math.max(hover.value, 0.7F) : hover.value;
                int alpha = (int) (emphasis * 85.0F) + 140;
                int grey = (int) (emphasis * 20.0F) + 220;
                int color = isSelected
                        ? com.adjustclient.render.ColorUtil.withAlpha(com.adjustclient.module.impl.render.hud.HUDModule.animatedColor != null
                                ? com.adjustclient.module.impl.render.hud.HUDModule.animatedColor
                                : com.adjustclient.module.impl.render.hud.HUDModule.colorSetting.getColor(), alpha).getRGB()
                        : new Color(grey, grey, grey, alpha).getRGB();
                Fonts.TAHOMA_12.drawString((isSelected || hit ? String.valueOf(ChatColor.BOLD) : "") + friend.user.name,
                        x + 2.0F, rowY + 3.5F, color);
            }
            if (rowY + 10.0F >= y + height) break;
            rowY += 10.0F;
        }
        NvgDraw.popScissor();
        if (!selectionPresent) selected = null;
    }

    @SuppressWarnings("unused")
    private void vendorInviteToParty(Friend friend) {
        com.adjustclient.service.party.PartyService party = Adjust.INSTANCE.screenStack.partyService;
        party.syncPreferences();
        int id = PartyCommand.nextInviteRequestId++;
        party.requests.add(new PartyRequest(id, status -> FriendsScreen.onInviteResponse(friend.user, status)));
        com.adjustclient.service.util.ServiceUtil.sendToBackend(
                com.adjustclient.backend.transport.HydraPacketEncoder.encodePartyInvite(friend.user.name, id));
    }
}
