package com.adjustclient.managers.party;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.bind.BindMode;
import com.adjustclient.core.bind.KeyboardKey;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.movable.Movable;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.*;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.managers.party.ui.*;
import com.adjustclient.core.gui.dialog.ActionLabel;
import com.adjustclient.utilities.ScreenUtil;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import org.joml.Vector4d;
import org.joml.Vector4f;
import net.minecraft.util.ChatColor;

public final class PartyOverlay implements Listener {
    public static PartyOverlay INSTANCE;
    public static boolean introShown;
    public PartyService service;
    public Movable movable;
    public Timer focusTimer = new Timer();

    public PartyOverlay(PartyService service) { this.service = service; }

    @EventHandler public void onKey(ScreenKeyEvent event) {
        boolean blocking = Adjust.INSTANCE.screenStack.hasBlockingOverlay();
        if (blocking == event.pressed) return;
        PartyPanel panel = service.party;
        if (panel == null || !panel.chat.input.focused) return;
        if (event.keyCode == org.lwjgl.input.Keyboard.KEY_ESCAPE) panel.chat.closeChat(false);
        else if (focusTimer.hasElapsed(5L)) panel.chat.onKey(event);
    }

    @EventHandler public void onMouse(ScreenMouseClickEvent event) {
        PartyPanel panel = service.party;
        if (panel != null) {
            Movable bounds = panel.movable;
            Vec2f position = bounds.updatePosition();
            float x = position.x, y = position.y - 1.0F, height = bounds.height + 2.0F;
            if (panel.title == null) panel.updateTitle();
            float width = panel.width;
            boolean unhandled = true;
            if (panel.contextMenu != null) {
                unhandled = !panel.contextMenu.handleMouse(event.mouseButton,
                        event.inputAction, event.mouseX, event.mouseY);
                if (event.mouseButton != -1 && event.inputAction == BindMode.PRESSED)
                    panel.contextMenu = null;
            }
            if (event.mouseX < x || event.mouseX > x + width || event.mouseY < y || event.mouseY > y + height) {
                if (event.mouseButton == 0 && event.inputAction == BindMode.PRESSED)
                    panel.chat.closeChat(false);
            } else {
                event.cancelled = true;
                if (event.mouseButton == 0 && event.inputAction == BindMode.PRESSED
                        && panel.isCloseButtonHovered(event.mouseX, event.mouseY)) {
                    service.leaveParty(panel);
                    return;
                }
                float inputTop = y + height - 9.5F;
                if (event.mouseY > y + 10.0F && event.mouseY <= inputTop) {
                    int wheelDelta = org.lwjgl.input.Mouse.getEventDWheel();
                    if (wheelDelta != 0)
                        panel.scroll(true, Math.max(-1.0F, Math.min(1.0F, (float) wheelDelta)) * 13.0F);
                    if (event.mouseButton == 1 && event.inputAction == BindMode.PRESSED) {
                        for (PartyPanelButton button : panel.visibleRows) {
                            PartyPanelRow row = (PartyPanelRow) button.owner;
                            float rowX = x + button.x;
                            float rowY = y + button.y;
                            if (event.mouseX > rowX && event.mouseX <= rowX + width
                                    && event.mouseY >= rowY - 3.0F && event.mouseY <= rowY + row.height - 3.0F) {
                                row.onMouseClick(event);
                                unhandled = false;
                                break;
                            }
                        }
                    }
                }
                if (event.mouseButton == 1 && panel.contextMenu == null) {
                    if (event.inputAction == BindMode.PRESSED) {
                        AdjustContextMenu menu = new AdjustContextMenu(panel, event.mouseX, event.mouseY);
                        if (event.mouseY >= inputTop) {
                            menu.entries.add(new ContextMenuAction(
                                    new TextComponent[]{new TextComponent("Paste", new Color(210, 210, 210).getRGB())},
                                    () -> {
                                        panel.chat.input.field.writeText(com.adjustclient.utilities.ClipboardUtil.getClipboard());
                                    }));
                        }
                        menu.entries.add(panel.createLeaveAction());
                        panel.contextMenu = menu;
                    }
                } else if (event.mouseButton != -1 && event.inputAction == BindMode.PRESSED) {
                    if (event.mouseY >= inputTop) {
                        if (!panel.chat.input.focused && event.mouseButton == 0) openChat();
                    } else if (unhandled) panel.chat.closeChat(false);
                }
                return;
            }
        }
        if (event.mouseButton != -1 && event.inputAction == BindMode.PRESSED)
            clickInvitation(event.mouseButton, new Vec2f(event.mouseX, event.mouseY));
    }

    @EventHandler public void onTick(PreClientTickEvent event) {
        PartyPanel panel = service.party;
        if (panel == null) return;
        panel.chat.input.field.updateCursorCounter();
        panel.pingRenderer.pings.removeIf(PartyPingRenderer.expired);
        panel.pingRenderer.pings.forEach(PartyPingRenderer.updatePing);
    }

    @EventHandler public void onWorldRender(Render2DEvent event) {
        if (service.party == null) return;
        PartyPingRenderer pings = service.party.pingRenderer;
        GlStateManager.pushMatrix();
        Vector4d[] positions = new Vector4d[pings.pings.size()];
        if (pings.projectPings(positions, event.scaledResolution, event.partialTicks))
            pings.renderLabels(positions, event.partialTicks);
        GlStateManager.popMatrix();
    }

    @EventHandler public void onPingRender(Render3DEvent event) {
        if (service.party != null) service.party.pingRenderer.renderWorld(event);
    }

    public void clickInvitation(int button, Vec2f mouse) {
        for (PartyChatMessage message : service.invitations) {
            if (message.dismissed || message.expiresAt - System.currentTimeMillis() < 1L) continue;
            ActionLabel label;
            if (message.accept.contains(mouse)) label = message.accept;
            else if (message.decline.contains(mouse)) label = message.decline;
            else continue;
            message.onDecision.accept(label == message.accept);
            message.dismissed = true;
            return;
        }
    }

    public void onRender(Render2DEvent event, float mouseX, float mouseY) {
        synchronized (service.invitations) {
            float y = 66.0F;
            for (int i = 0; i < service.invitations.size();) {
                PartyChatMessage message = service.invitations.get(i);
                boolean finished = message.dismissed || message.expiresAt - System.currentTimeMillis() < 1L;
                if (finished && message.animation.value <= 0.0F) {
                    service.invitations.remove(i);
                } else {
                    drawInvitation(message, event.scaledResolution, y, mouseX, mouseY);
                    y += 22.0F;
                    i++;
                }
            }
        }
        if (service.party != null) {
            movable.drawOverlay(event.scaledResolution);
            drawParty(event, service.party, mouseX, mouseY);
        }
    }

    public boolean canDrag(float mouseX, float mouseY) {
        return service.party == null || !service.party.isCloseButtonHovered(mouseX, mouseY);
    }

    public void drawParty(Render2DEvent event, PartyPanel panel, float mouseX, float mouseY) {
        Movable bounds = panel.movable;
        PartyChat chat = panel.chat;
        float oldHeight = bounds.height;
        bounds.height = 90.0F + Math.max(0, chat.input.lines.length - 1) * 7.5F;
        panel.scrollAnimation.value += oldHeight - bounds.height;
        if (!ScreenUtil.isScreenOpen()) {
            if (chat.input.focused) chat.closeChat(false);
            if (panel.contextMenu != null) panel.contextMenu = null;
        }
        if (panel.autoScroll) panel.scroll(false, -99999.0F);
        Vec2f position = bounds.updatePosition();
        float x = position.x, y = position.y - 1.0F, height = bounds.height + 2.0F;
        if (panel.title == null) panel.updateTitle();
        float width = panel.width;
        NvgContext.pushEvenScale();
        drawPanelFrame((int)2350060307L, (int)2352297783L, x, y + 1.0F, x + width, y + height);
        RenderUtil.drawShadowRect(com.adjustclient.modules.impl.render.hud.HUDModule.getHudColor(), x + 0.5F, y + 10.0F, width - 1.5F);
        NvgContext.popEvenScale();
        panel.closeHoverAnimation.update(panel.isCloseButtonHovered(mouseX, mouseY) ? 1.0F : 0.0F);
        TextComponent.draw(panel.title, Fonts.TAHOMA_10, x + 3.0F, y + 5.0F);
        Fonts.TAHOMA_10.renderString(ChatColor.BOLD.toString() + "x", x + width - 6.0F, y + 5.0F,
                ColorUtil.blend(new Color(222, 45, 78), new Color(99, 99, 99), panel.closeHoverAnimation.value).getRGB(), false);
        chat.typingHeight.update(panel.hasTypingMembers ? 7.0F : 0.0F);
        chat.typingFade.duration = panel.hasTypingMembers ? 150L : 60L;
        chat.typingFade.update(panel.hasTypingMembers ? 1.0F : 0.0F);
        float inputHeight = 9.5F + Math.max(0, chat.input.lines.length - 1) * 7.5F;
        chat.draw((int)2352297783L, event.scaledResolution, x, y, height, width, inputHeight, mouseX, mouseY);
        panel.layoutVisibleRows(height, inputHeight, 10.0F);
        float contentHeight = height - inputHeight - 10.0F;
        RenderUtil.pushScissor(event.scaledResolution, 1, x, y + 10.0F, width, contentHeight);
        if (chat.typingHeight.value != 0.0F && panel.autoScroll) {
            List<ContextMenuKey> typers = new ArrayList<>();
            StringBuilder text = new StringBuilder();
            synchronized (panel.members) {
                for (ContextMenuKey key : panel.members) if (key.isTyping) typers.add(key);
                for (int i = 0; i < typers.size(); i++) {
                    text.append(ChatColor.WHITE).append(typers.get(i).user.name).append(ChatColor.RESET);
                    if (i != typers.size() - 1) text.append(", ");
                }
                text.append(typers.size() == 1 ? " is" : " are").append(" typing...");
            }
            int alpha = Math.max(0, Math.min(255, (int)(chat.typingFade.value * 255.0F)));
            Fonts.TAHOMA_10.drawString(text.toString(), x + 3.0F,
                    y + height - inputHeight - 10.0F + 11.5F - chat.typingHeight.value,
                    new Color(155, 155, 155, alpha).getRGB());
        }
        RenderUtil.popScissor();
        RenderUtil.pushScissor(event.scaledResolution, 1, x, y + 11.0F, width, contentHeight - 1.5F);
        Render2DEvent deferred = new Render2DEvent(event.scaledResolution, event.partialTicks);
        boolean hoveredAny = false;
        for (PartyPanelButton button : panel.visibleRows) {
            PartyPanelRow row = (PartyPanelRow) button.owner;
            boolean selected = panel.contextMenu != null && panel.contextMenu.source == row;
            float rowX = x + button.x, rowY = y + button.y;
            boolean hovered = false;
            if (Minecraft.getMinecraft().currentScreen != null) {
                hovered = selected || (!hoveredAny && mouseX >= rowX && mouseX <= rowX + width
                        && mouseY >= rowY - 3.0F && mouseY <= rowY + row.height - 3.0F);
            }
            row.updateHighlight(hovered, selected);
            if (hovered && !selected) hoveredAny = true;
            if (row.highlightAnimation.value != 0.0F) {
                float value = row.highlightAnimation.value;
                int grey = (int)(value * 10.0F + 25.0F), alpha = (int)(value * 100.0F);
                NvgDraw.fillRect(new Color(grey, grey, grey, alpha).getRGB(), rowX, rowY - 3.0F,
                        rowX + width, rowY - 3.0F + row.height);
            }
            row.render(deferred, rowX, rowY, width);
            if (!row.heightCounted) {
                panel.addContentHeight(row.height);
                row.heightCounted = true;
            }
        }
        NvgContext.flushFrame();
        RenderUtil.pushScissor(event.scaledResolution, 1, x, y + 11.0F, width, contentHeight - 1.5F);
        deferred.runPostTasks();
        RenderUtil.popScissor();
        RenderUtil.popScissor();
        chat.scrollFade.update(chat.field40 ? 60.0F : 0.0F);
        if (chat.scrollFade.value != 0.0F) {
            float bottom = y + height - inputHeight;
            int alpha = Math.max(0, Math.min(255, (int)chat.scrollFade.value));
            NvgDraw.fillGradientRect(new Color(134, 134, 134, 0).getRGB(), new Color(134, 134, 134, alpha).getRGB(),
                    x, bottom - 4.0F, x + width, bottom);
        }
        if (panel.contextMenu != null) panel.contextMenu.render();
    }

    public void drawInvitation(PartyChatMessage message, ScaledResolution resolution, float y, float mouseX, float mouseY) {
        if (!introShown && message.introAnimation == null) {
            message.introAnimation = new Animation(Easing.EASE_OUT_SINE, message.animation.duration / 2L);
            introShown = true;
        }
        long remaining = message.expiresAt - System.currentTimeMillis();
        float cardHeight = 19.0F;
        Animation hint = message.introAnimation;
        if (hint != null) {
            if (remaining >= 26200L) hint.update(5.0F);
            else {
                hint.duration = 300L;
                hint.update(0.0F);
                if (hint.value <= 0.0F) message.introAnimation = null;
            }
            if (message.introAnimation != null) cardHeight += hint.value;
        }
        float textWidth = TextComponent.getWidth(message.text, Fonts.TAHOMA_12) + 6.0F;
        Animation reveal = message.animation;
        reveal.update(message.dismissed || remaining < 1L ? 0.0F : textWidth);
        reveal.duration = 220L;
        RenderUtil.pushScissor(resolution, 1, 1.5F, y - 0.5F, reveal.value + 0.5F, cardHeight + 0.5F);
        NvgContext.pushEvenScale();
        drawPanelFrame(2098402067, 1496396081, 2.0F, y + 1.0F, textWidth + 2.0F, y + cardHeight);
        NvgContext.popEvenScale();
        long clamped = Math.max(0L, Math.min(30000L, remaining));
        String time = com.adjustclient.utilities.TimeUtil.formatDuration(clamped, true);
        Fonts.TAHOMA_10.drawString("Incoming " + message.title + ChatColor.GRAY + " (" + time + ")",
                5.0F, y + 5.0F, com.adjustclient.modules.impl.render.hud.HUDModule.getHudColor().getRGB());
        if (message.introAnimation != null) {
            int alpha = Math.max(0, Math.min(255, (int)(message.introAnimation.value * 0.045F * 1000.0F)));
            Fonts.TAHOMA_8.renderString("You can change your invite settings from the HUD module.",
                    5.5F, y + 19.5F, new Color(185, 185, 185, alpha).getRGB(), false);
        }
        TextComponent.draw(message.text, Fonts.TAHOMA_12, 5.5F, y + 12.0F);
        float right = textWidth + 2.0F - 0.5F;
        ActionLabel decline = message.decline;
        right -= Fonts.TAHOMA_10.getStringWidth("Decline") + 2.0F;
        Color declineColor = ColorUtil.blend(com.adjustclient.modules.impl.render.hud.HUDModule.getHudColor(), new Color(148, 148, 148), decline.action.value);
        Fonts.TAHOMA_10.drawString("Decline", right, y + 5.0F, declineColor.getRGB());
        if (reveal.finished) {
            decline.x = right;
            decline.y = y + 5.0F;
            decline.action.update(decline.contains(new Vec2f(mouseX, mouseY)) ? 1.0F : 0.0F);
        }
        ActionLabel accept = message.accept;
        right -= Fonts.TAHOMA_10.getStringWidth("Accept") + 2.0F;
        Color acceptColor = ColorUtil.blend(com.adjustclient.modules.impl.render.hud.HUDModule.getHudColor(), new Color(148, 148, 148), accept.action.value);
        Fonts.TAHOMA_10.drawString("Accept", right, y + 5.0F, acceptColor.getRGB());
        if (reveal.finished) {
            accept.x = right;
            accept.y = y + 5.0F;
            accept.action.update(accept.contains(new Vec2f(mouseX, mouseY)) ? 1.0F : 0.0F);
        }
        RenderUtil.popScissor();
    }

    /** Draws a pixel-snapped fill and even-scale-adjusted 0.5px border inside an active {@link NvgContext#pushEvenScale()} block. */
    private static void drawPanelFrame(int fillColor, int borderColor, float x1, float y1, float x2, float y2) {
        Vector4f rect = NvgContext.toPixels(x1, y1, x2, y2);
        NvgDraw.fillRect(fillColor, rect.x, rect.y, rect.z, rect.w);
        float lineWidth = NvgContext.scaleFactor == null ? 0.5F
                : (NvgContext.roundScaledPixels ? Math.round(NvgContext.scaleFactor)
                        : NvgContext.scaleFactor.intValue()) * 0.5F;
        NvgDraw.strokeRect(false, borderColor, rect.x, rect.y, rect.z, rect.w, lineWidth);
    }

    public void openChat() {
        if (service.party == null) return;
        focusTimer.reset();
        service.party.chat.input.focused = true;
        KeyBinding.unPressAllKeys();
        if (Minecraft.getMinecraft().currentScreen == null)
            Adjust.INSTANCE.screenStack.addOverlay(PartyHudScreen.INSTANCE);
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        if (service.party != null) service.party.pingRenderer.pings.clear();
    }

    public void initialize() {
        Bind bind = Adjust.INSTANCE.bindManager.register("global.party.chatinput", mode -> {
            if (mode != BindMode.PRESSED || service.party == null) return;
            PartyChat chat = service.party.chat;
            if (chat.input.focused) chat.submit();
            else openChat();
        }, null);
        bind.down = true;
        if (bind.mutable) bind.setKey(new KeyboardKey(org.lwjgl.input.Keyboard.KEY_RETURN));
        bind.mutable = false;
        movable = Adjust.movableManager.registerMovable("partyHudMovable", new Vec2f(2.0F, 175.0F));
        movable.canDrag = () -> service.party != null;
        movable.dragFilter = this::canDrag;
    }
}
