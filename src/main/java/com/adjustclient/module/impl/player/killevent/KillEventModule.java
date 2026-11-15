package com.adjustclient.module.impl.player.killevent;
import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.hypixel.game.SkyWarsGameState;
import com.adjustclient.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.hypixel.api.model.player.HypixelRank;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.module.impl.exploit.playerdetect.hackerdetect.HackerDetector;
import com.adjustclient.module.impl.player.killevent.effect.*;
import com.adjustclient.setting.*;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.Cooldown;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
public final class KillEventModule extends Module {
    public static KillEventModule INSTANCE;
    public final ExpandSetting sendMessage;
    public final BooleanSetting sendMessageEnabled;
    public final BooleanSetting onlyOnHackers;
    public final ExpandSetting messages;
    public final StringSetting normalMessages;
    public final StringSetting hackerMessages;
    public final ExpandSetting effect;
    public final BooleanSetting effectEnabled;
    public final ModeSetting<EffectMode> effectMode;
    public final Cooldown messageCooldown;
    public String lastMessage;
    public final List<KillEffect> effects;
    public KillEventModule() {
        super("Kill Event", Category.PLAYER);
        INSTANCE = this;
        sendMessage = new ExpandSetting(this, "Send message");
        sendMessage.enabledPredicate = new SendMessageCondition(this);
        sendMessageEnabled = new BooleanSetting(sendMessage, "Enabled", false, "Yes", "No");
        onlyOnHackers = new BooleanSetting(sendMessage, "Only on hackers", false, "On", "Off");
        messages = new ExpandSetting(sendMessage, "Messages");
        normalMessages = new StringSetting(messages, "Normal", Arrays.asList(
                "If they ever resume sending chimps into space, maybe %player will make an actual contribution.",
                "Donate to the Parkinson's Support Group for a chance to improve %player's aim.",
                "Do something useful for the world %player and invent a vaccine for your stupidity.",
                "Do you always suck this much %player or is this a special occasion?",
                "Hey %player, here's some advice: quit Minecraft.",
                "The way you swing at the air while I'm hopping around you is fascinating %player.",
                "Two wrongs don't make a right, take %player's parents as an example.",
                "For the people who believe in evolution, show them %player.",
                "Just wondering %player, do you even try to win?",
                "Wanna laugh? Let's take a look at %player's life.",
                "Try selling that face as a Halloween mask %player.",
                "%player is the example for natures worst disasters.",
                "I have mixed opinions about the worst accidents ever, people say Chernobyl, I say %player.",
                "Like your aim, you're a living joke %player.",
                "You can't even catch up to me hopping around you %player, how were you the winning sperm?",
                "How can you love nature after what it did to %player?",
                "Please %player, never ever have children.",
                "Donate some condoms to your neighbors %player, don't let humanity make the same mistake twice.",
                "Is your life somehow tied to your ability to aim %player? They both don't exist.",
                "Do you pay rent for your dog house %player, or do you live for free in it?",
                "I could so see %player being the face of condoms.",
                "I've seen you try %player, please just consider cheating.",
                "Go put on some lipstick to mask that hideous face %player.",
                "%player is inspiration for the 4th autism spectrum disorder.",
                "Are your parents that ugly or just you %player?",
                "If there's an exhibition for losers, %player would finally be in something.",
                "Are you this useless in real life too %player?",
                "What do %player's life and aim have in common? They're both a joke!",
                "If %player is really what's considered average, extinction is the only way to solve our problems.",
                "I'd make fun of %player, but I'd just be copying everyone who saw him.",
                "You're so annoying %player, you could be hired as a hitman. All you'd have to do is talk.",
                "I think that when you pick up a pair of chopsticks, it's a reunion dinner."), 100, 100,
                () -> {
                    return onlyOnHackers.isEnabled();
                });
        hackerMessages = new StringSetting(messages, "Hacker", Arrays.asList(
                "Nice client %player, where'd you get it?",
                "%player your client just got %health hearted, embarrassing."), 100, 100, null);
        effect = new ExpandSetting(this, "Effect");
        effect.enabledPredicate = new EffectGroupCondition(this);
        effectEnabled = new BooleanSetting(effect, "Enabled", false, "Yes", "No");
        effectMode = new ModeSetting<>(effect, "Mode", EffectMode.BLOOD, EffectMode.values());
        messageCooldown = new Cooldown(-1L);
        effects = new ArrayList<>();
    }
    @EventHandler public void onUpdate(com.adjustclient.event.PreClientTickEvent event) {
        if (!isEnabled() && effects.isEmpty()) return;
        if (mc.thePlayer == null || mc.theWorld == null) { effects.clear(); return; }
        for (Iterator<KillEffect> iterator = effects.iterator(); iterator.hasNext();) {
            KillEffect effect = iterator.next();
            effect.play();
            if (effect.finished) iterator.remove();
        }
    }
    @Override public void onDisable() {
        messageCooldown.timer.reset();
        messageCooldown.durationMs = -1L;
        lastMessage = null;
    }
    @EventHandler public void onPlayerKill(PlayerKillEvent event) {
        if (!isEnabled() || !event.isLocalPlayer()) return;
        TrackedFakePlayer victim = ServerTracker.INSTANCE.entityTracker.getByName(event.victim);
        HypixelGameState game = Adjust.gameStateTracker.state;
        if (sendMessageEnabled.isEnabled() && (messageCooldown.durationMs < 0 || messageCooldown.isExpired())
                && !game.isSpectator() && game.startTimer != null && !game.ended) {
            boolean cheater = victim != null && HackerDetector.isCheater(victim.entityId);
            if (!cheater && onlyOnHackers.isEnabled()) return;
            String health = RandomUtil.format(Math.round(mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) * 0.5F);
            String message = chooseMessage(cheater ? hackerMessages : normalMessages)
                    .replaceAll("%player", event.victim).replaceAll("%health", health);
            if (!message.isEmpty()) {
                var tracker = ServerTracker.INSTANCE.serverState;
                if (tracker != null && tracker.antiCheatModel != null
                        && tracker.antiCheatModel.serverType == ServerType.HYPIXEL) {
                    boolean shout = game instanceof BedWarsGameState && ((BedWarsGameState)game).isLargeGame()
                            || game instanceof SkyWarsGameState && ((SkyWarsGameState)game).isLargeGame();
                    if (shout) {
                        messageCooldown.timer.reset();
                        messageCooldown.durationMs = game instanceof BedWarsGameState
                                ? ((BedWarsGameState)game).getTrackingDelay() : ((SkyWarsGameState)game).getTrackingDelay();
                        message = "/shout " + message;
                    } else {
                        var profile = Adjust.hypixelApiManager.getCachedPlayer(mc.getSession().getProfile().getId());
                        if (profile == null || profile.getEffectiveRank() == null
                                || HypixelRank.valueOf(profile.getEffectiveRank()) == HypixelRank.NONE) {
                            messageCooldown.timer.reset();
                            messageCooldown.durationMs = 3000L;
                        }
                        message = "/ac " + message;
                    }
                }
                Adjust.chatMessageSender.sendMessage(message, false);
                event.flags |= 2;
            }
        }
        if (effectEnabled.isEnabled() && victim != null) {
            EntityLivingBase entity = (EntityLivingBase)victim.entity;
            mc.addScheduledTask((java.util.concurrent.Callable<Object>)() -> {
                effects.add(effectMode.getValue().factory.apply(entity));
                return null;
            });
        }
    }
    @EventHandler public void onWorldChange(WorldResetEvent event) {
        messageCooldown.timer.reset();
        messageCooldown.durationMs = -1L;
        effects.clear();
    }
    public String chooseMessage(StringSetting setting) {
        List<String> candidates = setting.getValue().stream().filter(text -> {
            return setting.getValue().size() < 2 || !text.equals(lastMessage);
        }).toList();
        int index = setting.getValue().size() == 1 ? 0 : RandomUtil.nextInt(setting.getValue().size() - 1);
        lastMessage = candidates.get(index);
        return lastMessage;
    }
    public enum EffectMode implements ModeOption {
        BLOOD("Blood", entity -> new BloodEffect(entity)),
        LIGHTNING_BOLT("Lightning Bolt", entity -> new LightningEffect(entity));
        public final String name;
        public final Function<EntityLivingBase, KillEffect> factory;
        EffectMode(String name, Function<EntityLivingBase, KillEffect> factory) { this.name = name; this.factory = factory; }
        public String getName() { return name; }
    }
    public static final class SendMessageCondition implements BooleanSupplier {
        public final KillEventModule module;
        public SendMessageCondition(KillEventModule module) { this.module = module; }
        public boolean getAsBoolean() { return module.sendMessageEnabled.isEnabled(); }
    }
    public static final class EffectGroupCondition implements BooleanSupplier {
        public final KillEventModule module;
        public EffectGroupCondition(KillEventModule module) { this.module = module; }
        public boolean getAsBoolean() { return module.effectEnabled.isEnabled(); }
    }
}
