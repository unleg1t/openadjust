package com.adjustclient.modules.impl.player.guessthebuild;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.ChatAutocompleteEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.LowLevelPacketSendEvent;
import com.adjustclient.core.events.Render2DEvent;
import com.adjustclient.core.events.UserInputVerificationEvent;
import com.adjustclient.utilities.hypixel.game.GuessTheBuildGameState;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.managers.party.ui.PartyPanel;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.CommandCompleter;
import com.adjustclient.utilities.Timer;
import java.util.function.BooleanSupplier;
import net.minecraft.network.play.client.C01PacketChatMessage;

public final class GuessTheBuildModule extends Module {
    public static GuessTheBuildModule INSTANCE;
    public final BooleanSetting tabComplete;
    public final BooleanSetting autoGuessEnabled;
    public final NumberSetting autoGuessLimit;
    public final BooleanSetting shareTheme;
    public final GuessTheBuildHud hud;
    public final Timer guessTimer = new Timer();
    public String sharedTheme;
    public boolean themeShared;

    public GuessTheBuildModule() {
        super("Guess The Build", Category.PLAYER);
        INSTANCE = this;
        tabComplete = new BooleanSetting(this, "Tab complete", true);
        ExpandSetting autoGuess = new ExpandSetting(this, "Auto guess", () -> false);
        autoGuessEnabled = new BooleanSetting(autoGuess, "Enabled", false, "Yes", "No");
        autoGuessLimit = new NumberSetting(autoGuess, "Limit", 5, 1, 51, 1);
        autoGuessLimit.setDescription("Auto guess won't activate if there are more themes than the specified value.");
        autoGuess.enabledPredicate = new AutoGuessCondition(this);
        autoGuessLimit.formatter(setting -> {
            return setting.getInt() == setting.getMax().intValue() ? "Disabled" : Integer.toString(setting.getInt());
        });
        shareTheme = new BooleanSetting(this, "Share theme", false);
        shareTheme.setDescription("Automatically shares your theme with Adjust party members.");
        hud = new GuessTheBuildHud(this);
    }

    @EventHandler
    public void onTabComplete(ChatAutocompleteEvent event) {
        if (!isEnabled() || !tabComplete.isEnabled() || !(Adjust.gameStateTracker.state instanceof GuessTheBuildGameState)) return;
        GuessTheBuildGameState state = (GuessTheBuildGameState) Adjust.gameStateTracker.state;
        String[] themes = state.matches.stream().map(value -> value.toLowerCase())
                .toArray(size -> new String[size]);
        event.completions = CommandCompleter.complete(event.text,
                CommandCompleter.MATCHING.apply(themes));
    }

    @EventHandler
    public void onUpdate(UserInputVerificationEvent event) {
        if (!isEnabled() || !(Adjust.gameStateTracker.state instanceof GuessTheBuildGameState state)) return;
        if (state.field56 != null && state.field56.equals(mc.getSession().getUsername())) {
            if (!shareTheme.isEnabled() || themeShared) return;
            if (state.hint != null && !state.hint.contains("_")) {
                PartyPanel party = Adjust.INSTANCE.screenStack.partyService.party;
                if (party != null) {
                    new NotificationBuilder(getName())
                            .addText("Shared your theme with " + party.members.size() + " party members", 0xFFBEBCBD)
                            .show();
                    themeShared = true;
                }
            }
            return;
        }
        if (sharedTheme != null && shareTheme.isEnabled()) {
            if (!guessTimer.hasElapsed(3000L)) return;
            Adjust.chatMessageSender.sendMessage(sharedTheme, false);
            guessTimer.reset();
            sharedTheme = null;
            return;
        }
        if (state.matches.isEmpty() || !autoGuessEnabled.isEnabled()) return;
        int limit = autoGuessLimit.getInt();
        if (limit < 51 && limit > state.matches.size()) return;
        if (!guessTimer.hasElapsed(3000L)) return;
        String guess = state.matches.getFirst();
        Adjust.chatMessageSender.sendMessage(guess, false);
        state.guesses.add(guess);
        guessTimer.reset();
    }

    @SuppressWarnings("unused")
    private void vendorShareTheme(GuessTheBuildGameState state) {
        PartyPanel party = Adjust.INSTANCE.screenStack.partyService.party;
        if (party == null) return;
        GuessTheBuildThemePayload payload = new GuessTheBuildThemePayload(mc.getSession().getUsername(), state.hint);
        net.minecraft.network.PacketBuffer buffer = new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
        payload.write(buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        for (var member : party.members) {
            new com.adjustclient.utilities.backend.api.request.client.SendCustomPayloadRequest(member.user, 1, bytes)
                    .toPacket()
                    .sendAsync(null);
        }
        new NotificationBuilder(getName())
                .addText("Shared your theme with " + party.members.size() + " party members", 0xFFBEBCBD)
                .show();
        themeShared = true;
    }

    @EventHandler
    public void onPacket(LowLevelPacketSendEvent event) {
        if (isEnabled() && event.packet instanceof C01PacketChatMessage) guessTimer.reset();
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (isEnabled() && Adjust.gameStateTracker.state instanceof GuessTheBuildGameState) {
            hud.render(event, (GuessTheBuildGameState) Adjust.gameStateTracker.state);
        }
    }

    @Override
    public void onDisable() {
        themeShared = false;
        sharedTheme = null;
    }

    public static final class AutoGuessCondition implements BooleanSupplier {
        public final GuessTheBuildModule module;

        public AutoGuessCondition(GuessTheBuildModule module) {
            this.module = module;
        }

        @Override
        public boolean getAsBoolean() {
            return module.autoGuessEnabled.isEnabled();
        }
    }
}
