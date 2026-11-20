package com.adjustclient.modules.impl.player.spammer;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.UserInputVerificationEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.Cooldown;
import com.adjustclient.utilities.RandomUtil;
import java.util.Arrays;
import java.util.List;

public final class SpammerModule extends Module {
    public static SpammerModule INSTANCE;
    public final ModeSetting<SpammerMode> mode;
    public final ModeSetting<Order> order;
    public final NumberSetting delay;
    public final StringSetting messages;
    private final Cooldown cooldown;
    private int messageIndex;

    public SpammerModule() {
        super("Spammer", Category.PLAYER);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", SpammerMode.NORMAL, SpammerMode.values());
        order = new ModeSetting<>(this, "Order", Order.SEQUENTIAL,
                () -> mode.getValue() != SpammerMode.NORMAL, Order.values());
        delay = new NumberSetting(this, "Delay", 1.0, 0.3, 120.0, 0.1,
                () -> mode.getValue() != SpammerMode.NORMAL);
        messages = new StringSetting(this, "Messages", Arrays.asList(
                "Nefarious called us retarded for spending Intent Store's funds on WeedHack Premium AI development.",
                "We killed him.",
                "WeedHack Premium Beta, has hired world famous bypass developer The_Tri11iona1re,",
                "to develop the first ever interdimensional movement disabled for the Hypixel Network.",
                "This allows all WeedHack Premium Beta Members,",
                "to abuse the Nano-Particle Atom Biosphere Velocity Packet",
                "And gain full unpatched Galaxy Strafe.",
                "We are WeedHack. Simon, We are coming for your soul."), 100, 100,
                () -> mode.getValue() != SpammerMode.NORMAL);
        cooldown = new Cooldown(-1L);
        notifyToggle = false;
    }

    private int randomMessageIndex() {
        int size = messages.getValue().size();
        return size == 1 ? 0 : RandomUtil.RANDOM.nextInt(size - 1);
    }

    @EventHandler public void onTick(UserInputVerificationEvent event) {
        if (!isEnabled()) return;
        if (cooldown.durationMs >= 0 && !cooldown.isExpired()) return;
        if (mode.getValue() == SpammerMode.NORMAL) {
            List<String> lines = messages.getValue();
            String line;
            switch (order.getValue()) {
                case SEQUENTIAL:
                    if (messageIndex >= lines.size()) messageIndex = lines.size() - 1;
                    line = lines.get(messageIndex++);
                    if (messageIndex > lines.size() - 1) messageIndex = 0;
                    break;
                case RANDOM:
                    int index = randomMessageIndex();
                    if (lines.size() >= 2) {
                        while (index == messageIndex) index = randomMessageIndex();
                    }
                    messageIndex = index;
                    line = lines.get(index);
                    break;
                default: throw new RuntimeException("Unimplemented message order");
            }
            Adjust.chatMessageSender.sendMessage(line, true);
        }
        rollDelay();
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }

    private void rollDelay() {
        double milliseconds = delay.getValue().doubleValue() * 1000.0;
        double minimum = Math.max(0.0, milliseconds - 110.0);
        int duration = (int) RandomUtil.nextGaussian(minimum, milliseconds + 155.0);
        cooldown.timer.reset();
        cooldown.durationMs = duration;
    }

    public enum SpammerMode implements ModeOption {
        NORMAL("Normal");
        private final String displayName;
        SpammerMode(String displayName) { this.displayName = displayName; }
        public String getName() { return displayName; }
    }

    public enum Order implements ModeOption {
        SEQUENTIAL("Sequential"), RANDOM("Random");
        private final String displayName;
        Order(String displayName) { this.displayName = displayName; }
        public String getName() { return displayName; }
    }
}
