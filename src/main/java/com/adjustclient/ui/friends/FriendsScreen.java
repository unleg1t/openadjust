package com.adjustclient.ui.friends;

import com.adjustclient.Adjust;
import com.adjustclient.backend.api.error.FriendsError;
import com.adjustclient.backend.api.request.friends.AddFriendRequest;
import com.adjustclient.backend.api.request.friends.RemoveFriendRequest;
import com.adjustclient.backend.api.response.*;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.render.font.*;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.server.ServerInfo;
import com.adjustclient.service.friends.Friend;
import com.adjustclient.ui.dialog.*;
import com.adjustclient.ui.home.IconActionButton;
import com.adjustclient.ui.screen.AdjustScreen;
import com.adjustclient.ui.screen.transition.*;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.Vec2f;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import net.minecraft.util.ChatColor;
import com.adjustclient.backend.api.response.ResponseStatus;

public final class FriendsScreen extends AdjustScreen {
    public DimTransition dimTransition;
    public PopupTransition popupTransition;
    public List<IconActionButton> buttons = new ArrayList<>();
    public IconActionButton add;
    public IconActionButton dismiss;
    public Vec2f position;
    public Vec2f dragOffset;
    public FriendGroup group;
    public FriendEntry detailsPane;
    public float width = 280.0F;
    public float height = 170.0F;
    public Object field52;

    public FriendsScreen() {
        blocksGameInteraction = true;
        dimTransition = new DimTransition();
        popupTransition = new PopupTransition(this, 400L);
        group = new FriendGroup(this);
        detailsPane = new FriendEntry(this, group);
        add = new IconActionButton("+", () -> openAddFriendDialog());
        add.button.font = Fonts.TAHOMA_12;
        dismiss = new IconActionButton("x", () -> popupTransition.close());
        dismiss.button.font = Fonts.TAHOMA_12;
        buttons.add(add);
        buttons.add(dismiss);
    }

    @Override public boolean keyTyped(char character, int key) {
        TextInput input = detailsPane.input;
        int enter = Keyboard.KEY_RETURN, escape = Keyboard.KEY_ESCAPE;
        Friend friend = group.selected;
        if (key == enter && friend != null && friend.online) {
            if (!input.focused) { input.focused = true; return true; }
            String message = input.field.getText();
            if (!message.isBlank()) {
                message = com.adjustclient.service.party.PartyChat.expandQuickMessages(message);
                ChatMessage failure = new ChatMessage();
                failure.append("Friends ");
                failure.color = ChatColor.GRAY;
                Adjust.INSTANCE.screenStack.directMessages.send(friend.user, message, failure);
            }
            input.field.setText("");
            input.focused = false;
            return true;
        }
        if (key == escape && input.focused) { input.focused = false; return true; }
        if (!input.focused) {
            if (key != escape) return false;
            popupTransition.close();
            return true;
        }
        input.field.setFocused(true);
        if (input.field.textboxKeyTyped(character, key)) return true;
        if (key == escape) { popupTransition.close(); return true; }
        return false;
    }

    @SuppressWarnings("unused")
    private static void vendorOnRemoveResponse(ClientUser user, BackendResponse response) {
        if (response.status == ResponseStatus.OK) {
            if (response.body instanceof EnumResponseBody && ((EnumResponseBody) response.body).value instanceof FriendsError) {
                ChatMessage text = new ChatMessage();
                ((FriendsError) ((EnumResponseBody) response.body).value).handler.accept(user, text);
                com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
                notification.title = "Friends";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.addText(text.getComponent().getFormattedText(), 0xffbebcbd).show();
            } else System.out.println("An unknown error occurred: " + response);
        } else if (response.status == ResponseStatus.RATELIMITED) {
            com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = "Friends";
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("You are being ratelimited. Please wait a little bit before trying again.", 0xffbebcbd).show();
        } else Logger.log(LogLevel.WARN, true, "Unable to remove friend '{}': {}", new Object[] {user.name, response});
    }

    public static void onInviteResponse(ClientUser user, ServerInfo status) {
        com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
        notification.title = "Friends";
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        if (status == ServerInfo.UNKNOWN_USER) {
            notification.addText("The user ", 0xffbebcbd).append(user.name).addText(" no longer exists.", 0xffbebcbd).show();
        } else if (status == ServerInfo.INVITED) {
            notification.addText("Invited ", 0xffbebcbd).append(user.name)
                    .addText(" to your party! They have ", 0xffbebcbd).append("30 seconds").addText(" to accept.", 0xffbebcbd).show();
        } else if (status == ServerInfo.ALREADY_PENDING) {
            notification.addText("An invite is already pending! Wait for ", 0xffbebcbd).append(user.name).addText(" to accept.", 0xffbebcbd).show();
        } else {
            String reason;
            if (status == ServerInfo.ALREADY_IN_PARTY) reason = " because they are already in a party!";
            else if (status == ServerInfo.INVITE_SETTINGS) reason = " because of their invite settings!";
            else if (status == ServerInfo.BLOCKED) reason = " because they blocked you!";
            else if (status == ServerInfo.SOCIAL_SETTINGS) reason = " because of their social settings!";
            else if (status == ServerInfo.OFFLINE) reason = " because they aren't online!";
            else return;
            notification.addText("Cannot invite ", 0xffbebcbd).append(user.name).addText(reason, 0xffbebcbd).show();
        }
    }

    @Override public void mouseScrolled(Vec2f mouse, int button, boolean pressed, int wheel, float delta) {
        if (position != null && mouse.x >= position.x + 2.0F && mouse.x < position.x + 72.0F
                && mouse.y >= position.y + 23.5F && mouse.y < position.y + height - 2.0F) {
            group.scrollTarget = Math.max(0.0F, group.scrollTarget + delta * 10.0F);
        } else if (detailsPane.atBottom) {
            detailsPane.scrollTarget = Math.min(0.0F, detailsPane.scrollTarget + delta * 8.5F);
        }
    }

    public void drawFrame(ScaledResolution window, float x, float y) {
        dimTransition.draw(window, 65.0F);
        NvgDraw.fillRect((int) 3725792019L, x, y, x + width, y + height);
        com.adjustclient.render.RenderUtil.drawAccentGradient(x, y, width, 11.0F, 0.0F, 0.0F);
        String title = String.valueOf(ChatColor.BOLD) + "Friends";
        Fonts.TAHOMA_12.drawString(title, x + width * 0.5F - Fonts.TAHOMA_12.getStringWidth(title) * 0.5F, y + 4.5F, 0xcae1e4e4);
    }

    public void render(Object context, Vec2f mouse, ScaledResolution window) {
        if (popupTransition.getProgress()) return;
        if (position == null) position = new Vec2f(window.getScaledWidth() * 0.5F - width * 0.5F, window.getScaledHeight() * 0.5F - height * 0.5F);
        if (dragOffset != null) position = new Vec2f(mouse.x + dragOffset.x, mouse.y + dragOffset.y);
        position.x = Math.min(Math.max(position.x, 0.0F), window.getScaledWidth() - width);
        position.y = Math.min(Math.max(position.y, 0.0F), window.getScaledHeight() - height);
        NvgDraw.setGlobalAlpha(popupTransition.animation.value);
        drawFrame(window, position.x, position.y);
        drawButtons(mouse, position.x, position.y);
        float listTop = group.draw(mouse, position.x, position.y);
        detailsPane.drawDetails(mouse, window, position.x, position.y, listTop);
        group.drawContextMenu(mouse);
        NvgDraw.setGlobalAlpha(1.0F);
    }

    public void drawButtons(Vec2f mouse, float x, float y) {
        dismiss.x = x + width - 9.0F;
        dismiss.y = y + 2.5F;
        add.x = x + width - 18.0F;
        add.y = y + 2.5F;
        for (IconActionButton button : buttons) {
            button.button.x = button.x;
            button.button.y = button.y;
            button.button.draw(mouse.x, mouse.y);
        }
    }

    @Override public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (dragOffset != null && button == 0 && !pressed) { dragOffset = null; return true; }
        for (IconActionButton action : buttons) {
            if (button == 0 && pressed && action.button.isHovered(mouse.x, mouse.y)) {
                action.action.run();
                return true;
            }
        }
        if (detailsPane.mouseClicked(mouse, button, pressed)) return true;
        if (position == null || mouse.x < position.x || mouse.x >= position.x + width
                || mouse.y < position.y || mouse.y >= position.y + height) return false;
        if (button == 0 && pressed && mouse.y < position.y + 12.0F && mouse.x <= position.x + width - 21.0F) {
            dragOffset = new Vec2f(position.x - mouse.x, position.y - mouse.y);
            return true;
        }
        return group.mouseClicked(mouse, button, pressed);
    }

    @SuppressWarnings("unused")
    private static void vendorOnAddResponse(ClientUser user, BackendResponse response) {
        if (response.status == ResponseStatus.OK) {
            if (response.body instanceof EnumResponseBody && ((EnumResponseBody) response.body).value instanceof FriendsError) {
                ChatMessage text = new ChatMessage();
                ((FriendsError) ((EnumResponseBody) response.body).value).handler.accept(user, text);
                com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
                notification.title = "Friends";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.addText(text.getComponent().getFormattedText(), 0xffbebcbd).show();
            } else System.out.println("An unknown error occurred: " + response);
        } else if (response.status == ResponseStatus.RATELIMITED) {
            com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = "Friends";
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("You are being ratelimited. Please wait a little bit before trying again.", 0xffbebcbd).show();
        } else Logger.log(LogLevel.WARN, true, "Unable to send friend request '{}': {}", new Object[] {user.name, response});
    }

    public void removeFriend(Friend friend) {
        if (friend == null) return;
        ClientUser user = friend.user;
        DialogBuilder builder = new DialogBuilder("Remove friend '" + user.name + "'?");
        builder.message.append("Are you sure you want to remove ");
        builder.message.italic = true;
        builder.message.append(user.name).append(" from your friends?");
        builder.addButton("Remove", dialog -> {
            user.setFriend(false);
            if (group.selected == friend) group.selected = null;
        }, DialogBuilder.ALWAYS_ENABLED);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build();
    }

    @SuppressWarnings("unused")
    private void vendorRemoveFriend(Friend friend) {
        if (friend == null) return;
        ClientUser user = friend.user;
        DialogBuilder builder = new DialogBuilder("Remove friend '" + user.name + "'?");
        builder.message.append("Are you sure you want to remove ");
        builder.message.italic = true;
        builder.message.append(user.name).append(" from your friends?");
        builder.addButton("Remove", dialog -> {
            new RemoveFriendRequest(user).toPacket().sendAsync(response -> vendorOnRemoveResponse(user, response));
        }, DialogBuilder.ALWAYS_ENABLED);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build();
    }

    @Override public void onOpen() { group.contextMenu = null; }

    public void tick() { detailsPane.input.field.updateCursorCounter(); }

    public static void openAddFriendDialog() {
        DialogBuilder builder = new DialogBuilder("Send friend request");
        builder.message.append("Enter an Adjust username:");
        builder.addTextInput("Username", "", 16, 0);
        builder.addButton("Send", dialog -> {
            String name = dialog.textInputs.stream().filter(element -> element.index == 0).findFirst()
                    .map(element -> element.box.input.field.getText()).orElse(null);
            if (name == null || name.isBlank()) return;
            ClientUser user = ((com.adjustclient.property.UserManager) Adjust.INSTANCE.field40).getOrCreate(name.trim());
            if (user.useClientColor) {
                com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
                notification.title = "Friends";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.addText("You cannot add yourself as a friend.", 0xffbebcbd).show();
                return;
            }
            user.setFriend(true);
        }, DialogBuilder.ALWAYS_ENABLED);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build();
    }

    @SuppressWarnings("unused")
    private static void vendorOpenAddFriendDialog() {
        DialogBuilder builder = new DialogBuilder("Send friend request");
        builder.message.append("Enter an Adjust username:");
        builder.addTextInput("Username", "", 16, 0);
        builder.addButton("Send", dialog -> {
            String name = dialog.textInputs.stream().filter(element -> element.index == 0).findFirst()
                    .map(element -> element.box.input.field.getText()).orElse(null);
            ((com.adjustclient.property.UserManager) Adjust.INSTANCE.field40).lookup(name, user -> {
                if (user == null) {
                    com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
                    notification.title = "Friends";
                    notification.parts = new ArrayList<>();
                    notification.actions = new ArrayList<>();
                    notification.addText("No user by the name ", 0xffbebcbd).append(name).addText(" exists!", 0xffbebcbd).show();
                    return;
                }
                new AddFriendRequest(user).toPacket().sendAsync(response -> vendorOnAddResponse(user, response));
            });
        }, DialogBuilder.ALWAYS_ENABLED);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build();
    }
}
