package com.adjustclient.ui.account;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

import com.adjustclient.logging.Logger;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.util.Timer;
import com.adjustclient.Adjust;
import com.adjustclient.ui.account.repository.AccountRepository;
import com.adjustclient.auth.LoginResult;
import com.adjustclient.auth.LoginStatus;
import com.adjustclient.backend.api.BackendRequest;
import com.adjustclient.backend.api.PendingRequest;
import com.adjustclient.backend.api.request.repository.DeleteAccountRequest;
import com.adjustclient.backend.api.request.repository.GetAccountDataRequest;
import com.adjustclient.backend.api.request.repository.SetAccountBanRequest;
import com.adjustclient.backend.api.response.BackendResponse;
import com.adjustclient.backend.api.response.EmptyResponseBody;
import com.adjustclient.backend.api.response.ResponseStatus;
import com.adjustclient.backend.transport.ByteCodec;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.font.AccentText;
import com.adjustclient.render.font.TextComponent;
import com.adjustclient.server.ServerType;
import com.adjustclient.ui.account.login.AccountButton;
import com.adjustclient.ui.account.login.DeleteAccountButton;
import com.adjustclient.ui.account.login.LoginAccountButton;
import com.adjustclient.ui.account.login.RefreshAccountButton;
import com.adjustclient.ui.account.login.ViewAccountButton;
import com.adjustclient.ui.account.login.auth.SessionTokenAccount;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.TimeUtil;
import net.minecraft.util.ChatColor;
import com.adjustclient.service.util.ServiceUtil;

public abstract class AccountEntry implements GuiListExtended.IGuiListEntry {

    public static final ExecutorService EXECUTOR;

    /** Minimum time between two "last login" updates sent to the backend (see {@link #login}). */
    private static final long LAST_LOGIN_UPDATE_INTERVAL = 1000L;

    static {
        EXECUTOR = Executors.newFixedThreadPool(32);
    }

    public String username = "";
    public String uuid = "";
    public String token = "";
    public LastLogin lastLogin;
    public BanTracker bans;
    public AccountHead head;
    public AccountRepository repository;
    Timer clickTimer;
    Animation highlight;
    protected AccountButton deleteButton;
    protected List<AccountButton> buttons;
    HoverTooltip headTooltip;
    Timer headTimer;
    Timer loginTimer;
    public int id;
    int headTicks;
    public volatile boolean fetchingAccountData;

    protected AccountEntry() {
        this.bans = new BanTracker();
        this.clickTimer = new Timer();
        this.deleteButton = new DeleteAccountButton(this, new ResourceLocation("textures/gui/container/beacon.png"));
        this.loginTimer = new Timer();
        this.loginTimer.lastMs = System.currentTimeMillis() - 1000L;            // lastMs = now - 1000 (so the first update is not blocked)
    }

    public abstract void deserialize(byte[] data) throws IOException;

    public abstract AccountType getType();

    public abstract LoginResult login();

    public abstract byte[] serialize() throws IOException;

    // ------------------------------------------------------------------------------------------------------------------------------
    // login
    // ------------------------------------------------------------------------------------------------------------------------------

    public void login(Minecraft mc) {
        Minecraft game = Minecraft.getMinecraft();
        AccountManagerScreen manager = manager();
        if (game.thePlayer != null) {
            ServiceUtil.pendingSessionChange = (Runnable) () -> mc.session = new Session(this.username, this.uuid, this.token, "mojang");
        } else if (game.theWorld != null) {
            ServiceUtil.pendingSessionChange = (Runnable) () -> mc.session = new Session(this.username, this.uuid, this.token, "mojang");
        } else {
            mc.session = new Session(this.username, this.uuid, this.token, "mojang");
        }
        manager.lastAccount = this;

        this.lastLogin = new LastLogin("Local owner", System.currentTimeMillis(), null);
        this.loginTimer.lastMs = System.currentTimeMillis();
        LocalAccountStore.INSTANCE.save();
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // account data
    // ------------------------------------------------------------------------------------------------------------------------------

    public void requestAccountData(Consumer<Boolean> callback) {
        if (callback == null) {
            callback = b -> { };
        }
        if (this.fetchingAccountData) {
            callback.accept(Boolean.FALSE);
            return;
        }
        this.fetchingAccountData = true;
        Consumer<Boolean> cb = callback;
        EXECUTOR.execute(() -> {
            try { processAccountData(cb, serialize()); }
            catch (Exception error) {
                Minecraft.getMinecraft().addScheduledTask(() -> cb.accept(false));
                error.printStackTrace();
            } finally { this.fetchingAccountData = false; }
        });
    }

    void processAccountData(Consumer<Boolean> callback, byte[] data) throws IOException {
        deserialize(data);
        LoginResult result = (this instanceof SessionTokenAccount)
                ? ((SessionTokenAccount) this).requestSession()
                : login();
        LoginStatus status = result.status;
        switch (AccountEntrySwitchMap.$SwitchMap$LoginStatus[status.ordinal()]) {
            case 1:
            case 2:
            case 4: {
                List<TextComponent> parts = new ArrayList<>();
                parts.add(new TextComponent("Failed to login to ", 0xFFBEBCBD));
                parts.add(new AccentText(this.username));
                parts.add(new TextComponent(".", 0xFFBEBCBD));
                if (this instanceof SessionTokenAccount) {
                    parts.add(new TextComponent(" (session account)", 0xFF8A8A8A));
                }
                int kind = AccountEntrySwitchMap.$SwitchMap$LoginStatus[status.ordinal()];
                if (kind == 1) {
                    parts.add(new TextComponent(" (abuse locked)", 0xFFBEBCBD));
                } else if (kind == 2) {
                    parts.add(new TextComponent(" (no Minecraft)", 0xFFBEBCBD));
                }
                NotificationBuilder notification = new NotificationBuilder("Session", parts, Integer.valueOf(60));
                notification.show();
                callback.accept(Boolean.FALSE);
                break;
            }
            case 3: {
                Session session = result.session;
                Minecraft.getMinecraft().addScheduledTask(() -> onSessionUpdated(callback, session));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void setSelected(int slotIndex, int x, int y) {
    }

    /**
     * Local completion of the account login started by {@link #processAccountData}: replaces the credentials with those
     * of {@code session}, logs in ({@link #login}), calls {@code callback.accept(true)} and shows the "Session" notification.
     */
    void onSessionUpdated(Consumer<Boolean> callback, Session session) {
        Minecraft mc = Minecraft.getMinecraft();
        this.username = session.getUsername();
        this.token = session.getToken();
        setUuid(session.getPlayerID());
        login(mc);
        callback.accept(Boolean.TRUE);
        List<TextComponent> parts = new ArrayList<>();
        parts.add(new TextComponent("Successfully logged into ", 0xFFBEBCBD));
        parts.add(new AccentText(this.username));
        parts.add(new TextComponent(".", 0xFFBEBCBD));
        NotificationBuilder notification = new NotificationBuilder("Session", parts, Integer.valueOf(60));
        HUDModule.INSTANCE_ALT.notifications.add(notification.build());
    }

    @SuppressWarnings("unused")
    private void vendorOnSessionUpdated(Consumer<Boolean> callback, Session session, BackendResponse response) {
        Minecraft mc = Minecraft.getMinecraft();
        if (response == null || response.status == ResponseStatus.OK) {
            onSessionUpdated(callback, session);
        } else {
            Logger.log(LogLevel.WARN, true, "Failed to update {} account: {}", new Object[] { this.username, response.status });
            callback.accept(Boolean.FALSE);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------
    // IGuiListEntry
    // ------------------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        Minecraft mc = Minecraft.getMinecraft();
        AccountManagerScreen manager = manager();
        if (this.buttons == null) {
            initButtons();
        }
        boolean wasSelected = manager.selectedAccount == this;
        if (!GuiScreen.isCtrlKeyDown()) {
            manager.select(this);
        } else {
            if (manager.selectedAccount != null && !manager.selection.contains(manager.selectedAccount)) {
                manager.selection.add(manager.selectedAccount);
            }
            if (GuiScreen.isShiftKeyDown()) {
                // range selection: indices of this entry and of the last selected entry inside the repository list
                List<AccountEntry> all = this.repository.accounts;
                int from = all.indexOf(this);
                int to = all.indexOf(manager.selection.get(manager.selection.size() - 1));
                if (from != -1 && to != -1) {
                    int low = Math.min(from, to);
                    int high = Math.max(from, to);
                    manager.selection.clear();
                    for (int i = low; i < high; i++) {
                        AccountEntry e = (AccountEntry) all.get(i);
                        if (!manager.selection.contains(e)) {
                            manager.selection.add(e);
                        }
                    }
                }
            }
        }
        manager.addToSelection(this);
        manager.selection.sort(Comparator.comparingInt(e -> this.repository.accounts.indexOf(e)));

        for (AccountButton button : this.buttons) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                return true;
            }
        }
        if (wasSelected) {
            // double click on the already selected row logs in
            if (this.clickTimer.lastMs + 700L > System.currentTimeMillis()) {
                login(mc);
            }
            this.clickTimer.lastMs = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        if (this.buttons == null) {
            initButtons();
        }
        AccountManagerScreen manager = manager();
        boolean selected = manager.selectedAccount == this || manager.selection.contains(this);
        float target = selected ? 120.0F : 0.0F;
        if (isSelected) {
            target += 80.0F;
        }
        if (this.loginTimer.lastMs + 200L > System.currentTimeMillis()) {
            target += 100.0F;
        }
        if (this.highlight == null) {
            this.highlight = new Animation(Easing.LINEAR, 100L);
        }
        int alpha = (int) this.highlight.update(target);
        alpha = Math.max(0, Math.min(255, alpha));
        if (alpha != 0) {
            Gui.drawRect(new Color(50, 50, 50, alpha).getRGB(), (float) x + 2.0F, (float) y, (float) (x + listWidth) - 0.5F, (float) (y + slotHeight));
            RenderUtil.drawBorderedRect(new Color(110, 110, 115, alpha).getRGB(), (float) x + 2.0F, (float) y, (float) (x + listWidth) - 0.5F, (float) (y + slotHeight), 0.5F);
        }

        // head animation tick: every 75 ms; after 10 ticks the head asks for its profile / skin again
        if (this.headTimer == null) {
            this.headTimer = new Timer();
        }
        if (this.head != null && this.headTimer.lastMs + 75L <= System.currentTimeMillis()) {
            if (this.headTicks++ >= 10) {
                this.head.c();
                this.headTimer.lastMs = System.currentTimeMillis();
            }
        }

        // buttons
        int buttonAlpha = Math.min(255, 2 * alpha);
        if (alpha >= 11) {
            Minecraft mc = Minecraft.getMinecraft();
            for (int i = 0; i < this.buttons.size(); i++) {
                AccountButton button = this.buttons.get(i);
                button.x = (float) (x + listWidth - i * (button.width + 2) - 10);   // button x
                button.y = (float) y + 1.5F;                                        // button y
                int a = button.getAlpha(buttonAlpha, mouseX, mouseY);
                button.drawBackground(a, button.x, button.y, (float) button.width, (float) button.height);   // drawBackground
                button.drawTexturedIcon(a);                                                  // drawTexturedIcon
                if (!(button instanceof LoginAccountButton)) {
                    String tooltip = button.getTooltip();
                    HoverTooltip state = (HoverTooltip) button.hoverTooltip;
                    if (state != null) {
                        state.draw(tooltip, mouseX, mouseY, buttonAlpha, button.x, button.y, 500);
                    } else if ((float) mouseX > button.x && button.x + button.width > (float) mouseX
                            && (float) mouseY > (float) y + 1.5F && (float) y + 1.5F + button.width > (float) mouseY) {
                        state = new HoverTooltip((float) button.width, (float) button.height);
                        button.hoverTooltip = state;
                        state.draw(tooltip, mouseX, mouseY, buttonAlpha, button.x, button.y, 500);
                    }
                }
            }
        }

        // name line
        Minecraft mc = Minecraft.getMinecraft();
        Session session = mc.session;
        boolean current = session != null && session.getUsername().equals(this.username);
        String prefix = current ? ChatColor.BOLD.toString() : "";
        String name = prefix + com.adjustclient.module.impl.render.streamer.StreamerModule.INSTANCE.maskNameIfEnabled(this.username);  // Streamer module hides the name when enabled
        FontRenderer fr = mc.fontRendererObj;
        int nameColor = current ? 0xFFFFFFFF : new Color(213, 213, 212, 255).getRGB();
        fr.drawString(name, (float) (x + 38), (float) (y + 3), nameColor, true, 1.0F);

        // "last used" line
        String lastUsed;
        if (this.lastLogin != null) {
            lastUsed = TimeUtil.timeAgo(this.lastLogin.time, " ago") + " by " + ChatColor.GREEN + this.lastLogin.user;
        } else {
            lastUsed = "new";
        }

        // ban line
        Long expiry = this.bans.getExpiry(ServerType.HYPIXEL);
        float lineY = (float) (y + 14);
        fr.drawString(lastUsed, (float) (x + 38), lineY, new Color(180, 180, 180, 255).getRGB(), true, 0.5F);
        if (expiry != null) {
            String banText;
            int banColor;
            if (BanTracker.isUnbanned(expiry)) {
                banText = "unbanned";
                banColor = new Color(88, 152, 72, 255).getRGB();
            } else {
                if (expiry == -1L) {
                    banText = "permanently banned";
                } else {
                    banText = TimeUtil.formatDuration(expiry - System.currentTimeMillis(), false);     // remaining time, formatted
                }
                banColor = fr.getColorCode(ChatColor.RED.formattingCode);
            }
            fr.drawString(banText, (float) (x + 38), (float) (y + 20), banColor, true, 0.5F);
        }

        // skin head (32x32) with hover tooltip = account type name
        if (this.head != null) {
            this.head.draw((float) (x + 2), (float) y);
        }
        if (this.headTooltip == null && (float) mouseX > (float) (x + 2) && (float) (x + 2) + 32.0F > (float) mouseX
                && (float) mouseY > (float) y && (float) y + 32.0F > (float) mouseY) {
            this.headTooltip = new HoverTooltip(32.0F, 32.0F);
        }
        if (this.headTooltip != null) {
            this.headTooltip.draw(getType().displayName, mouseX, mouseY, Math.min(255, Math.max(alpha, 150)), (float) (x + 2), (float) y, 240);
        }
    }

    /** Stores the UUID and (re)creates the skin head for (username, uuid) unless the current head already matches. */
    public void setUuid(String uuid) {
        this.uuid = uuid;
        if (this.head == null) {
            this.head = new AccountHead(this.username, uuid);                   // sets Timer(lastMs = now)
            return;
        }
        if (!this.head.isFor(this.username, uuid)) {
            this.head = new AccountHead(this.username, uuid);
        }
    }

    protected void initButtons() {
        this.buttons = List.of(
                this.deleteButton,
                new RefreshAccountButton(this, new ResourceLocation("adjust", "textures/account/refresh.png")),
                new ViewAccountButton(this, new ResourceLocation("adjust", "textures/account/view.png")));
    }

    public byte[] i() {
        try { return serialize(); }
        catch (IOException error) { throw new java.io.UncheckedIOException(error); }
    }

    public void delete() {
        LocalAccountStore.INSTANCE.remove(manager(), repository, new AccountEntry[]{this});
    }

    public void syncBans() {
        if (this.bans.dirty.isEmpty()) return;
        this.bans.dirty.clear();
        LocalAccountStore.INSTANCE.save(); // Locally observed bans only; no server-side flags or pushes.
    }

    private static AccountManagerScreen manager() {
        return (AccountManagerScreen) Adjust.INSTANCE.accountManager;
    }

    // Backend request plumbing replaced by LocalAccountStore (user decision 2026-11-09).
}
