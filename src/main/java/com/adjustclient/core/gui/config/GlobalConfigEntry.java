package com.adjustclient.core.gui.config;

import com.adjustclient.Adjust;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.core.gui.dialog.*;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.Timer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;
import java.io.*;
import java.util.*;
import com.adjustclient.managers.ScreenStack;

public final class GlobalConfigEntry {
    private static final Timer UPDATE_TIMER = new Timer(0L);
    public String name;
    public String description;
    public ClientUser author;
    public List<IChatComponent> descriptionLines;
    public ServerType server;
    public Animation hoverAnimation;
    public com.adjustclient.utilities.Timer timer;
    public AdjustButton[] buttons;
    public ConfigIconButton downvoteButton;
    public ConfigIconButton upvoteButton;
    public ConfigIconButton[] voteButtons;
    public Boolean vote;
    public GlobalConfigsScreen screen;
    public AdjustButton downloadButton;
    public AdjustButton updateButton;
    public AdjustButton editButton;
    public Animation appearAnimation;
    public long createdAt;
    public long updatedAt;
    public int id;
    public int upvotes;
    public int downvotes;
    public int downloads;
    public boolean requestPending;
    public boolean updating;
    public boolean downloading;
    public boolean editing;
    public boolean featured;
    public boolean deleting;
    public boolean shiftDown;
    private Object field112;

    public GlobalConfigEntry(int id, String name, ClientUser author, String description, long createdAt, int upvotes) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.createdAt = createdAt;
        this.upvotes = upvotes;
        setDescription(description);
        timer = new com.adjustclient.utilities.Timer();
        hoverAnimation = new Animation(com.adjustclient.core.rendering.animation.Easing.LINEAR, 100L);
        downvoteButton = new ConfigIconButton(this, false, new java.awt.Color(154, 49, 66));
        upvoteButton = new ConfigIconButton(this, true, new java.awt.Color(128, 192, 68));
        voteButtons = new ConfigIconButton[] {downvoteButton, upvoteButton};
        rebuildButtons();
    }

    public static GlobalConfigEntry read(DataInputStream input) throws IOException {
        int id = ByteUtil.readVarInt(input);
        String authorName = input.readUTF();
        ClientUser author = ((com.adjustclient.core.settings.property.UserManager) Adjust.INSTANCE.field40).get(authorName);
        if (author == null) {
            author = new ClientUser(authorName);
            ((com.adjustclient.core.settings.property.UserManager) Adjust.INSTANCE.field40).add(author);
        }
        int downloads = ByteUtil.readVarInt(input);
        int upvotes = ByteUtil.readVarInt(input);
        int downvotes = ByteUtil.readVarInt(input);
        long updated = ByteUtil.readVarLong(input) + 1728500000000L;
        long created = ByteUtil.readVarLong(input) + 1728500000000L;
        int serverId = input.readInt();
        ServerType server = serverId == 0 ? null : (ServerType) ServerType.byId.get(serverId);
        String name = input.readUTF();
        String description = input.readUTF();
        byte vote = (byte) input.readUnsignedByte();
        GlobalConfigEntry entry = new GlobalConfigEntry(id, name, author, description, created, upvotes);
        entry.downvotes = downvotes;
        entry.server = server;
        entry.vote = vote == 0 ? Boolean.FALSE : vote == 1 ? Boolean.TRUE : null;
        entry.downloads = downloads;
        entry.updatedAt = updated;
        return entry;
    }

    @SuppressWarnings("unused")
    private void vendorOnUpdated(com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        UPDATE_TIMER.lastMs = System.currentTimeMillis();
        updating = false;
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
            createdAt = System.currentTimeMillis();
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Successfully updated configuration ", 0xFFBEBCBD).append(name)
                    .addText(" with your current settings!", 0xFFBEBCBD).show();
        } else if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.RATELIMITED) {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("You are updating configurations too quickly, please wait a few seconds!", 0xFFBEBCBD).show();
        } else {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Failed to update configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.WARN, true,
                    "Failed to receive configuration {}: {}", new Object[] {name, response});
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnDescriptionEdited(String description, com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        editing = false;
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Successfully updated description of configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
            setDescription(description);
        } else if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.RATELIMITED) {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("You are editing configurations too quickly, please wait a few seconds!", 0xFFBEBCBD).show();
        } else {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Failed to edit configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.WARN, true,
                    "Failed to edit configuration {}: {}", new Object[] {name, response});
        }
    }

    public void setVote(Boolean vote) {
        if (this.vote != null) {
            if (this.vote) --upvotes;
            else --downvotes;
        }
        this.vote = vote;
        if (vote != null) {
            if (vote) ++upvotes;
            else ++downvotes;
        }
    }

    public boolean canDownload() {
        return Adjust.INSTANCE.screenStack.configurations.configurations.size() < 32
                && timer.lastMs + 5000L <= System.currentTimeMillis() && !downloading;
    }

    public void rebuildButtons() {
        com.adjustclient.core.gui.dialog.AlphaModifier alpha = value -> (int) (value * hoverAnimation.value);
        if (!author.useClientColor || net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
            downloadButton = new AdjustButton(0, 0.0F, 0.0F, 40, 10, "Download", alpha);
            buttons = new AdjustButton[] {downloadButton};
        } else {
            updateButton = new AdjustButton(2, 0.0F, 0.0F, 30, 10, "Update", alpha);
            editButton = new AdjustButton(3, 0.0F, 0.0F, 21, 10, "Edit", alpha);
            buttons = new AdjustButton[] {updateButton, editButton,
                    new AdjustButton(1, 0.0F, 0.0F, 32, 10, net.minecraft.util.ChatColor.RED + "Remove", alpha)};
        }
        for (AdjustButton button : buttons) button.base = 62;
    }

    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (button != 0 || !pressed) return false;
        for (ConfigIconButton voteButton : voteButtons) if (voteButton.mouseClicked(mouse, 0, pressed)) return true;
        for (AdjustButton action : buttons) {
            if (!action.isHovered(mouse.x, mouse.y)) continue;
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Global configurations are unavailable offline.", 0xFFBEBCBD)
                    .show();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean vendorMouseClicked(Vec2f mouse, int button, boolean pressed) {
        if (button != 0 || !pressed) return false;
        for (ConfigIconButton voteButton : voteButtons) if (voteButton.mouseClicked(mouse, 0, pressed)) return true;
        for (AdjustButton action : buttons) {
            if (!action.isHovered(mouse.x, mouse.y)) continue;
            switch (action.id) {
                case 0:
                    if (canDownload()) {
                        downloading = true;
                        timer.lastMs = System.currentTimeMillis();
                        new com.adjustclient.utilities.backend.api.request.config.DownloadConfigurationRequest(this).toPacket()
                                .sendAsync(response -> vendorOnDownloaded(response));
                    }
                    break;
                case 1:
                    if (!deleting) {
                        DialogBuilder builder = new DialogBuilder("Delete global configuration '" + name + "'?");
                        builder.message.append("Are you sure you want to delete ");
                        builder.message.italic = true;
                        builder.message.append(name).append("? This action cannot be undone.");
                        builder.addButton("Delete", dialog -> {
                            deleting = true;
                            new com.adjustclient.utilities.backend.api.request.config.GetConfigurationRequest(this).toPacket()
                                    .sendAsync(response -> vendorOnDeleted(response));
                        }, DialogBuilder.ALWAYS_ENABLED);
                        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
                        builder.build();
                    }
                    break;
                case 2:
                    if (!updating && UPDATE_TIMER.lastMs + 2000L <= System.currentTimeMillis()) {
                        byte[] data = Adjust.INSTANCE.fileSystem.emptyConfig();
                        if (data != null) {
                            updating = true;
                            UPDATE_TIMER.lastMs = System.currentTimeMillis();
                            new com.adjustclient.utilities.backend.api.request.config.UpdateConfigurationRequest(this, data).toPacket()
                                    .sendAsync(response -> vendorOnUpdated(response));
                        }
                    }
                    break;
                case 3:
                    if (!editing) {
                        DialogBuilder builder = new DialogBuilder("Edit description of global configuration '" + name + "'");
                        builder.message.append("Update the description of your published configuration");
                        builder.addTextInput("Description", "", 79, 128);
                        builder.addButton("Update", dialog -> {
                            if (!editing) {
                                editing = true;
                                var input = dialog.findTextInput(0);
                                String text = input == null ? null : input.box.input.field.getText();
                                new com.adjustclient.utilities.backend.api.request.config.EditConfigurationRequest(this, text).toPacket()
                                        .sendAsync(response -> vendorOnDescriptionEdited(text, response));
                            }
                        }, dialog -> {
                            var input = dialog.findTextInput(0);
                                String text = input == null ? null : input.box.input.field.getText();
                            for (int i = 0; i < text.length(); i++) {
                                if (text.charAt(i) < 32 || text.charAt(i) >= 127) return false;
                            }
                            return true;
                        });
                        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
                        DialogScreen dialog = builder.build();
                        dialog.setInputText(0, description);
                    }
                    break;
                default: break;
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    private void vendorOnDeleted(com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
            screen.featured.entries.remove(this);
        });
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Successfully removed configuration ", 0xFFBEBCBD).append(name)
                    .addText(" from public listing!", 0xFFBEBCBD).show();
        } else {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Failed to remove configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.WARN, true,
                    "Failed to remove configuration {}: {}", new Object[] {name, response});
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnDownloaded(com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        downloading = false;
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
            if (!(response.body instanceof com.adjustclient.utilities.backend.api.response.DataResponseBody))
                throw new NullPointerException("gc download data body");
            byte[] data = ((com.adjustclient.utilities.backend.api.response.DataResponseBody) response.body)
                    .value;
            new com.adjustclient.utilities.backend.api.request.config.CreateConfigurationRequest(name, data).toPacket() /* /configuration/createConfiguration */
                    .sendAsync(saved -> vendorOnSaved(saved));
        } else if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.RATELIMITED) {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("You are downloading configurations too quickly, wait a few seconds before trying again!", 0xFFBEBCBD).show();
        } else {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Failed to receive configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.WARN, true,
                    "Failed to receive configuration {}: {}", new Object[] {name, response});
        }
    }

    @SuppressWarnings("unused")
    private void vendorOnSaved(com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Successfully downloaded configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
        } else {
            new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Global Configurations")
                    .addText("Failed to save downloaded configuration ", 0xFFBEBCBD).append(name).addText("!", 0xFFBEBCBD).show();
            com.adjustclient.core.logging.Logger.log(com.adjustclient.core.logging.LogLevel.WARN, true,
                    "Failed to receive configuration {}: {}", new Object[] {name, response});
        }
    }

    public void drawDescription(ScaledResolution window, int background, float x, float y) {
        int alpha = Math.min(185, 255 - (int) (255.0F * hoverAnimation.value));
        int color = alpha << 24 | 0xE1E4E4;
        com.adjustclient.core.rendering.RenderUtil.pushScissor(window, 1, x, y, 120.0F, 34.5F);
        float lineY = y + 17.5F;
        for (IChatComponent line : descriptionLines) {
            Fonts.TAHOMA_11.drawString(line.getFormattedText(), x + 2.0F, lineY, color);
            if (lineY + 5.0F > y + 35.0F) {
                com.adjustclient.core.rendering.RenderUtil.popScissor();
                com.adjustclient.core.rendering.font.image.NvgDraw.fillGradientRect(0, background,
                        x + 0.5F, y + 27.0F, x + 119.5F, y + 34.5F);
                return;
            }
            lineY += 5.0F;
        }
        com.adjustclient.core.rendering.RenderUtil.popScissor();
    }

    public void draw(Vec2f mouse, ScaledResolution window, float x, float y) {
        boolean shift = net.minecraft.client.gui.GuiScreen.isShiftKeyDown();
        if (shiftDown != shift && author.useClientColor) {
            shiftDown = shift;
            rebuildButtons();
        }
        if (appearAnimation != null) appearAnimation.update(1.0F);
        hoverAnimation.update(mouse.x >= x && mouse.y >= y + 14.5F && mouse.x < x + 120.0F && mouse.y < y + 35.0F ? 1.0F : 0.0F);
        com.adjustclient.core.rendering.font.image.NvgDraw.fillRect(0x55272627, x, y, x + 120.0F, y + 35.0F);
        NvgContext.pushEvenScale();
        org.joml.Vector4f bounds = NvgContext.toPixels(x, y, x + 120.0F, y + 35.0F);
        float border = (NvgContext.scaleFactor == null ? 1.0F : (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor) : NvgContext.scaleFactor.intValue())) * 0.5F;
        com.adjustclient.core.rendering.font.image.NvgDraw.strokeRect(false, 0x55242424, bounds.x, bounds.y, bounds.z, bounds.w, border);
        org.joml.Vector4f separator = NvgContext.toPixels(x, y + 14.5F, x + 120.0F, y + 15.0F);
        com.adjustclient.core.rendering.font.image.NvgDraw.fillRect(0x55242424, separator.x + border, separator.y, separator.z - border, separator.y + border);
        NvgContext.popEvenScale();
        boolean hasIcon = server != null && server.icon != null;
        Fonts.TAHOMA_12.drawString(name, x + (hasIcon ? 11.0F : 2.0F), y + 3.0F, 0xE4E1E4E4);
        Fonts.TAHOMA_10.drawString("by " + (featured ? net.minecraft.util.ChatColor.RED : net.minecraft.util.ChatColor.GRAY)
                + author.name, x + (hasIcon ? 11.0F : 2.0F), y + 10.0F, 0xE47F7F7F);
        String updated = "updated " + net.minecraft.util.ChatColor.GRAY + TimeUtil.timeAgo(createdAt, " ago");
        Fonts.TAHOMA_10.drawString(updated, x + 118.0F - Fonts.TAHOMA_10.getStringWidth(updated), y + 10.0F, 0xE47F7F7F);
        downvoteButton.x = x + 104.5F;
        downvoteButton.y = y + 3.0F;
        upvoteButton.x = x + 112.0F;
        upvoteButton.y = y + 3.5F;
        for (ConfigIconButton voteButton : voteButtons) voteButton.draw(mouse);
        String votes = Integer.toString(upvotes - downvotes);
        float ratio = Math.max(0.0F, Math.min(1.0F, (upvotes - downvotes + 100.0F) / 200.0F));
        int shade = (int) (ratio * 40.0F) + 175;
        Fonts.TAHOMA_10.drawString(votes, x + 103.5F - Fonts.TAHOMA_10.getStringWidth(votes), y + 4.0F,
                new java.awt.Color(shade, shade, shade, 230).getRGB());
        drawDescription(window, 0x55272627, x, y);
        if (hoverAnimation.value == 0.0F) {
            if (hasIcon) com.adjustclient.core.rendering.font.image.ImageLoader.getImage(new net.minecraft.util.ResourceLocation("adjust", server.icon), 0)
                    .draw(x + 1.5F, y + 3.5F, 8.0F, 8.0F);
        } else {
            int totalWidth = Arrays.stream(buttons).mapToInt(action -> action.getButtonWidth() + 4).sum();
            float buttonX = x + 60.0F - totalWidth / 2.0F;
            if (downloadButton != null) downloadButton.enabled = canDownload();
            if (updateButton != null) updateButton.enabled = !updating && UPDATE_TIMER.lastMs + 2000L <= System.currentTimeMillis();
            for (AdjustButton action : buttons) {
                action.x = buttonX;
                action.y = y + 20.0F;
                action.draw(mouse.x, mouse.y);
                buttonX += action.getButtonWidth() + 4.0F;
            }
        }
    }

    public void setDescription(String description) {
        this.description = description;
        descriptionLines = GuiUtilRenderComponents.splitText(new ChatComponentText(description), 114, Fonts.TAHOMA_11, false, false);
    }
}
