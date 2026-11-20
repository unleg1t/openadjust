package net.minecraft.client.gui;

import com.adjustclient.core.events.ChatLineAddedEvent;
import com.adjustclient.core.events.ChatLineEvent;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.modules.impl.render.tweaks.TweaksModule;
import com.adjustclient.core.gui.chat.ChatSenderPatterns;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPersistentChat extends GuiNewChat
{
    public final List<Object> pendingMessages = Lists.newArrayList();
    public final Object pendingMessagesLock = new Object();
    private final Map<ChatLine, NetworkPlayerInfo> lineSenders = Collections.synchronizedMap(new WeakHashMap<ChatLine, NetworkPlayerInfo>());
    public int unreadMessages;
    public int maxDrawnChatLines = 100;

    public GuiPersistentChat(Minecraft mcIn)
    {
        super(mcIn);
    }

    private NetworkPlayerInfo getLineSender(ChatLine line)
    {
        return line == null ? null : this.lineSenders.get(line);
    }

    private void setLineSender(ChatLine line, NetworkPlayerInfo sender)
    {
        if (line != null && sender != null)
        {
            this.lineSenders.put(line, sender);
        }
    }

    public List<ChatLine> getVisibleChatLines()
    {
        if (this.mc.currentScreen instanceof GuiChat)
        {
            return ((GuiChat)this.mc.currentScreen).getChatCommandSuggestions().withManual(this, this.mc.currentScreen, this.drawnChatLines);
        }

        return this.drawnChatLines;
    }

    private void drainPendingMessages()
    {
        List<Object> drained;

        synchronized (this.pendingMessagesLock)
        {
            if (this.pendingMessages.isEmpty())
            {
                return;
            }

            drained = new ArrayList<Object>(this.pendingMessages);
            this.pendingMessages.clear();
        }

        for (Object item : drained)
        {
            if (item instanceof QueuedChatMessage)
            {
                QueuedChatMessage queued = (QueuedChatMessage)item;
                this.printChatMessageWithOptionalDeletion(queued.component, queued.chatLineId, queued.persistent);
            }
            else if (item instanceof IChatComponent)
            {
                this.printChatMessageWithOptionalDeletion((IChatComponent)item, 0, false);
            }
        }
    }

    @Override
    public void drawChat(int updateCounter)
    {
        this.drainPendingMessages();

        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            List<ChatLine> visibleLines = this.getVisibleChatLines();
            int i = this.getLineCount();
            boolean flag = this.getChatOpen();
            int j = 0;
            int k = visibleLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0)
            {
                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                for (int i1 = 0; i1 + this.scrollPos < visibleLines.size() && i1 < i; ++i1)
                {
                    ChatLine chatline = visibleLines.get(i1 + this.scrollPos);

                    if (chatline != null)
                    {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag)
                        {
                            double d0 = (double)j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0D * d0);

                            if (flag)
                            {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);
                            ++j;

                            if (l1 > 3)
                            {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                boolean transparent = TweaksModule.INSTANCE != null
                                        && TweaksModule.INSTANCE.isEnabled()
                                        && TweaksModule.INSTANCE.chat != null
                                        && TweaksModule.INSTANCE.chat.isModernSuggestionsEnabled()
                                        && TweaksModule.INSTANCE.chat.transparent.isEnabled();
                                if (!transparent)
                                {
                                    drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                                }
                                if (TweaksModule.INSTANCE != null
                                        && TweaksModule.INSTANCE.chat != null
                                        && TweaksModule.INSTANCE.chat.isStackingEnabled()
                                        && chatline.firstWrappedLine)
                                {
                                    NetworkPlayerInfo sender = this.getLineSender(chatline);
                                    if (sender != null)
                                    {
                                        ChatSenderPatterns.drawHead(sender, i2, j2 - 8, l1);
                                        i2 += 10;
                                    }
                                }
                                IChatComponent comp = chatline.getChatComponent();
                                String s = comp.getFormattedText();
                                int textColor = 16777215 + (l1 << 24);
                                if (comp.getChatStyle() != null && comp.getChatStyle().colorSupplier != null)
                                {
                                    Integer supplied = comp.getChatStyle().colorSupplier.get();
                                    if (supplied != null)
                                    {
                                        textColor = (l1 << 24) | (supplied.intValue() & 0xFFFFFF);
                                    }
                                }
                                GlStateManager.enableBlend();
                                this.mc.fontRendererObj.drawStringWithShadow(s, (float)i2, (float)(j2 - 8), textColor);
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag)
                {
                    int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * k2 + k;
                    int i3 = j * k2 + j;
                    int j3 = this.scrollPos * i3 / k;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3)
                    {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void clearChatMessages()
    {
        synchronized (this.pendingMessagesLock)
        {
            this.pendingMessages.clear();
        }

        this.unreadMessages = 0;
        super.clearChatMessages();
    }

    @Override
    public void printChatMessage(IChatComponent chatComponent)
    {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0, true);
    }

    @Override
    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId)
    {
        this.printChatMessageWithOptionalDeletion(chatComponent, chatLineId, true);
    }

    @Override
    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId, boolean persistent)
    {
        synchronized (this.pendingMessagesLock)
        {
            if (!this.pendingMessages.isEmpty() || !this.mc.isCallingFromMinecraftThread())
            {
                this.pendingMessages.add(new QueuedChatMessage(chatComponent, persistent, chatLineId));
                return;
            }
        }

        int updateCounter = this.mc.ingameGUI != null ? this.mc.ingameGUI.getUpdateCounter() : 0;
        this.setChatLine(chatComponent, chatLineId, updateCounter, false, System.currentTimeMillis());

        if (persistent)
        {
            String filtered = ChatAllowedCharacters.filterAllowedCharacters(StringUtils.stripControlCodes(chatComponent.getUnformattedText()));
            if (!filtered.isBlank())
            {
                Logger.log(LogLevel.INFO, true, "{}", filtered);
            }
        }
    }

    @Override
    protected void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly)
    {
        this.setChatLine(chatComponent, chatLineId, updateCounter, displayOnly, System.currentTimeMillis());
    }

    public void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, long timestamp)
    {
        final boolean removedOldLine;
        if (chatLineId != 0)
        {
            final int targetId = chatLineId;
            removedOldLine = this.removeMatchingLines(new Predicate<ChatLine>()
            {
                @Override
                public boolean test(ChatLine entry)
                {
                    return entry.getChatLineID() == targetId;
                }
            });
        }
        else
        {
            removedOldLine = false;
        }

        IChatComponent origChatComponent = chatComponent;
        ChatLineEvent preEvent = new ChatLineEvent(this, chatComponent, displayOnly, true, removedOldLine, timestamp);
        EventBus.post(preEvent);
        chatComponent = preEvent.component;
        if (chatComponent == null)
        {
            return;
        }

        NetworkPlayerInfo sender = null;
        if (TweaksModule.INSTANCE != null && TweaksModule.INSTANCE.chat != null && TweaksModule.INSTANCE.chat.isStackingEnabled())
        {
            sender = ChatSenderPatterns.findSender(origChatComponent);
        }

        int i = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
        if (sender != null)
        {
            i = (i - 10 < 2) ? 1 : (i - 10);
        }

        List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();

        for (int idx = 0; idx < list.size(); ++idx)
        {
            IChatComponent ichatcomponent = list.get(idx);
            if (chatComponent.getChatStyle() != null
                    && chatComponent.getChatStyle().colorSupplier != null
                    && ichatcomponent.getChatStyle() != null
                    && ichatcomponent.getChatStyle().colorSupplier == null)
            {
                ichatcomponent.getChatStyle().colorSupplier = chatComponent.getChatStyle().colorSupplier;
            }

            if (flag && this.scrollPos > 0)
            {
                this.isScrolled = true;
                this.scroll(1);
            }

            ChatLine drawnLine = new ChatLine(updateCounter, ichatcomponent, origChatComponent, chatComponent, chatLineId, timestamp, idx == 0);
            this.setLineSender(drawnLine, sender);
            this.drawnChatLines.add(0, drawnLine);
        }

        while (this.drawnChatLines.size() > this.maxDrawnChatLines)
        {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }

        if (!displayOnly)
        {
            if (!removedOldLine)
            {
                ++this.unreadMessages;
            }

            ChatLine chatLine = new ChatLine(updateCounter, chatComponent, origChatComponent, chatComponent, chatLineId, timestamp, true);
            this.setLineSender(chatLine, sender);
            this.chatLines.add(0, chatLine);
            EventBus.post(new ChatLineAddedEvent(this, chatLine));

            while (this.chatLines.size() > this.maxDrawnChatLines)
            {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }

        EventBus.post(new ChatLineEvent(this, chatComponent, displayOnly, false, removedOldLine, timestamp));
    }

    @Override
    public void refreshChat()
    {
        this.drawnChatLines.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i)
        {
            ChatLine chatline = this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true, chatline.timestamp);
        }
    }

    public void refreshChatLines()
    {
        int prevUnread = this.unreadMessages;
        int prevScroll = this.scrollPos;
        boolean prevScrolled = this.isScrolled;
        this.drawnChatLines.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i)
        {
            ChatLine chatline = this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true, chatline.timestamp);
        }

        this.unreadMessages = prevUnread;
        this.scrollPos = prevScroll + this.scrollPos;
        int maxScroll = this.drawnChatLines.size() - this.getLineCount();

        if (this.scrollPos > maxScroll)
        {
            this.scrollPos = maxScroll;
        }

        if (this.scrollPos < 1)
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
        else
        {
            this.isScrolled = prevScrolled;
        }
    }

    public boolean removeChatLine(ChatLine line, boolean singleLine)
    {
        if (line == null || !this.drawnChatLines.contains(line))
        {
            return false;
        }

        if (singleLine)
        {
            Iterator<ChatLine> it = this.drawnChatLines.iterator();

            while (it.hasNext())
            {
                if (it.next() == line)
                {
                    it.remove();
                    break;
                }
            }
        }
        else
        {
            final IChatComponent original = line.originalComponent;
            this.drawnChatLines.removeIf(new Predicate<ChatLine>()
            {
                @Override
                public boolean test(ChatLine entry)
                {
                    return entry == line || (original != null && entry.originalComponent == original);
                }
            });
            this.chatLines.removeIf(new Predicate<ChatLine>()
            {
                @Override
                public boolean test(ChatLine entry)
                {
                    return entry == line || (original != null && entry.originalComponent == original);
                }
            });
        }

        this.clampScroll();
        return true;
    }

    public boolean removeMatchingLines(Predicate<ChatLine> predicate)
    {
        if (predicate == null)
        {
            return false;
        }

        final Set<IChatComponent> removedOriginals = Collections.newSetFromMap(new IdentityHashMap<IChatComponent, Boolean>());
        boolean removed = false;
        Iterator<ChatLine> it = this.drawnChatLines.iterator();

        while (it.hasNext())
        {
            ChatLine entry = it.next();

            if (predicate.test(entry))
            {
                if (entry.originalComponent != null)
                {
                    removedOriginals.add(entry.originalComponent);
                }

                it.remove();
                removed = true;
            }
        }

        Iterator<ChatLine> chatIt = this.chatLines.iterator();

        while (chatIt.hasNext())
        {
            ChatLine entry = chatIt.next();

            if (predicate.test(entry) || (entry.originalComponent != null && removedOriginals.contains(entry.originalComponent)))
            {
                chatIt.remove();
                removed = true;
            }
        }

        if (removed)
        {
            this.clampScroll();
        }

        return removed;
    }

    private void clampScroll()
    {
        int maxScroll = this.drawnChatLines.size() - this.getLineCount();

        if (this.scrollPos > maxScroll)
        {
            this.scrollPos = maxScroll;
        }

        if (this.scrollPos < 1)
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    @Override
    public IChatComponent getChatComponent(int mouseX, int mouseY)
    {
        if (!this.getChatOpen())
        {
            return null;
        }

        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int i = scaledresolution.getScaleFactor();
        float f = this.getChatScale();
        int j = mouseX / i - 3;
        int k = mouseY / i - 27;
        j = MathHelper.floor_float((float)j / f);
        k = MathHelper.floor_float((float)k / f);

        if (j >= 0 && k >= 0)
        {
            List<ChatLine> visibleLines = this.getVisibleChatLines();
            int l = Math.min(this.getLineCount(), visibleLines.size());

            if (j <= MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l)
            {
                int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

                if (i1 >= 0 && i1 < visibleLines.size())
                {
                    ChatLine chatline = visibleLines.get(i1);
                    float j1 = 0.0F;
                    if (TweaksModule.INSTANCE != null
                            && TweaksModule.INSTANCE.chat != null
                            && TweaksModule.INSTANCE.chat.isStackingEnabled()
                            && chatline.firstWrappedLine
                            && this.getLineSender(chatline) != null)
                    {
                        if (j < 10)
                        {
                            return null;
                        }
                        j1 = 10.0F;
                    }

                    for (IChatComponent ichatcomponent : chatline.getChatComponent())
                    {
                        if (ichatcomponent instanceof ChatComponentText)
                        {
                            j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));

                            if (j1 > j)
                            {
                                return ichatcomponent;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    public static class QueuedChatMessage
    {
        public final IChatComponent component;
        public final int chatLineId;
        public final boolean persistent;
        public int unused;

        public QueuedChatMessage(IChatComponent component, boolean persistent, int chatLineId)
        {
            this.component = component;
            this.persistent = persistent;
            this.chatLineId = chatLineId;
        }
    }
}
