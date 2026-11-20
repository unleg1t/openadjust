package com.adjustclient.core.gui.config;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.backend.api.request.config.*;
import com.adjustclient.utilities.backend.api.response.*;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.core.gui.dialog.*;
import com.adjustclient.core.gui.home.IconActionButton;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.core.gui.screen.AdjustScreen;
import com.adjustclient.core.gui.screen.transition.*;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.Vec2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;
import java.io.*;
import java.util.*;
import com.adjustclient.managers.ScreenStack;

public final class GlobalConfigsScreen extends AdjustScreen {
    private static final Timer REFRESH_TIMER = new Timer(0L);
    public boolean fetching;
    public Vec2f position;
    public DimTransition dim;
    public PopupTransition popup;
    public List<IconActionButton> controls;
    public IconActionButton refreshButton;
    public IconActionButton publishButton;
    public IconActionButton closeButton;
    public Vec2f dragOffset;
    public GlobalConfigList featured;
    public GlobalConfigList[] lists;
    public float width;
    public float height;
    private Object field56;

    public boolean hasFocusedSearch() { return featured.search.input.focused; }

    public GlobalConfigsScreen() {
        blocksGameInteraction = true;
        dim = new DimTransition();
        dim.animation = new com.adjustclient.core.rendering.animation.Animation(com.adjustclient.core.rendering.animation.Easing.LINEAR, 100L);
        popup = new PopupTransition(this, 400L);
        controls = new ArrayList<>();
        featured = new GlobalConfigList(this);
        lists = new GlobalConfigList[] {featured};
        width = 374.0F;
        height = 190.0F;
        refreshButton = new IconActionButton("j", () -> fetchListings());
        refreshButton.button.labelOffsetY = 0.5F;
        controls.add(refreshButton);
        publishButton = new IconActionButton("+", () -> openPublishDialog());
        publishButton.button.font = Fonts.TAHOMA_12;
        controls.add(publishButton);
        closeButton = new IconActionButton("x", () -> {
            popup.close();
        });
        closeButton.button.font = Fonts.TAHOMA_12;
        controls.add(closeButton);
    }

    @SuppressWarnings("unused")
    private void vendorOnListings(BackendResponse response) {
        fetching = false;
        if (response.status == ResponseStatus.OK) {
            REFRESH_TIMER.reset();
            if (!(response.body instanceof DataResponseBody)) throw new NullPointerException("global config data body");
            List<GlobalConfigEntry> result = new ArrayList<>();
            try {
                DataInputStream input = new DataInputStream(new ByteArrayInputStream(((DataResponseBody) response.body).value));
                while (input.available() > 0) result.add(GlobalConfigEntry.read(input));
            } catch (Exception error) {
                new NotificationBuilder("Global Configurations").addText("Failed to deserialize listing from server", 0xFFBEBCBD).show();
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                featured.entries.clear();
                featured.showCount = result != null;
                if (result != null) featured.addAll(result);
            });
        } else if (response.status == ResponseStatus.RATELIMITED) {
            new NotificationBuilder("Global Configurations")
                    .addText("You are requesting configurations too quickly, try again in a few seconds!", 0xFFBEBCBD).show();
        } else {
            new NotificationBuilder("Global Configurations").addText("Unable to request listing from the server.", 0xFFBEBCBD).show();
        }
    }

    public void fetchListings() {
        for (GlobalConfigList list : lists) {
            list.entries.clear();
            list.showCount = false;
        }
        fetching = false;
        REFRESH_TIMER.reset();
        new NotificationBuilder("Global Configurations")
                .addText("Global configurations are unavailable offline.", 0xFFBEBCBD)
                .show();
    }

    @SuppressWarnings("unused")
    private void vendorFetchListings() {
        for (GlobalConfigList list : lists) {
            list.entries.clear();
            list.showCount = false;
        }
        fetching = true;
        new GetGlobalConfigurationsRequest().toPacket().sendAsync(response -> vendorOnListings(response));
    }

    public void refreshIfStale() {
        if (REFRESH_TIMER.lastMs + 60000L <= System.currentTimeMillis() && !fetching) fetchListings();
    }

    @Override
    public void onAdded() {
        refreshIfStale();
    }

    @Override
    public void onClose() { dragOffset = null; }

    public void tick() {
        for (GlobalConfigList list : lists) {
            if (list.scroll < 0.0F) list.scroll += Math.min(-list.scroll, -list.scroll / 2.0F);
            float max = Math.max(0.0F, 35 * (list.entries.size() / 3) - list.screen.height / 2.0F - 6.0F);
            if (list.scroll > max) list.scroll -= (list.scroll - max) / 3.0F;
            list.search.input.field.updateCursorCounter();
        }
    }

    @Override
    public void updateScreen() {
        tick();
    }

    @Override
    public void render(Object queue, Vec2f mouse, ScaledResolution window) {
        draw(window, 252, mouse);
    }

    public boolean keyTyped(char character, int key) {
        for (GlobalConfigList list : lists) {
            if (key == Keyboard.KEY_ESCAPE && list.sortDropdown.expanded) {
                list.sortDropdown.expanded = false;
                return true;
            }
            if (key == Keyboard.KEY_ESCAPE && list.search.input.focused) {
                list.search.input.focused = false;
                return true;
            }
            if (list.search.input.focused) {
                list.search.input.field.setFocused(true);
                if (list.search.input.field.textboxKeyTyped(character, key)) return true;
                if (key == Keyboard.KEY_TAB) list.search.input.focused = !list.search.input.focused;
            } else if (key == Keyboard.KEY_TAB) list.search.input.focused = true;
        }
        if (key == Keyboard.KEY_ESCAPE) { popup.close(); return true; }
        return false;
    }

    public void mouseScrolled(Vec2f mouse, int button, boolean pressed, int wheel, float amount) {
        for (GlobalConfigList list : lists) {
            if (mouse.x >= position.x && mouse.y >= position.y + 23.0F && mouse.x < position.x + width && mouse.y < position.y + height)
                list.scroll += amount * 18.0F;
        }
    }

    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (dragOffset != null && button == 0 && !pressed) { dragOffset = null; return true; }
        for (IconActionButton control : controls) {
            if (button == 0 && pressed && control.button.isHovered(mouse.x, mouse.y)) {
                control.action.run();
                return true;
            }
        }
        for (GlobalConfigList list : lists) {
            if (button == 0 && pressed && list.sortDropdown.mouseClicked(mouse)) {
                list.search.input.focused = false;
                return true;
            }
            if (list.search != null) {
                if (pressed) {
                    if (button == 0 && mouse.x >= list.search.x && mouse.y >= list.search.y
                            && mouse.x < list.search.x + list.search.width && mouse.y < list.search.y + list.search.height) {
                        list.search.input.focused = true;
                        return true;
                    }
                    list.search.input.focused = false;
                }
                for (GlobalConfigEntry entry : list.entries) if (entry.mouseClicked(mouse, button, pressed)) return true;
            }
        }
        if (mouse.x >= position.x && mouse.y >= position.y
                && mouse.x < position.x + width && mouse.y < position.y + height) {
            if (button == 0 && pressed && mouse.y < position.y + 12.0F && mouse.x <= position.x + width - 21.0F) {
                dragOffset = new Vec2f(position.x - mouse.x, position.y - mouse.y);
                return true;
            }
        }
        return false;
    }

    public void drawControls(Vec2f mouse) {
        refreshButton.button.enabled = !fetching && REFRESH_TIMER.lastMs + 2100L <= System.currentTimeMillis();
        closeButton.x = position.x + width - 9.0F;
        publishButton.x = position.x + width - 18.0F;
        refreshButton.x = position.x + width - 27.0F;
        for (IconActionButton control : controls) {
            control.y = position.y + 2.5F;
            control.button.x = control.x;
            control.button.y = control.y;
            control.button.draw(mouse.x, mouse.y);
        }
    }

    public void drawBackground(ScaledResolution window, int color) {
        dim.animation.update(65.0F);
        int alpha = (int) dim.animation.value + 20;
        NvgDraw.fillRect(alpha << 24, 0.0F, 0.0F, window.getScaledWidth(), window.getScaledHeight());
        NvgDraw.fillRect(0xDE131313, position.x, position.y, position.x + width, position.y + height);
        RenderUtil.drawAccentGradient(position.x, position.y, width, 11.0F, 0.0F, 0.0F);
        String title = net.minecraft.util.ChatColor.BOLD + "Global Configurations";
        Fonts.TAHOMA_12.drawString(title, position.x + width * 0.5F - Fonts.TAHOMA_12.getStringWidth(title) * 0.5F,
                position.y + 4.5F, 0xCAE1E4E4);
    }

    public void drawLists(ScaledResolution window, int color, Vec2f mouse) {
        Vec2f listMouse = mouse.x >= position.x && mouse.y >= position.y && mouse.x < position.x + width && mouse.y < position.y + height
                ? mouse : new Vec2f(-9999.0F, -9999.0F);
        float y = position.y + 11.5F;
        for (GlobalConfigList list : lists) y += list.draw(listMouse, window, position.x, y) + 8.0F;
    }

    public void publish(DialogScreen dialog) {
        new NotificationBuilder("Global Configuration")
                .addText("Global configurations are unavailable offline.", 0xFFBEBCBD)
                .show();
    }

    @SuppressWarnings("unused")
    private void vendorPublish(DialogScreen dialog) {
        var nameInput = dialog.findTextInput(0);
        String name = nameInput == null ? null : nameInput.box.input.field.getText();
        var descriptionInput = dialog.findTextInput(1);
        String description = descriptionInput == null ? null : descriptionInput.box.input.field.getText();
        description = description.trim();
        int selected = dialog.selectedDropdownIndex();
        ServerType server = selected == -1 ? null : ServerType.values()[selected];
        byte[] data = Adjust.INSTANCE.fileSystem.emptyConfig();
        new CreateGlobalConfigurationRequest(name, description, server == null ? 0 : server.id, data)
                .toPacket().sendAsync(response -> vendorOnPublished(name, response));
    }

    @SuppressWarnings("unused")
    private void vendorOnPublished(String name, BackendResponse response) {
        if (response.status == ResponseStatus.OK) {
            if (response.body instanceof DataResponseBody) {
                try {
                    DataInputStream input = new DataInputStream(new ByteArrayInputStream(
                            ((DataResponseBody) response.body).value));
                    GlobalConfigEntry entry = GlobalConfigEntry.read(input);
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        if (entry != null) featured.addAll(Collections.singletonList(entry));
                    });
                } catch (Exception error) {
                    com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true,
                            "Failed to deserialize new global configuration '{}' data: {}", new Object[] {name, error});
                }
            } else {
                com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true,
                        "Failed to receive new global configuration data: {}", new Object[] {name});
            }
            new NotificationBuilder("Global Configuration").addText("Configuration ", 0xFFBEBCBD).append(name)
                    .addText(" was successfully published to Adjust!", 0xFFBEBCBD).show();
        } else if (response.status == ResponseStatus.RATELIMITED) {
            new NotificationBuilder("Global Configuration")
                    .addText("You are uploading configurations too quickly, try again in a few seconds.", 0xFFBEBCBD).show();
        } else if (response.body instanceof TextResponseBody message) {
            new NotificationBuilder("Global Configuration").addText(message.value, 0xFFBEBCBD).show();
        } else {
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.ERROR, true,
                    "Failed to publish configuration {}: {}", new Object[] {name, response});
            new NotificationBuilder("Global Configuration").addText("Failed to publish configuration ", 0xFFBEBCBD).append(name)
                    .addText(".", 0xFFBEBCBD).show();
        }
    }

    public void openPublishDialog() {
        DialogBuilder builder = new DialogBuilder("Publish global configuration");
        builder.message.append("You are creating a public configuration for everyone with Adjust to use. The configuration you are currently using will be uploaded.");
        builder.addTextInput("Name", "", 16, 128);
        builder.addTextInput("Description", "", 79, 128);
        String[] servers = Arrays.stream(ServerType.values()).map(server -> server.displayName)
                .toArray(String[]::new);
        builder.addDropdown("No specific server", Arrays.asList(servers), null);
        builder.addButton("Publish", dialog -> publish(dialog),
                dialog -> {
                    var input = dialog.findTextInput(0);
                    String name = input == null ? null : input.box.input.field.getText();
                    if (!(!name.isEmpty() && name.length() <= 16
                            && name.matches("^[a-zzA-Z0-9 !@#$%^&~*_\\-=+,./\"';:]*$")
                            && !name.startsWith(" ") && !name.endsWith(" "))) {
                        return false;
                    }
                    var descInput = dialog.findTextInput(1);
                    String description = descInput == null ? null : descInput.box.input.field.getText();
                    for (int i = 0; i < description.length(); i++) {
                        char c = description.charAt(i);
                        if (c < 32 || c >= 127) return true;
                    }
                    return true;
                });
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.bottomInset = 14;
        builder.build(200.0F);
    }

    public void draw(ScaledResolution window, int color, Vec2f mouse) {
        if (popup.getProgress()) return;
        if (position == null) position = new Vec2f(window.getScaledWidth() / 2.0F - width / 2.0F, window.getScaledHeight() / 2.0F - height / 2.0F);
        if (dragOffset != null) position = new Vec2f(mouse.x + dragOffset.x, mouse.y + dragOffset.y);
        position.x = Math.min(window.getScaledWidth() - width, Math.max(0.0F, position.x));
        position.y = Math.min(window.getScaledHeight() - height, Math.max(0.0F, position.y));
        com.adjustclient.core.rendering.font.image.NvgDraw.setGlobalAlpha(popup.animation.value);
        drawBackground(window, 252);
        drawControls(mouse);
        drawLists(window, 252, mouse);
        com.adjustclient.core.rendering.font.image.NvgDraw.setGlobalAlpha(1.0F);
    }
}
