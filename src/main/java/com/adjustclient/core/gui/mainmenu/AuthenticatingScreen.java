package com.adjustclient.core.gui.mainmenu;

import com.adjustclient.Adjust;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.animation.Easing;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.utilities.Timer;
import java.awt.Color;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatColor;
import com.adjustclient.managers.config.ConfigurationService;

public final class AuthenticatingScreen extends GuiScreen {
    public static boolean fadingLetters;
    public static boolean authenticationFinished;
    private Timer timer;
    public static String message;
    public static Animation[] letters;
    public static String tip;
    public static Animation fade;
    public static Animation textFade;
    public static String[] messages;
    public static boolean advance;
    public static boolean ready;
    public static boolean changing;
    public static int messageIndex;

    static {
        fade = new Animation(Easing.LINEAR, 130L);
        textFade = new Animation(Easing.LINEAR, 200L);
        messages = new String[] {"Authenticating to Adjust", "Waiting to receive data from Hydra",
                "Authentication is taking too long - try restarting Adjust"};
        advance = false;
        ready = false;
        String[] tips = {
            "Use *^cfg loadbinds <cfg>* to load keybindings from a config",
            "Use *^cfg loadvisuals <cfg>* to load visual settings from a config",
            "Middle-click configurations or modules in the click GUI to bind them",
            "Use *^cfg reset* to reset every setting to default",
            "Start your chat messages with *^^* to talk in IRC",
            "Clicking highlighted text in the changelog will bring you to the change",
            "Use *^stats <ign>* to view Hypixel statistics of any player",
            "Make sure to set your API keys (in HUD) to view more statistics in the player list",
            "Found a bug or crashed? Report it in the Discord!",
            "Want to improve Adjust? Give us feedback and suggest new ideas!",
            "You can move HUD windows by going in a GUI and clicking on their title",
            "Don't forget to smile once in a while",
            "Use *^p <username>* to invite other Adjust users to a party",
            "Use *^msg <username> <message>* to message other Adjust users",
            "Overwhelmed with messages? You can manage your privacy settings from the HUD module (called Invites)",
            "Use *^block <username>* to block other Adjust users from interacting with you",
            "Seamlessly share alts by inviting friends to your account repository",
            "You can share configs to your friends through *^cfg send <cfg> <user>* or by using the Click GUI",
            "Use *^fr* to manage your Adjust friend list",
            "Hide modules in the HUD toggled list by clicking on the eye next to their name in the Click GUI",
            "In an Adjust party, you can type *{ign}* to quickly share your Minecraft username",
            "Download user-made configurations by clicking on the world icon next to Configurations in the Click GUI"
        };
        tip = ChatColor.WHITE.toString() + ChatColor.BOLD + "Tip: "
                + ChatColor.RESET + tips[com.adjustclient.utilities.RandomUtil.RANDOM.nextInt(22)];
        if (Math.random() >= 0.999D) {
            tip = ChatColor.RESET + "This is a special message with a chance of "
                    + ChatColor.WHITE + ChatColor.BOLD.toString() + "1 in 1,000"
                    + ChatColor.RESET + "! Congratulations!";
        }
        while (tip.indexOf('*') != -1) {
            tip = tip.replaceFirst("\\*", ChatColor.WHITE.toString());
            tip = tip.replaceFirst("\\*", ChatColor.RESET.toString());
        }
    }

    public AuthenticatingScreen() {
        if (!changing) {
            textFade.value = 1.0F;
            textFade.startedAt = System.currentTimeMillis();
            textFade.from = textFade.value;
            textFade.finished = false;
        }
        if (!advance) {
            advance = true;
            advanceMessage();
        }
    }

    @Override public void updateScreen() {
    }

    public static void setMessage(String text) {
        message = text;
        letters = new Animation[text.length()];
        changing = true;
        for (int i = 0; i < letters.length; i++) {
            letters[i] = new Animation(Easing.EASE_OUT_SINE, 75L * i + 100L);
            letters[i].value = 1.0F;
            letters[i].startedAt = System.currentTimeMillis();
            letters[i].from = letters[i].value;
            letters[i].finished = false;
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Color background = new Color(160, 160, 160);
        VoronoiBackground.draw(this, mouseX, mouseY, background);
        if (VoronoiBackground.transition == null || VoronoiBackground.transition.finished)
            VoronoiBackground.previousColor = background;
        if (timer == null) timer = new Timer();
        NvgFontRenderer font = Fonts.TAHOMA_12;
        float x = width / 2.0F - font.getStringWidth(message) / 2.0F;
        float y = height / 2.35F + 15.0F;
        for (int i = 0; i < message.length(); i++) {
            String character = String.valueOf(message.charAt(i));
            Color color = ColorUtil.blend(new Color(88, 88, 88), new Color(200, 200, 200, 133),
                    Math.max(0.0F, Math.min(1.0F, letters[i].value)));
            int alpha = Math.max(0, Math.min(color.getAlpha(), (int) (255.0F * textFade.value)));
            int argb = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
            font.renderString(character, x + 0.5F, y + 0.5F, argb, true);
            font.renderString(character, x, y, argb, false);
            x += font.getStringWidth(character);
        }
        String text = getTip();
        font.drawString(text, width / 2.0F - font.getStringWidth(text) * 0.5F, height / 2.35F + 35.0F, new Color(200, 200, 200, (int) (getFade() * 211.0F)).getRGB());
        AdjustMainMenu.openingReady = true;
        for (Animation letter : letters) letter.update(fadingLetters ? 0.0F : 1.0F);
        if (letters[letters.length - 1].finished) {
            boolean wasFading = fadingLetters;
            fadingLetters = !wasFading;
            if (!wasFading) {
                if (ready) {
                    advanceMessage();
                    ready = false;
                } else ready = true;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        boolean offline = Adjust.hydraConnection == null || !Adjust.hydraConnection.authenticated;
        if (offline || (ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived)) {
            textFade.update(0.0F);
            if ((offline || (ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived)) && textFade.value == 0.0F) {
                mc.displayGuiScreen(new AdjustMainMenu());
                VoronoiBackground.reset();
                authenticationFinished = true;
            }
        }
    }

    @Override public void initGui() {
        VoronoiBackground.resize(width, height);
    }

    public static float getFade() {
        return fade.update(1.0F);
    }

    public static String getTip() {
        return tip.replace("^^", String.valueOf(com.adjustclient.managers.config.user.UserConfiguration.IRC_PREFIX.getChar()))
                .replace("^", String.valueOf(com.adjustclient.managers.config.user.UserConfiguration.COMMAND_PREFIX.getChar()));
    }

    public static void advanceMessage() {
        if (messageIndex >= messages.length) return;
        setMessage(messages[messageIndex]);
        messageIndex++;
    }
}
