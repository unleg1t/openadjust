package net.minecraft.client.gui;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.lunar.LunarClientService;
import com.adjustclient.lunar.websocket.LunarTabLogo;
import com.adjustclient.module.impl.exploit.ClientSpooferModule;
import com.adjustclient.module.impl.exploit.playerdetect.PlayerDetectModule;
import com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector;
import com.adjustclient.module.impl.exploit.playerdetect.hypixel.nick.NickDetector;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.module.impl.render.hud.playerlist.TeamColors;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.module.impl.render.tweaks.TweaksModule;
import com.adjustclient.service.servertracker.FriendServerTracker;
import com.adjustclient.user.ClientUser;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatColor;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiPlayerTabOverlay extends Gui
{
    private static final Ordering<NetworkPlayerInfo> field_175252_a = Ordering.from(new GuiPlayerTabOverlay.PlayerComparator());
    private final Minecraft mc;
    private final GuiIngame guiIngame;
    private IChatComponent footer;
    private IChatComponent header;
    private long lastTimeOpened;
    private boolean isBeingRendered;

    public GuiPlayerTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn)
    {
        this.mc = mcIn;
        this.guiIngame = guiIngameIn;
    }

    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        boolean flag = this.isNickedTabEntry(networkPlayerInfoIn);
        String s = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());

        if (StreamerModule.nameMap != null)
        {
            s = StreamerModule.replaceName(s, networkPlayerInfoIn.getGameProfile().getName());
        }

        return flag ? s.trim() + " " + ChatColor.RESET + ChatColor.DARK_RED + "*" : s;
    }

    private ClientUser getClientUser(NetworkPlayerInfo networkPlayerInfoIn)
    {
        if (HUDModule.tabOverlay != null && HUDModule.tabOverlay.isEnabled() && Adjust.INSTANCE != null && Adjust.INSTANCE.screenStack != null && Adjust.INSTANCE.screenStack.serverTracker instanceof FriendServerTracker)
        {
            return ((FriendServerTracker)Adjust.INSTANCE.screenStack.serverTracker).findUser(networkPlayerInfoIn.getGameProfile().getName());
        }

        return null;
    }

    private boolean isNickedTabEntry(NetworkPlayerInfo networkPlayerInfoIn)
    {
        return PlayerDetectModule.INSTANCE != null
            && PlayerDetectModule.INSTANCE.isEnabled()
            && PlayerDetectModule.INSTANCE.nickDetector != null
            && PlayerDetectModule.INSTANCE.nickDetectTabOverlay.isEnabled()
            && NickDetector.isNicked(networkPlayerInfoIn.getGameProfile().getName());
    }

    public void updatePlayerList(boolean willBeRendered)
    {
        if (willBeRendered && !this.isBeingRendered)
        {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = willBeRendered;
    }

    public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn)
    {
        NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());

        if (TweaksModule.INSTANCE != null && TweaksModule.INSTANCE.tabOverlay != null && TweaksModule.INSTANCE.tabOverlay.enabledSetting())
        {
            list.removeIf((p_test_1_) -> this.mc.theWorld != null && this.mc.theWorld.getScoreboard() != null && FriendServerTracker.isNpcName(this.mc.theWorld.getScoreboard().getPlayersTeam(p_test_1_.getGameProfile().getName())));
        }

        int i = 0;
        int j = 0;

        for (NetworkPlayerInfo networkplayerinfo : list)
        {
            int k = (int)this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkplayerinfo));
            ClientUser clientuser = this.getClientUser(networkplayerinfo);

            if (clientuser != null)
            {
                k += (int)this.mc.fontRendererObj.getStringWidth(" " + ChatColor.BOLD + clientuser.name);
            }

            if (PlayerDetectModule.isHackerTabOverlayEnabled() && this.mc.theWorld != null)
            {
                EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(networkplayerinfo.getGameProfile().getId());

                if (entityplayer != null && HackerDetector.isCheater(entityplayer.getEntityId()))
                {
                    k += 9;
                }
            }

            if (ClientSpooferModule.INSTANCE != null && ClientSpooferModule.INSTANCE.showIcon() && LunarClientService.INSTANCE != null && LunarClientService.INSTANCE.tabLogoCache != null && LunarClientService.INSTANCE.tabLogoCache.getTabLogo(networkplayerinfo.getGameProfile().getId()) != null)
            {
                k += 10;
            }

            i = Math.max(i, k);

            if (scoreObjectiveIn != null && scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS)
            {
                k = (int)this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn.getValueFromObjective(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
                j = Math.max(j, k);
            }
        }

        list = list.subList(0, Math.min(list.size(), 80));
        int l3 = list.size();
        int i4 = l3;
        int j4;

        for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4)
        {
            ++j4;
        }

        boolean flag = this.mc.isIntegratedServerRunning() || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
        int l;

        if (scoreObjectiveIn != null)
        {
            if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS)
            {
                l = 90;
            }
            else
            {
                l = j;
            }
        }
        else
        {
            l = 0;
        }

        int i1 = Math.min(j4 * ((flag ? 9 : 0) + i + l + 13), width - 50) / j4;
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int k1 = 10;
        int l1 = i1 * j4 + (j4 - 1) * 5;
        List<String> list1 = null;
        List<String> list2 = null;

        if (this.header != null)
        {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

            for (String s : list1)
            {
                l1 = Math.max(l1, (int)this.mc.fontRendererObj.getStringWidth(s));
            }
        }

        if (this.footer != null)
        {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

            for (String s2 : list2)
            {
                l1 = Math.max(l1, (int)this.mc.fontRendererObj.getStringWidth(s2));
            }
        }

        if (list1 != null)
        {
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list1.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s3 : list1)
            {
                int i2 = (int)this.mc.fontRendererObj.getStringWidth(s3);
                this.mc.fontRendererObj.drawStringWithShadow(s3, (float)(width / 2 - i2 / 2), (float)k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }

            ++k1;
        }

        drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

        for (int k4 = 0; k4 < l3; ++k4)
        {
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (k4 < list.size())
            {
                NetworkPlayerInfo networkplayerinfo1 = (NetworkPlayerInfo)list.get(k4);
                String s1 = this.getPlayerName(networkplayerinfo1);
                GameProfile gameprofile = networkplayerinfo1.getGameProfile();
                EntityPlayer entityplayer1 = this.mc.theWorld != null ? this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId()) : null;
                boolean flagCheater = PlayerDetectModule.isHackerTabOverlayEnabled() && entityplayer1 != null && HackerDetector.isCheater(entityplayer1.getEntityId());
                LunarTabLogo lunartablogo = ClientSpooferModule.INSTANCE != null && ClientSpooferModule.INSTANCE.showIcon() && LunarClientService.INSTANCE != null && LunarClientService.INSTANCE.tabLogoCache != null ? LunarClientService.INSTANCE.tabLogoCache.getTabLogo(gameprofile.getId()) : null;

                if (flag)
                {
                    EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
                    boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && (gameprofile.getName().equals("Dinnerbone") || gameprofile.getName().equals("Grumm"));
                    this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                    int l2 = 8 + (flag1 ? 8 : 0);
                    int i3 = 8 * (flag1 ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect(j2, k2, 8.0F, (float)l2, 8, i3, 8, 8, 64.0F, 64.0F);

                    if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT))
                    {
                        int j3 = 8 + (flag1 ? 8 : 0);
                        int k3 = 8 * (flag1 ? -1 : 1);
                        Gui.drawScaledCustomSizeModalRect(j2, k2, 40.0F, (float)j3, 8, k3, 8, 8, 64.0F, 64.0F);
                    }

                    j2 += 9;
                }

                if (lunartablogo != null)
                {
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                    GlStateManager.color(lunartablogo.red, lunartablogo.green, lunartablogo.blue, lunartablogo.alpha);
                    this.mc.getTextureManager().bindTexture(new ResourceLocation("adjust", "textures/other/lunar_logo.png"));
                    Gui.drawScaledCustomSizeModalRect((float)j2, (float)k2, 0.0F, 0.0F, 100.0F, 100.0F, 8.0F, 8.0F, 100.0F, 100.0F);
                    GlStateManager.disableBlend();
                }

                float fTextX = (float)j2 + (lunartablogo != null ? 10.0F : 0.0F);
                float fNameEndX;

                if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR)
                {
                    s1 = ChatColor.ITALIC + s1;
                    fNameEndX = this.mc.fontRendererObj.drawString(s1, fTextX, (float)k2, -1862270977);
                }
                else
                {
                    fNameEndX = this.mc.fontRendererObj.drawString(s1, fTextX, (float)k2, -1);
                }

                ClientUser clientuser = this.getClientUser(networkplayerinfo1);

                if (clientuser != null)
                {
                    this.mc.fontRendererObj.drawString(" " + ChatColor.BOLD + clientuser.name, fNameEndX - 0.5F, (float)k2, clientuser.getColor());
                }

                if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR)
                {
                    int k5 = j2 + i + 1;
                    int l5 = k5 + l;

                    if (l5 - k5 > 5)
                    {
                        this.drawScoreboardValues(scoreObjectiveIn, k2, gameprofile.getName(), k5, l5, networkplayerinfo1);
                    }
                }

                if (flagCheater)
                {
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.mc.getTextureManager().bindTexture(new ResourceLocation("adjust", "textures/warning/tab.png"));
                    float fWarnX = (float)(j2 - (flag ? 9 : 0) + i1 - 21);

                    if (l >= 6)
                    {
                        fWarnX -= (float)(l - 3);
                    }

                    Gui.drawModalRectWithCustomSizedTexture(fWarnX * 2.0F, (float)k2 * 2.0F, 0.0F, 0.0F, 16.0F, 16.0F, 16.0F, 16.0F);
                    GL11.glScalef(2.0F, 2.0F, 2.0F);
                }

                this.drawPing(i1, j2 - (flag ? 9 : 0), k2, networkplayerinfo1);
            }
        }

        if (list2 != null)
        {
            k1 = k1 + i4 * 9 + 1;
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s4 : list2)
            {
                int j5 = (int)this.mc.fontRendererObj.getStringWidth(s4);
                this.mc.fontRendererObj.drawStringWithShadow(s4, (float)(width / 2 - j5 / 2), (float)k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawPing(networkPlayerInfoIn.getResponseTime(), p_175245_1_, (float)p_175245_2_, (float)p_175245_3_, this.zLevel);
    }

    public static void drawPing(int ping, int width, float x, float y, float zLevel)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(icons);
        int i = 0;
        int j;

        if (ping < 0)
        {
            j = 5;
        }
        else if (ping < 150)
        {
            j = 0;
        }
        else if (ping < 300)
        {
            j = 1;
        }
        else if (ping < 600)
        {
            j = 2;
        }
        else if (ping < 1000)
        {
            j = 3;
        }
        else
        {
            j = 4;
        }

        float drawX = x + (float)width - 11.0F;
        float drawZ = zLevel + 100.0F;
        int u = 0 + i * 10;
        int v = 176 + j * 8;
        float f = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)drawX, (double)(y + 8.0F), (double)drawZ).tex((double)((float)u * f), (double)((float)(v + 8) * f)).endVertex();
        worldrenderer.pos((double)(drawX + 10.0F), (double)(y + 8.0F), (double)drawZ).tex((double)((float)(u + 10) * f), (double)((float)(v + 8) * f)).endVertex();
        worldrenderer.pos((double)(drawX + 10.0F), (double)y, (double)drawZ).tex((double)((float)(u + 10) * f), (double)((float)v * f)).endVertex();
        worldrenderer.pos((double)drawX, (double)y, (double)drawZ).tex((double)((float)u * f), (double)((float)v * f)).endVertex();
        tessellator.draw();
    }

    private void drawScoreboardValues(ScoreObjective p_175247_1_, int p_175247_2_, String p_175247_3_, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo p_175247_6_)
    {
        int i = p_175247_1_.getScoreboard().getValueFromObjective(p_175247_3_, p_175247_1_).getScorePoints();

        if (p_175247_1_.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS)
        {
            this.mc.getTextureManager().bindTexture(icons);

            if (this.lastTimeOpened == p_175247_6_.func_178855_p())
            {
                if (i < p_175247_6_.func_178835_l())
                {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(this.guiIngame.getUpdateCounter() + 20));
                }
                else if (i > p_175247_6_.func_178835_l())
                {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(this.guiIngame.getUpdateCounter() + 10));
                }
            }

            if (Minecraft.getSystemTime() - p_175247_6_.func_178847_n() > 1000L || this.lastTimeOpened != p_175247_6_.func_178855_p())
            {
                p_175247_6_.func_178836_b(i);
                p_175247_6_.func_178857_c(i);
                p_175247_6_.func_178846_a(Minecraft.getSystemTime());
            }

            p_175247_6_.func_178843_c(this.lastTimeOpened);
            p_175247_6_.func_178836_b(i);
            int j = MathHelper.ceiling_float_int((float)Math.max(i, p_175247_6_.func_178860_m()) / 2.0F);
            int k = Math.max(MathHelper.ceiling_float_int((float)(i / 2)), Math.max(MathHelper.ceiling_float_int((float)(p_175247_6_.func_178860_m() / 2)), 10));
            boolean flag = p_175247_6_.func_178858_o() > (long)this.guiIngame.getUpdateCounter() && (p_175247_6_.func_178858_o() - (long)this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

            if (j > 0)
            {
                float f = Math.min((float)(p_175247_5_ - p_175247_4_ - 4) / (float)k, 9.0F);

                if (f > 3.0F)
                {
                    for (int l = j; l < k; ++l)
                    {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)l * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                    }

                    for (int j1 = 0; j1 < j; ++j1)
                    {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);

                        if (flag)
                        {
                            if (j1 * 2 + 1 < p_175247_6_.func_178860_m())
                            {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 70, 0, 9, 9);
                            }

                            if (j1 * 2 + 1 == p_175247_6_.func_178860_m())
                            {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 79, 0, 9, 9);
                            }
                        }

                        if (j1 * 2 + 1 < i)
                        {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (j1 * 2 + 1 == i)
                        {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                }
                else
                {
                    float f1 = MathHelper.clamp_float((float)i / 20.0F, 0.0F, 1.0F);
                    int i1 = (int)((1.0F - f1) * 255.0F) << 16 | (int)(f1 * 255.0F) << 8;
                    String s = "" + (float)i / 2.0F;

                    if (p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s + "hp") >= p_175247_4_)
                    {
                        s = s + "hp";
                    }

                    this.mc.fontRendererObj.drawStringWithShadow(s, (float)((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2), (float)p_175247_2_, i1);
                }
            }
        }
        else
        {
            String s1 = ChatColor.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(s1, (float)(p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s1)), (float)p_175247_2_, 16777215);
        }
    }

    public void setFooter(IChatComponent footerIn)
    {
        this.footer = footerIn;
    }

    public void setHeader(IChatComponent headerIn)
    {
        this.header = headerIn;
    }

    public void resetFooterHeader()
    {
        this.header = null;
        this.footer = null;
    }

    @SideOnly(Side.CLIENT)
    public static class PlayerComparator implements Comparator<NetworkPlayerInfo>
        {
            public PlayerComparator()
            {
            }

            public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_)
            {
                if (Adjust.gameStateTracker != null && Adjust.gameStateTracker.state != null && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS)
                {
                    int i = TeamColors.getTeamIndex(p_compare_1_);
                    int j = TeamColors.getTeamIndex(p_compare_2_);

                    if (i != j)
                    {
                        return j > i ? -1 : 1;
                    }
                }

                ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
                ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
                return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
            }
        }
}
