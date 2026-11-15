package com.adjustclient.ui.friends;

import com.adjustclient.Adjust;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.*;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.render.NvgRenderer;
import com.adjustclient.service.friends.Friend;
import com.adjustclient.service.friends.FriendDisplay;
import com.adjustclient.service.party.PartyRequest;
import com.adjustclient.ui.home.IconActionButton;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;
import com.adjustclient.command.impl.PartyCommand;

public final class FriendEntry {
    public FriendsScreen screen;
    public FriendGroup group;
    public List<IconActionButton> buttons = new ArrayList<>();
    public IconActionButton remove;
    public IconActionButton invite;
    public Timer inviteTimer = new Timer(0L);
    public TextInput input;
    public Animation scroll;
    public float scrollTarget;
    public boolean atBottom;
    public Object field44;

    public FriendEntry(FriendsScreen screen, FriendGroup group) {
        this.screen = screen;
        this.group = group;
        input = new TextInput("", java.util.Objects.requireNonNull(Fonts.TAHOMA_11, "text input font: " + Fonts.TAHOMA_11), 100);
        input.lines = new String[0];
        scroll = new Animation(Easing.EASE_OUT_EXPO, 150L);
        remove = new IconActionButton("c", () -> screen.removeFriend(group.selected));
        invite = new IconActionButton("l", () -> {
            inviteTimer.reset();
        });
        buttons.add(remove);
        buttons.add(invite);
    }

    @SuppressWarnings("unused")
    private void vendorInviteToParty(Friend friend) {
        if (friend != null) {
            com.adjustclient.service.party.PartyService party = Adjust.INSTANCE.screenStack.partyService;
            party.syncPreferences();
            int id = PartyCommand.nextInviteRequestId++;
            party.requests.add(new PartyRequest(id, status -> FriendsScreen.onInviteResponse(friend.user, status)));
            com.adjustclient.service.util.ServiceUtil.sendToBackend(
                    com.adjustclient.backend.transport.HydraPacketEncoder.encodePartyInvite(friend.user.name, id));
        }
        inviteTimer.reset();
    }

    public void onMessageAdded(Friend friend) {
        if (scrollTarget < 0.0F && group.selected != null && group.selected.equals(friend)) {
            friend.messages.getLast().field28 = true;
        }
    }

    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        for (IconActionButton action : buttons) {
            if (button == 0 && pressed && action.button.isHovered(mouse.x, mouse.y)) {
                action.action.run();
                return true;
            }
        }
        if (button == 0 && pressed && group.selected != null && group.selected.online && input.hovered) {
            input.focused = true;
            return true;
        }
        if (pressed) input.focused = false;
        return false;
    }

    public void drawButtons(Vec2f mouse, float right, float y) {
        Friend friend = group.selected;
        invite.button.enabled = friend != null && friend.online
                && inviteTimer.hasElapsed(2100L)
                && (Adjust.INSTANCE.screenStack.partyService.party == null
                    || !Adjust.INSTANCE.screenStack.partyService.party.containsMember(friend.user));
        remove.x = right - 9.0F;
        remove.y = y + 2.5F;
        invite.x = right - 18.0F;
        invite.y = y + 2.5F;
        for (IconActionButton action : buttons) {
            action.button.x = action.x;
            action.button.y = action.y;
            action.button.draw(mouse.x, mouse.y);
        }
    }

    public void drawDetails(Vec2f mouse, ScaledResolution window, float x, float y, float listTop) {
        Friend friend = group.selected;
        if (friend == null) return;
        float left = x + 74.5F;
        float top = y + 15.0F;
        float paneWidth = screen.width - 74.5F;
        String name = String.valueOf(ChatColor.BOLD) + friend.user.name;
        String status;
        int color;
        if (friend.online) {
            status = "online for " + String.valueOf(ChatColor.GREEN)
                    + com.adjustclient.util.TimeUtil.formatDuration(System.currentTimeMillis() - friend.statusChangedAt, false);
            color = 0xe4e1e4e4;
        } else {
            status = "last online " + String.valueOf(ChatColor.RED)
                    + com.adjustclient.util.TimeUtil.timeAgo(friend.statusChangedAt, " ago");
            color = 0xc8c8c8c8;
        }
        float textX = left + 2.5F;
        Fonts.TAHOMA_10.drawString(status, textX + Fonts.TAHOMA_12.getStringWidth(name) + 1.5F, top + 1.5F, color);
        Fonts.TAHOMA_12.drawString(name, textX, top, color);
        NvgContext.pushEvenScale();
        org.joml.Vector4f divider = NvgContext.toPixels(left, y + 21.0F, left + paneWidth, y + 21.5F);
        NvgDraw.fillRect(1681800766, listTop, divider.y, divider.z, divider.w);
        drawMessages(friend, mouse, window, left, top + 8.5F, paneWidth, screen.height - 15.0F - 8.5F - 2.0F);
        drawButtons(mouse, left + paneWidth, screen.position.y + 11.0F);
    }

    public void drawMessages(Friend friend, Vec2f mouse, ScaledResolution window, float x, float y, float width, float height) {
        float left = x + 2.0F;
        float innerWidth = width - 4.0F;
        float right = left + innerWidth;
        float contentWidth = innerWidth - 2.0F;
        float contentRight = left + contentWidth;
        float bottom = y + height;
        float inputHeight = 9.5F + Math.max(0, input.lines.length - 1) * 7.5F;
        float inputY = bottom - inputHeight;
        float inputTop = inputY + 0.5F;
        NvgContext.pushEvenScale();
        org.joml.Vector4f bounds = NvgContext.toPixels(left, y, right, bottom);
        NvgDraw.fillRect(1428431908, bounds.x, bounds.y, bounds.z, bounds.w);
        NvgDraw.strokeRect(false, (int) 2183406628L, bounds.x, bounds.y, bounds.z, bounds.w, 0.5F);
        org.joml.Vector4f separator = NvgContext.toPixels(left, inputY, right, inputTop);
        NvgDraw.fillRect((int) 2183406628L, separator.x, separator.y, separator.z, separator.w);
        NvgContext.popEvenScale();
        atBottom = mouse.x >= left && mouse.x < right + 2.0F && mouse.y >= y && mouse.y < bottom;
        if (friend.messages != null) {
            float totalHeight = 0.0F;
            for (int i = friend.messages.size() - 1; i >= 0; i--) {
                FriendDisplay message = friend.messages.get(i);
                if (message.lines == null) message.wrap((int) contentWidth);
                float messageHeight = Math.max(0, message.lines.length - 1) * 7.0F + 8.5F;
                if (message.field28) {
                    scroll.value -= messageHeight;
                    scrollTarget -= messageHeight;
                    message.field28 = false;
                }
                totalHeight += messageHeight;
            }
            scrollTarget = Math.max(scrollTarget, Math.min(0.0F, height - inputHeight - totalHeight));
            scroll.update(scrollTarget);
            NvgDraw.pushScissor(left, y, innerWidth, height - inputHeight);
            float cursor = inputY - 8.5F;
            for (int i = friend.messages.size() - 1; i >= 0; i--) {
                FriendDisplay message = friend.messages.get(i);
                float textHeight = Math.max(0, message.lines.length - 1) * 7.0F;
                float lineBottom = cursor - scroll.value;
                float lineTop = lineBottom - textHeight;
                boolean hovered = mouse.x >= left && mouse.x < contentRight && mouse.y >= lineTop && mouse.y < lineBottom + 8.5F;
                if (message.field20 == null) message.field20 = new Animation(Easing.LINEAR, 100L);
                Animation hover = (Animation) message.field20;
                hover.update(hovered ? 1.0F : 0.0F);
                if (lineBottom <= bottom) {
                    NvgDraw.fillRect(new Color(53, 53, 53, (int) (hover.value * 69.0F)).getRGB(),
                            left, lineTop, contentRight, lineBottom + 8.5F);
                    message.draw(left + 2.0F, lineTop + 3.5F);
                }
                if (lineBottom < y) break;
                cursor -= textHeight + 8.5F;
            }
            NvgDraw.popScissor();
        }
        input.setWidth((int) contentWidth);
        if (!friend.online) {
            input.field.setText("");
            input.focused = false;
            input.placeholder = String.valueOf(ChatColor.WHITE) + friend.user.name + String.valueOf(ChatColor.RESET) + " is offline";
            input.hovered = false;
        } else {
            input.placeholder = "Press " + String.valueOf(ChatColor.WHITE) + "ENTER" + String.valueOf(ChatColor.RESET) + " to send a message";
            input.hovered = mouse.x >= left && mouse.x < contentRight && mouse.y >= inputTop && mouse.y < inputTop + inputHeight;
            if (input.hovered) com.adjustclient.render.RenderUtil.cursor = 221186;
        }
        NvgRenderer renderer = new NvgRenderer();
        input.draw(renderer, left + 2.0F, inputTop + 3.5F);
        renderer.flush(window);
    }
}
