package com.adjustclient.ui.clickgui.setting;

import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.image.NvgDraw;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Mouse;
import com.adjustclient.Adjust;
import com.adjustclient.backend.api.BackendRequest;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.request.client.SendCustomPayloadRequest;
import com.adjustclient.backend.api.response.EmptyResponseBody;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.backend.transport.ByteCodec;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefensePlacement;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefensePreset;
import com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseSetting;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.service.party.ui.AdjustContextMenu;
import com.adjustclient.service.party.ui.ContextMenuAction;
import com.adjustclient.service.party.ui.ContextMenuProvider;
import com.adjustclient.service.payload.BedDefensePresetPayload;
import com.adjustclient.ui.clickgui.ClickGuiScreen;
import com.adjustclient.ui.dialog.AdjustButton;
import com.adjustclient.ui.dialog.AlphaModifier;
import com.adjustclient.ui.dialog.DialogBuilder;
import com.adjustclient.ui.dialog.DialogScreen;
import com.adjustclient.ui.hud.notification.Notification;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.ui.screen.AdjustComponent;

public final class BedDefenseSettingComponent extends SettingComponent<BedDefenseSetting> implements ContextMenuProvider {

    boolean dragging;
    AdjustButton recordButton;
    AdjustButton menuButton;
    Map<UUID, Animation> rowAnimations = new HashMap<>();
    Animation slideAnimation;
    BedDefensePreset hoveredPreset;
    UUID pressedId;
    AdjustContextMenu menu;
    BedDefenseEntry menuEntry;
    float dragStartY;

    public BedDefenseSettingComponent(BedDefenseSetting setting) {
        super(setting);
        this.modifiers.remove(this.hover);
        this.recordButton = new AdjustButton(0, "+", 6, 6, 26, IDENTITY_ALPHA_2, Fonts.TAHOMA_12);
        this.menuButton = new AdjustButton(0, "...", 8, 6, 26, IDENTITY_ALPHA_1, Fonts.TAHOMA_8);
        this.slideAnimation = new Animation(Easing.EASE_OUT_EXPO, 160L);
        this.modifiers.add(new BedBlockListPanel(this));
        setting.addListener(BedDefenseSettingComponent::markGuiDirty);
    }

    static final AlphaModifier IDENTITY_ALPHA_1 = alpha -> alpha;
    static final AlphaModifier IDENTITY_ALPHA_2 = alpha -> alpha;

    private List<BedDefensePreset> presets() {
        return (List<BedDefensePreset>) this.setting.getValue();
    }

    // ------------------------------------------------------------------------------------------------------------------
    // layout helpers

    @Override
    public float getHeight() {
        return (float) this.presets().size() * 10.0F + 10.0F;
    }

    int indexOfPreset(BedDefensePreset wanted) {
        List<BedDefensePreset> list = this.presets();
        for (int i = 0; i < list.size(); i++) {
            UUID id = list.get(i).id;
            if (id != null && wanted.id != null && id.equals(wanted.id)) {
                return i + 1;
            }
        }
        return 0;
    }

    BedBlockPos sizeOf(BedDefensePreset preset) {
        int minX = 0, minY = 0, minZ = 0, maxX = 0, maxY = 0, maxZ = 1;
        for (BedDefensePlacement p : preset.placements) {
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
            minZ = Math.min(minZ, p.z); maxZ = Math.max(maxZ, p.z);
        }
        return new BedBlockPos(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
    }

    float rowOffset(BedDefensePreset preset, int index) {
        float target = (float) index * 10.0F;
        Animation animation = this.rowAnimations.get(preset.id);
        if (animation == null) {
            animation = new Animation(Easing.EASE_OUT_EXPO, 170L);
            animation.value = target;
            animation.restart();
            this.rowAnimations.put(preset.id, animation);
            return this.y + animation.value;
        }
        return animation.update(target) + this.y;
    }

    BedDefensePreset presetAt(float y) {
        BedDefensePreset best = null;
        float bestDistance = Float.MAX_VALUE; // 3.4028235e38
        List<BedDefensePreset> list = this.presets();
        for (int i = 0; i < list.size(); i++) {
            BedDefensePreset preset = list.get(i);
            Animation animation = this.rowAnimations.get(preset.id);
            float top = (animation != null ? animation.value : (float) i * 10.0F) + this.y;
            if (top > y) {
                continue;
            }
            if (y < top + 10.0F) {
                float distance = Math.abs((y - top) - 5.0F);
                if (bestDistance > distance) {
                    bestDistance = distance;
                    best = preset;
                }
            }
        }
        return best;
    }

    boolean isMenuFor(BedDefensePreset preset) {
        return this.menu != null && this.menu.source != null && preset.id != null && this.menu.source.equals(preset.id);
    }

    void positionMenu() {
        if (this.menu == null || this.menu.source == null) {
            return;
        }
        List<BedDefensePreset> list = this.presets();
        for (int i = 0; i < list.size(); i++) {
            BedDefensePreset preset = list.get(i);
            if (preset.id.equals(this.menu.source)) {
                float x = this.bounds.x + this.bounds.width;
                Animation animation = this.rowAnimations.get(preset.id);
                float top = (animation != null ? animation.value : (float) i * 10.0F) + this.y;
                this.menu.x = x - 50.0F;
                this.menu.y = top + 10.0F;
                return;
            }
        }
        // not found: close the menu
        this.closeMenu(null);
    }

    // ------------------------------------------------------------------------------------------------------------------
    // menu / drag state

    @Override
    public void onScreenClosed() {
        this.resetState();
    }

    public void resetState() {
        this.menu = null;
        this.menuEntry = null;
        this.rowAnimations.clear();
        if (this.slideAnimation != null) {
            this.slideAnimation.value = 0.0F;
            this.slideAnimation.restart();
        }
        this.pressedId = null;
        this.dragging = false;
    }

    public void closeMenu(AdjustContextMenu next) {
        ClickGuiScreen gui = ClickGuiScreen.instance();
        if (this.menuEntry != null) {
            gui.removeOverlay(this.menuEntry);
        }
        this.menu = next;
        this.menuEntry = null;
        if (next != null) {
            this.menuEntry = new BedDefenseEntry(this, new Animation(Easing.LINEAR, 150L));
            gui.addOverlay(this.menuEntry);
        }
    }

    void openMenu(BedDefensePreset preset) {
        Runnable send = () -> this.openSendDialog(preset);
        Runnable rename = () -> this.openRenameDialog(preset);
        Runnable delete = () -> this.openDeleteDialog(preset);
        AdjustContextMenu popup = new AdjustContextMenu(this, preset.id, 0.0F, 0.0F);
        popup.entries.addAll(List.of(
                new ContextMenuAction(new TextComponent[]{new TextComponent("Send to user", new Color(210, 210, 210, 255).getRGB())}, send),
                new ContextMenuAction(new TextComponent[]{new TextComponent("Rename", new Color(210, 210, 210, 255).getRGB())}, rename),
                new ContextMenuAction(new TextComponent[]{new TextComponent("Delete", new Color(233, 80, 80, 255).getRGB())}, delete)));
        this.closeMenu(popup);
        this.positionMenu();
    }

    // ------------------------------------------------------------------------------------------------------------------
    // dialogs and actions

    void startRecording() {
        Minecraft mc = Minecraft.getMinecraft();
        com.adjustclient.module.impl.combat.builder.feature.beddefense.BedAnchor found =
                (mc.thePlayer != null && mc.theWorld != null)
                        ? com.adjustclient.module.impl.combat.builder.feature.beddefense.BedAnchor.findNearbyBed(mc.thePlayer.getPositionVector())
                        : null;
        if (found == null) {
            NotificationBuilder notification = new NotificationBuilder("Bed Defense");
            notification.lifetimeSteps = 46;
            notification.addText("You need to be close to a bed.", 0xFFBEBCBD);
            HUDModule.INSTANCE_ALT.notifications.add(notification.build());
            return;
        }
        DialogBuilder builder = new DialogBuilder("Create bed defense preset");
        builder.message.append("Name the preset you want to record.");
        builder.addTextInput("Name", "", 32, 128); // addTextField
        Consumer<DialogScreen> record = dialog -> {
            String name = dialog.findTextInput(0).box.input.field.getText();
            Minecraft.getMinecraft().displayGuiScreen(null);
            this.setting.requestRecording(name);
        };
        Function<DialogScreen, Boolean> valid = dialog -> {
            String name = dialog.findTextInput(0).box.input.field.getText();
            if (name.isBlank() || name.length() > 32) return false;
            String trimmed = name.trim();
            for (BedDefensePreset preset : presets()) {
                if (preset.name.equalsIgnoreCase(trimmed)) return false;
            }
            return true;
        };
        builder.addButton("Record", record, valid); // addButton
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build(133.0F); // show(width)
    }

    void openSendDialog(BedDefensePreset preset) {
        if ((preset.builtinId != null)) {
            return;
        }
        DialogBuilder builder = new DialogBuilder("Send preset '" + preset.name + "'");
        builder.message.append("Enter the Adjust user who should receive this preset.");
        builder.addTextInput("Username", "", 16, 128);
        Consumer<DialogScreen> send = dialog -> this.sendAction(preset, dialog);
        Function<DialogScreen, Boolean> valid = dialog -> this.usernameValid(dialog);
        builder.addButton("Send", send, valid);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build(133.0F);
    }

    private void sendAction(BedDefensePreset preset, DialogScreen dialog) {
        String name = dialog.findTextInput(0).box.input.field.getText();
        if (name.length() < 3 || !name.matches("^[a-zA-Z0-9]*$") || name.length() >= 17) {
            this.sendToUser(preset, name, null);
            return;
        }
        if (Adjust.INSTANCE == null || !(Adjust.INSTANCE.field40 instanceof com.adjustclient.property.UserManager userManager)) {
            this.sendToUser(preset, name, null);
            return;
        }
        userManager.lookup(name, found -> this.sendToUser(preset, name, found));
    }

    private Boolean usernameValid(DialogScreen dialog) {
        String name = dialog.findTextInput(0).box.input.field.getText();
        return name.length() >= 3
                && java.util.regex.Pattern.matches("^[a-zA-Z0-9]*$", name)
                && name.length() < 17;
    }

    void sendToUser(BedDefensePreset preset, String name, com.adjustclient.user.ClientUser target) {
        if (target != null && target.useClientColor) {
            new NotificationBuilder("Bed Defense").addText("You cannot send a preset to yourself.", 0xFFBEBCBD).show();
            return;
        }
        if (preset == null) {
            throw new NullPointerException();
        }
        if ((preset.builtinId != null)) {
            throw new IllegalArgumentException("Default presets cannot be sent");
        }
        new NotificationBuilder("Bed Defense")
                .addText("Preset sharing is unavailable offline.", 0xFFBEBCBD)
                .show();
    }

    @SuppressWarnings("unused")
    private void vendorSendToUser(BedDefensePreset preset, String name, com.adjustclient.user.ClientUser target) {
        if (target == null) {
            NotificationBuilder n = new NotificationBuilder("Bed Defense");
            n.addText("No user by the name ", 0xFFBEBCBD).append(name).addText(" exists.", 0xFFBEBCBD).show();
            return;
        }
        if (target.useClientColor) {
            new NotificationBuilder("Bed Defense").addText("You cannot send a preset to yourself.", 0xFFBEBCBD).show();
            return;
        }
        if (preset == null) {
            throw new NullPointerException();
        }
        if ((preset.builtinId != null)) {
            throw new IllegalArgumentException("Default presets cannot be sent");
        }
        int packetId = BedDefensePresetPayload.PACKET_ID; // class-to-id registry, matching PayloadService.handlers
        PacketBuffer buffer = new PacketBuffer(io.netty.buffer.Unpooled.buffer(256, Integer.MAX_VALUE));
        BedDefensePresetPayload payload = new BedDefensePresetPayload();
        payload.preset = preset;
        payload.write(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        SendCustomPayloadRequest request = new SendCustomPayloadRequest(target, packetId, bytes);
        PendingRequest call = request.toPacket();
        call.sendAsync(response -> this.vendorOnSent(preset, target, response));
    }

    @SuppressWarnings("unused")
    private void vendorOnSent(BedDefensePreset preset, com.adjustclient.user.ClientUser target,
                              com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status == ResponseStatus.OK) {
            new NotificationBuilder("Bed Defense").addText("Sent ", 0xFFBEBCBD)
                    .append(preset.name).addText(" to ", 0xFFBEBCBD)
                    .append(target.name).addText(".", 0xFFBEBCBD).show();
            return;
        }
        int lifetime = Math.max(22, (int) (preset.name.length() * 1.3200001F) + 27);
        NotificationBuilder failure = new NotificationBuilder("Bed Defense");
        failure.lifetimeSteps = lifetime;
        Notification notification = failure.addText("Failed to send ", 0xFFBEBCBD)
                .append(preset.name).addText(".", 0xFFBEBCBD).build();
        HUDModule.INSTANCE_ALT.notifications.add(notification);
    }

    void openRenameDialog(BedDefensePreset preset) {
        if (preset == null || (preset.builtinId != null)) {
            return;
        }
        DialogBuilder builder = new DialogBuilder("Rename preset '" + preset.name + "'");
        builder.message.append("Enter a new name for this preset.");
        builder.addTextInput("Name", preset.name, 32, 128);
        Consumer<DialogScreen> rename = dialog -> {
            String text = dialog.findTextInput(0).box.input.field.getText().trim();
            List<BedDefensePreset> list = new ArrayList<>(this.presets());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id.equals(preset.id)) {
                    list.set(i, new BedDefensePreset(preset.id, preset.builtinId, text, preset.enabled, preset.placements));
                    this.setting.setValue(list);
                    return;
                }
            }
        };
        Function<DialogScreen, Boolean> valid = dialog -> this.setting.isNameAvailable(dialog.findTextInput(0).box.input.field.getText(), preset.id);
        builder.addButton("Rename", rename, valid);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build(133.0F);
    }

    void openDeleteDialog(BedDefensePreset preset) {
        if ((preset.builtinId != null)) {
            return;
        }
        DialogBuilder builder = new DialogBuilder("Delete preset '" + preset.name + "'?");
        builder.message.append("This custom preset will be removed from the current configuration.");
        Consumer<DialogScreen> delete = dialog -> {
            List<BedDefensePreset> list = new ArrayList<>(this.presets());
            list.removeIf(p -> p.id.equals(preset.id));
            this.setting.setValue(List.copyOf(list));
        };
        builder.addButton("Delete", delete, DialogBuilder.ALWAYS_ENABLED);
        builder.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
        builder.build(133.0F);
    }

    static void markGuiDirty() {
        ClickGuiScreen gui = ClickGuiScreen.instance();
        gui.markDirty();
    }

    // ------------------------------------------------------------------------------------------------------------------
    // reordering

    void finishDrag(float y) {
        if (!this.dragging) {
            if (Math.abs(y - this.dragStartY) < 2.5F) {
                return;
            }
            this.dragging = true;
            this.closeMenu(null);
        }
        List<BedDefensePreset> list = new ArrayList<>(this.presets());
        int newIndex = Math.max(0, Math.min(list.size() - 1, (int) ((y - this.y) / 10.0F)));
        int oldIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (this.pressedId != null && this.pressedId.equals(list.get(i).id)) {
                oldIndex = i;
                break;
            }
        }
        if (oldIndex == -1 || oldIndex == newIndex) {
            return;
        }
        BedDefensePreset moved = list.remove(oldIndex);
        list.add(newIndex, moved);
        this.setting.setValue(List.copyOf(list));
    }

    List<BedBlockEntry> blockCounts(BedDefensePreset preset) {
        Map<BedBlockKey, BedBlockEntry> counts = new LinkedHashMap<>();
        for (BedDefensePlacement p : preset.placements) {
            BedBlockKey key = new BedBlockKey(p.type,
                    p.type == com.adjustclient.module.impl.combat.builder.feature.beddefense.BedDefenseBlockType.OTHER ? p.blockId : -1);
            BedBlockEntry entry = counts.get(key);
            counts.put(key, entry == null ? new BedBlockEntry(p.type, p.blockId, 1) : new BedBlockEntry(p.type, p.blockId, entry.count() + 1));
        }
        return new ArrayList<>(counts.values());
    }

    // ------------------------------------------------------------------------------------------------------------------
    // draw / input

    @Override
    public void renderContent(com.adjustclient.event.Render2DEvent queue, com.adjustclient.util.Vec2f mouse) {
        AdjustComponent bounds = this.bounds;
        float x = bounds.x;
        float right = bounds.x + bounds.width;
        if (this.pressedId != null) {
            if (!Mouse.isButtonDown(0)) {
                this.pressedId = null;
                this.dragging = false;
            } else {
                this.finishDrag(mouse.y);
            }
        }
        this.hoveredPreset = null;
        float animValue = this.slideAnimation.update(this.pressedId != null ? 1.0F : 0.0F);
        float slide = animValue * 7.0F;
        List<BedDefensePreset> list = this.presets();
        float mouseX = mouse.x;
        float mouseY = mouse.y;
        for (int i = 0; i < list.size(); i++) {
            BedDefensePreset preset = list.get(i);
            float rowTop = this.rowOffset(preset, i);
            boolean pressed = this.pressedId != null && this.pressedId.equals(preset.id);
            boolean over = mouseX >= x && mouseX <= right && mouseY >= rowTop && mouseY < rowTop + 10.0F;
            if (over) {
                this.hoveredPreset = preset;
            }
            int backgroundAlpha = pressed ? (this.dragging ? 90 : 38) : 38;
            NvgDraw.fillRect((backgroundAlpha << 24) | 0x353535, x, rowTop, right, rowTop + 10.0F);
            NvgDraw.fillRect(0x620F0F13, x + 2.0F + slide, rowTop + 2.25F, x + 7.5F + slide, rowTop + 7.75F);
            int box = preset.enabled ? new Color(170, 205, 190, 230).getRGB() : new Color(65, 65, 69, 210).getRGB();
            NvgDraw.fillRect(box, x + 2.5F + slide, rowTop + 2.75F, x + 7.0F + slide, rowTop + 7.25F);
            boolean showMenuButton;
            float rightPad;
            if (animValue <= 0.01F) {
                if (preset.builtinId != null) {
                    showMenuButton = false;
                    rightPad = 1.0F;
                } else if (over || this.isMenuFor(preset)) {
                    showMenuButton = true;
                    rightPad = 10.0F;
                } else {
                    showMenuButton = false;
                    rightPad = 1.0F;
                }
            } else {
                showMenuButton = false;
                rightPad = 1.0F;
            }
            float maxTextWidth = Math.max(0.0F, bounds.width - rightPad - slide - 10.0F);
            String fitted = Fonts.TAHOMA_10.trimStringToWidth(preset.name, maxTextWidth, false);
            String displayName;
            if (fitted.equals(preset.name)) {
                displayName = fitted;
            } else {
                String trimmed = fitted.trim();
                displayName = trimmed.isEmpty() ? "..." : trimmed + "...";
            }
            int nameColor = preset.enabled ? 0xEBD7D7D7 : 0xD2828286;
            Fonts.TAHOMA_10.drawString(displayName, x + 9.0F + slide, rowTop + 3.5F, nameColor);
            if (animValue > 0.0F) {
                int rankAlpha = Math.max(0, Math.min(255, (int) ((pressed ? 245 : 220) * animValue)));
                int rankColor = pressed
                        ? new Color(170, 205, 190, rankAlpha).getRGB()
                        : new Color(155, 155, 160, rankAlpha).getRGB();
                Fonts.TAHOMA_10.drawString((i + 1) + ".", x + 2.0F, rowTop + 3.5F, rankColor);
            } else if (showMenuButton) {
                this.menuButton.x = right - (float) this.menuButton.getButtonWidth() - 1.0F;
                this.menuButton.y = rowTop + 2.0F;
                this.menuButton.draw(mouseX, mouseY);
            } else if (over && mouseX > x + 9.0F) {
                RenderUtil.cursor = 221188;
            }
        }
        this.recordButton.x = right - (float) this.recordButton.getButtonWidth() - 2.0F;
        this.recordButton.y = (float) list.size() * 10.0F + this.y + 2.0F;
        this.recordButton.draw(mouseX, mouseY);
    }

    @Override
    public boolean onMousePressed(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (button != 0) {
            return false;
        }
        if (!pressed) {
            if (this.pressedId != null) {
                this.pressedId = null;
                this.dragging = false;
                return true;
            }
            return false;
        }
        AdjustComponent b = this.bounds;
        float left = b.x;
        float right = b.x + b.width;
        float rowsBottom = (float) this.presets().size() * 10.0F + this.y;
        if (mouse.y >= rowsBottom) {
            if (this.recordButton.isHovered(mouse.x, mouse.y)) {
                this.startRecording();
                return true;
            }
            return false;
        }
        BedDefensePreset preset = this.presetAt(mouse.y);
        if (preset == null) {
            return false;
        }
        if (preset.builtinId == null && mouse.x >= right - 10.0F) {
            if (this.isMenuFor(preset)) {
                this.closeMenu(null);
            } else {
                this.openMenu(preset);
            }
            return true;
        }
        if (mouse.x >= left && mouse.x <= left + 9.0F) {
            this.setting.toggle(preset.id);
            return true;
        }
        this.pressedId = preset.id;
        this.dragStartY = mouse.y;
        this.dragging = false;
        return true;
    }

    @Override
    public void onClickOutside(com.adjustclient.util.Vec2f mouse, int button, boolean pressed) {
        if (button == 0 && !pressed) {
            this.pressedId = null;
            this.dragging = false;
        }
    }
}
