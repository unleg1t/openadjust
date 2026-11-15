package com.adjustclient.service.party.ui;

import com.adjustclient.Adjust;
import com.adjustclient.bind.BindMode;
import com.adjustclient.event.Render2DEvent;
import com.adjustclient.event.ScreenMouseClickEvent;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.*;
import com.adjustclient.render.font.*;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.ClipboardUtil;
import com.adjustclient.util.SoundUtil;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

public final class PartyMemberRow extends PartyPanelRow {
    public ContextMenuKey member;
    public String message;
    public TextComponent[][] lines;
    public ResourceLocation skin = net.minecraft.client.resources.DefaultPlayerSkin.getDefaultSkinLegacy();
    public AtomicInteger textColor = new AtomicInteger();
    public Animation fade = new Animation(Easing.LINEAR, 8500L);

    public PartyMemberRow(ContextMenuKey member, String message) {
        this.member = member;
        this.message = message;
        height = 0.0F;
        fade.value = 236.0F;
        ClientUser user = member.user;
        String texture = user.useClientColor ? Adjust.INSTANCE.screenStack.preferences.skinTexture.codec.value : user.displayName;
        if (texture == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) loadSkin(texture);
        else mc.addScheduledTask(() -> {
            loadSkin(texture);
        });
    }

    public void wrapMessage() {
        updateColor();
        PartyPanel panel = member.party;
        if (panel.title == null) panel.updateTitle();
        String combined = member.user.name + "\u0000: " + message;
        List<IChatComponent> wrapped = GuiUtilRenderComponents.splitText(new ChatComponentText(combined),
                (int)panel.width - 8, Fonts.TAHOMA_10, false, false);
        List<TextComponent[]> result = new ArrayList<>(1);
        for (int i = 0; i < wrapped.size(); i++) result.add(formatLine(i, wrapped.get(i).getUnformattedText()));
        lines = result.toArray(new TextComponent[0][]);
        height = 8.5F + Math.max(0, lines.length - 1) * 7.0F;
    }

    @Override public void onMouseClick(ScreenMouseClickEvent event) {
        if (event.mouseButton != 1 || event.inputAction != BindMode.PRESSED) return;
        PartyPanel panel = member.party;
        AdjustContextMenu menu = new AdjustContextMenu(panel, this, event.mouseX, event.mouseY);
        menu.entries.add(new ContextMenuAction(
                new TextComponent[]{new TextComponent("Copy", (int)4292006610L)}, () -> {
                    ClipboardUtil.copyToClipboard(message);
                }));
        boolean present;
        synchronized (panel.members) { present = panel.members.contains(member); }
        if (present && !member.user.useClientColor
                && panel.leader != member && panel.leader.user.useClientColor) {
            menu.entries.add(new ContextMenuAction(
                    new TextComponent[]{new TextComponent("Kick Member", (int)4293480528L)}, () -> {
                    }));
        }
        panel.contextMenu = menu;
    }

    @SuppressWarnings("unused")
    private void vendorKickMember(PartyPanel panel) {
        if (Adjust.INSTANCE.screenStack.partyService.party == panel
                && panel.containsMember(member.user))
            ServiceUtil.sendToBackend(com.adjustclient.backend.transport.HydraPacketEncoder.encodeKickPartyMember(member.user.name));
    }

    public TextComponent[] formatLine(int index, String text) {
        if (index != 0) return new TextComponent[]{new ReferenceColorText(text, textColor)};
        int separator = text.indexOf('\u0000');
        String body = separator + 3 <= text.length() ? text.substring(separator + 3) : "";
        String name = separator < 1 ? text : text.substring(0, separator);
        ClientUser user = member.user;
        return new TextComponent[]{user.useClientColor ? new AccentText(name) : new UserColorText(name, user),
                new TextComponent(": ", 0xFF9B9B9B), new ReferenceColorText(body, textColor)};
    }

    @Override public void render(Render2DEvent event, float x, float y, float width) {
        updateColor();
        event.postTasks.add(() -> {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.scale(0.2F, 0.2F, 0.2F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.86274511F);
            RenderUtil.drawHead(skin, (x + 3.0F) * 5.0F, (y - 2.0F) * 5.0F);
            GlStateManager.scale(5.0F, 5.0F, 5.0F);
        });
        wrapMessage();
        field13 = false;
        float drawX = x + 11.5F, drawY = y + 0.5F;
        for (TextComponent[] line : lines) {
            TextComponent.draw(line, Fonts.TAHOMA_10, drawX, drawY);
            drawY += 7.0F;
            drawX = x + 3.0F;
        }
    }

    public void playMessageSound() {
        ClientUser local = Adjust.INSTANCE != null && Adjust.INSTANCE.field40 instanceof com.adjustclient.property.UserManager
                ? (ClientUser) ((com.adjustclient.property.UserManager) Adjust.INSTANCE.field40).field16
                : null;
        if (local == null || member.user != local)
            SoundUtil.play("party.message", 0.5F, 0.89999998F);
        else SoundUtil.play("party.message", 0.5F, 1.25F);
    }

    public float updateColor() {
        int grey = Math.max(0, Math.min(255, (int)fade.value));
        textColor.set(new Color(grey, grey, grey).getRGB());
        return fade.update(165.0F);
    }

    public ResourceLocation loadSkin(String hash) {
        return Minecraft.getMinecraft().getSkinManager().loadSkin(
                new MinecraftProfileTexture("https://textures.minecraft.net/texture/" + hash, null),
                MinecraftProfileTexture.Type.SKIN, (type, location, texture) -> {
                    if (type == MinecraftProfileTexture.Type.SKIN && location != null) skin = location;
                });
    }
}
