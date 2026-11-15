package net.minecraft.client.multiplayer;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.ServerConnectionAttemptEvent;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.lunar.LunarClientService;
import com.adjustclient.module.impl.player.autohypixel.feature.AutoTip;
import com.adjustclient.module.impl.player.voicechat.VoiceChatModule;
import com.adjustclient.ui.account.AccountManagerScreen;
import com.adjustclient.ui.account.BanTracker;
import com.adjustclient.ui.hud.toggled.ToggledList;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.model.AntiCheatDetector;
import com.adjustclient.util.anticheat.model.AntiCheatModel;
import com.adjustclient.util.anticheat.model.WatchdogModel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class GuiConnecting extends GuiScreen
{
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    public boolean forceConnect;
    private NetworkManager networkManager;
    private boolean cancel;
    private final GuiScreen previousGuiScreen;
    private final ServerData serverData;

    public GuiConnecting(GuiScreen p_i1181_1_, Minecraft mcIn, ServerData p_i1181_3_)
    {
        this.mc = mcIn;
        this.previousGuiScreen = p_i1181_1_;
        this.serverData = p_i1181_3_;
        ServerAddress serveraddress = ServerAddress.fromString(p_i1181_3_.serverIP);
        mcIn.loadWorld((WorldClient)null);
        mcIn.setServerData(p_i1181_3_);
        this.connect(serveraddress.getIP(), serveraddress.getPort());
    }

    public GuiConnecting(GuiScreen p_i1182_1_, Minecraft mcIn, String hostName, int port)
    {
        this.mc = mcIn;
        this.previousGuiScreen = p_i1182_1_;
        this.serverData = null;
        mcIn.loadWorld((WorldClient)null);
        this.connect(hostName, port);
    }

    private static boolean isMissingServerInfo(ServerData data)
    {
        return data == null || !data.field_78841_f || data.pingToServer < 0L || data.serverMOTD == null;
    }

    private void connect(final String ip, final int port)
    {
        if (ServerTracker.INSTANCE != null)
        {
            ServerTracker.INSTANCE.field24 = ip;
            ServerTracker.INSTANCE.field28 = this.serverData;
        }

        if (!this.forceConnect
                && ip != null
                && AntiCheatDetector.matchesDomain(ip.toLowerCase(Locale.ROOT), "liquidproxy.net")
                && isMissingServerInfo(this.serverData))
        {
            ChatMessage message = new ChatMessage();
            message.bold = true;
            message.color = ChatColor.RED;
            message.append("Adjust");
            message.append("\n");
            message.append("You are connecting to a proxy server but the server information hasn't been received yet.");
            message.append("\n");
            message.append("The server type won't be able to be detected.");
            GuiDisconnected disconnected = new GuiDisconnected(this.previousGuiScreen, "connect.failed", message.getComponent());
            disconnected.forceReconnect = true;
            this.mc.displayGuiScreen(disconnected);
            return;
        }

        com.adjustclient.logging.Logger.log(
                LogLevel.INFO,
                true,
                "Establishing connection to {}",
                port == 25565 ? ip : ip + ":" + port);
        logger.info("Connecting to " + ip + ", " + port);
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet())
        {
            public void run()
            {
                InetAddress inetaddress = null;

                try
                {
                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    if (!GuiConnecting.this.forceConnect)
                    {
                        AntiCheatModel detectedModel = AntiCheatDetector.detectModel(ip, port, GuiConnecting.this.serverData);
                        if (detectedModel instanceof WatchdogModel)
                        {
                            AccountManagerScreen accounts = Adjust.INSTANCE != null ? Adjust.INSTANCE.accountManager : null;
                            if (accounts != null && accounts.isSessionAccountSelected() && accounts.selectedAccount != null)
                            {
                                Long expiry = accounts.selectedAccount.bans.getExpiry(((WatchdogModel) detectedModel).serverType);
                                if (expiry != null && !BanTracker.isUnbanned(expiry))
                                {
                                    ChatMessage message = new ChatMessage();
                                    message.bold = true;
                                    message.color = ChatColor.RED;
                                    message.append("Adjust");
                                    message.append("\n");
                                    if (expiry.longValue() == -1L)
                                    {
                                        message.append("This account is ");
                                        message.color = ChatColor.RED;
                                        message.append("permanently banned");
                                    }
                                    else
                                    {
                                        message.append("This account is banned for ");
                                        message.color = ChatColor.RED;
                                        message.append(TimeUtil.formatDuration(expiry.longValue() - System.currentTimeMillis(), false));
                                    }
                                    message.append("\n");
                                    message.color = ChatColor.GRAY;
                                    message.append("Connecting may result in your IP being banned.");
                                    GuiDisconnected disconnected = new GuiDisconnected(
                                            GuiConnecting.this.previousGuiScreen,
                                            "connect.failed",
                                            message.getComponent());
                                    disconnected.forceReconnect = true;
                                    GuiConnecting.this.mc.displayGuiScreen(disconnected);
                                    return;
                                }
                            }
                        }
                    }

                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    InetAddress[] resolved = InetAddress.getAllByName(ip);
                    inetaddress = resolved.length > 0 ? resolved[0] : InetAddress.getByName(ip);

                    EventBus.post(new ServerConnectionAttemptEvent(
                            ip,
                            inetaddress,
                            GuiConnecting.this.serverData,
                            port));

                    if (AutoTip.INSTANCE != null && AutoTip.INSTANCE.isActive())
                    {
                        AutoTip.INSTANCE.timer.lastMs = 0L;
                    }

                    if (VoiceChatModule.INSTANCE != null && VoiceChatModule.INSTANCE.isEnabled())
                    {
                        VoiceChatModule.INSTANCE.updateVoiceServer(true);
                    }

                    if (ToggledList.INSTANCE != null)
                    {
                        ToggledList.INSTANCE.dirty = true;
                    }

                    if (LunarClientService.INSTANCE != null)
                    {
                        LunarClientService.INSTANCE.refreshSession();
                    }

                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    if (Adjust.outboundPacketQueue != null)
                    {
                        Adjust.outboundPacketQueue.clear();
                    }

                    if (Adjust.inboundPacketQueue != null)
                    {
                        Adjust.inboundPacketQueue.clear();
                    }

                    GuiConnecting.this.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, GuiConnecting.this.mc.gameSettings.isUsingNativeTransport());
                    GuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(GuiConnecting.this.networkManager, GuiConnecting.this.mc, GuiConnecting.this.previousGuiScreen));
                    GuiConnecting.this.networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN));

                    if (ServerTracker.INSTANCE != null && ServerTracker.INSTANCE.serverState != null)
                    {
                        ServerTracker.INSTANCE.serverState.field32 = GuiConnecting.this.networkManager;
                        if (ServerTracker.INSTANCE.serverState.field36 != null)
                        {
                            ServerTracker.INSTANCE.serverState.field36.reset();
                        }
                    }

                    GuiConnecting.this.networkManager.sendPacket(new C00PacketLoginStart(GuiConnecting.this.mc.getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception)
                {
                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    GuiConnecting.logger.error((String)"Couldn\'t connect to server", (Throwable)unknownhostexception);
                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host"})));
                }
                catch (Exception exception)
                {
                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    GuiConnecting.logger.error((String)"Couldn\'t connect to server", (Throwable)exception);
                    String s = exception.toString();

                    if (inetaddress != null)
                    {
                        String s1 = inetaddress.toString() + ":" + port;
                        s = s.replaceAll(s1, "");
                    }

                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {s})));
                }
            }
        }).start();
    }

    public void updateScreen()
    {
        if (this.networkManager != null)
        {
            if (this.networkManager.isChannelOpen())
            {
                this.networkManager.processReceivedPackets();
            }
            else
            {
                this.networkManager.checkDisconnected();
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120 + 12, I18n.format("gui.cancel", new Object[0])));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.cancel = true;

            if (this.networkManager != null)
            {
                this.networkManager.closeChannel(new ChatComponentText("Aborted"));
            }

            this.mc.displayGuiScreen(this.previousGuiScreen);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        if (this.networkManager == null)
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("connect.connecting", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, I18n.format("connect.authorizing", new Object[0]), this.width / 2, this.height / 2 - 50, 16777215);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
