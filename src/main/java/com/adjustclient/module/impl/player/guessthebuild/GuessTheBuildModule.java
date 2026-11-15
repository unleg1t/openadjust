package com.adjustclient.module.impl.player.guessthebuild;
import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.setting.*;
import com.adjustclient.hypixel.game.GuessTheBuildGameState;
import com.adjustclient.util.*;
import java.util.function.BooleanSupplier;
import net.minecraft.network.play.client.C01PacketChatMessage;
public final class GuessTheBuildModule extends Module {
    public static GuessTheBuildModule INSTANCE;
    public final BooleanSetting tabComplete;
    public final BooleanSetting autoGuessEnabled;
    public final NumberSetting autoGuessLimit;
    public final GuessTheBuildHud hud;
    public final Timer guessTimer = new Timer();
    public GuessTheBuildModule() {
        super("Guess The Build", Category.PLAYER);
        INSTANCE = this;
        tabComplete = new BooleanSetting(this, "Tab complete", true);
        ExpandSetting autoGuess = new ExpandSetting(this, "Auto guess",
                () -> false);
        autoGuessEnabled = new BooleanSetting(autoGuess, "Enabled", false, "Yes", "No");
        autoGuessLimit = new NumberSetting(autoGuess, "Limit", 5, 1, 51, 1);
        autoGuessLimit.setDescription("Auto guess won't activate if there are more themes than the specified value.");
        autoGuess.enabledPredicate = new AutoGuessCondition(this);
        autoGuessLimit.formatter(setting -> {
            return setting.getInt() == setting.getMax().intValue() ? "Disabled" : Integer.toString(setting.getInt());
        });
        hud = new GuessTheBuildHud(this);
    }
    @EventHandler public void onTabComplete(com.adjustclient.event.ChatAutocompleteEvent event) {
        if (!isEnabled() || !tabComplete.isEnabled() || !(Adjust.gameStateTracker.state instanceof GuessTheBuildGameState)) return;
        GuessTheBuildGameState state = (GuessTheBuildGameState)Adjust.gameStateTracker.state;
        String[] themes = state.matches.stream().map(value -> value.toLowerCase())
                .toArray(size -> new String[size]);
        event.completions = CommandCompleter.complete(event.text,
                CommandCompleter.MATCHING.apply(themes));
    }
    @EventHandler public void onUpdate(com.adjustclient.event.UserInputVerificationEvent event) {
        if (!isEnabled() || !(Adjust.gameStateTracker.state instanceof GuessTheBuildGameState)) return;
        GuessTheBuildGameState state = (GuessTheBuildGameState)Adjust.gameStateTracker.state;
        if (state.matches.isEmpty() || !autoGuessEnabled.isEnabled()) return;
        int limit = autoGuessLimit.getInt();
        if (limit < 51 && limit > state.matches.size()) return;
        if (!guessTimer.hasElapsed(3000L)) return;
        String guess = state.matches.getFirst();
        Adjust.chatMessageSender.sendMessage(guess, false);
        state.guesses.add(guess);
        guessTimer.reset();
    }
    @EventHandler public void onPacket(LowLevelPacketSendEvent event) {
        if (isEnabled() && event.packet instanceof C01PacketChatMessage) guessTimer.reset();
    }
    @EventHandler public void onRender2D(Render2DEvent event) {
        if (isEnabled() && Adjust.gameStateTracker.state instanceof GuessTheBuildGameState)
            hud.render(event, (GuessTheBuildGameState)Adjust.gameStateTracker.state);
    }
    public static final class AutoGuessCondition implements BooleanSupplier {
        public final GuessTheBuildModule module;
        public AutoGuessCondition(GuessTheBuildModule module) { this.module = module; }
        public boolean getAsBoolean() { return module.autoGuessEnabled.isEnabled(); }
    }
}
