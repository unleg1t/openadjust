package com.adjustclient.core.gui.mainmenu;

import com.adjustclient.Adjust;
import com.adjustclient.core.changelog.Changelog;
import com.adjustclient.core.changelog.ChangelogPreview;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rendering.ColorUtil;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.rendering.font.TextComponent;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.managers.poll.PollService;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.gui.dialog.AdjustButton;
import com.adjustclient.core.gui.dialog.AlphaModifier;
import com.adjustclient.core.gui.dialog.DialogBuilder;
import com.adjustclient.core.gui.registration.RecoveryKeyScreen;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.Vec2f;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.ChatColor;
import com.adjustclient.utilities.backend.api.response.ResponseStatus;
import com.adjustclient.utilities.backend.transport.HydraMessageDispatcher;
import com.adjustclient.managers.config.ConfigurationService;
import com.adjustclient.managers.config.user.UserConfiguration;

public class AdjustMainMenu extends GuiScreen implements AlphaModifier {
    public static boolean clientUiAllowed = true;
    public static boolean windowActive = true;
    public static boolean openingReady = true;
    public static boolean changelogOpened;
    public static boolean menuActionPending;
    private static boolean initialTransitionSeeded;
    private boolean openingSplash;
    public boolean interactive;
    private String originalSplash;
    private String splash;
    private ChangelogPreview changelog;
    private final List<MenuParticle> particles = new ArrayList<>();
    private int particleTicks;
    public static Random random;
    public static ResourceLocation splashes;
    public static ResourceLocation background;
    public static Timer notificationTimer;
    public static ResourceLocation santa;
    public static boolean initialized;
    private static final com.adjustclient.core.rendering.animation.Animation OFFLINE_SPLASH_FADE =
            new com.adjustclient.core.rendering.animation.Animation(com.adjustclient.core.rendering.animation.Easing.LINEAR, 130L);
    private static final String[] OFFLINE_TIPS = {
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
    private static String offlineTipTemplate;

    static {
        random = new Random();
        splashes = new ResourceLocation("texts/splashes.txt");
        background = new ResourceLocation("adjust", "textures/background.png");
        notificationTimer = new Timer(0L);
        initialized = false;
        santa = new ResourceLocation("adjust", "textures/menu/santa.png");
    }

    public AdjustMainMenu() {
        changelog = new ChangelogPreview();
        String selected = "missingno";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Minecraft.getMinecraft().getResourceManager().getResource(splashes).getInputStream(),
                StandardCharsets.UTF_8))) {
            List<String> choices = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) choices.add(line);
            }
            if (!choices.isEmpty()) {
                for (int tries = 0; tries < 12; tries++) {
                    selected = net.minecraft.util.StringUtils.stripControlCodes(
                            choices.get(random.nextInt(choices.size())));
                    if (selected.hashCode() != 125780783) break;
                }
            }
        } catch (java.io.IOException ignored) {
        }
        originalSplash = selected;
        splash = selected;
    }

    @Override public void updateScreen() {
        for (GuiButton button : buttonList) button.enabled = true;
        if (UserConfiguration.changelogUnread && !changelogOpened) {
            int changes = Changelog.initialUnreadCount == null ? 0 : Changelog.initialUnreadCount;
            ChatMessage text = new ChatMessage();
            text.append("You just updated to version ");
            text.bold = true;
            text.append(Changelog.latest.title);
            text.append("! There " + (changes == 1 ? "is " : "are "));
            text.bold = true;
            text.append(Integer.toString(changes));
            text.append(" new change" + (changes == 1 ? "" : "s") + ". Would you like to view the changelog?");
            DialogBuilder dialog = new DialogBuilder("Update Notification");
            dialog.message = text;
            dialog.addButton("Later", null, DialogBuilder.ALWAYS_ENABLED);
            dialog.addButton("View Changelog",
                    screen -> {
                        java.util.Objects.requireNonNull(changelog);
                        VoronoiBackground.close();
                    }, DialogBuilder.ALWAYS_ENABLED);
            dialog.build();
            changelogOpened = true;
        }
        if (!initialized) initialized = true;
        if (!clientUiAllowed) return;
        particles.removeIf(
                particle -> particle.y > height + 0.75F);
        if (ServiceUtil.resolveHolidayTheme() == com.adjustclient.utilities.HolidayTheme.CHRISTMAS
                && ++particleTicks >= 5) {
            MenuParticle particle = new MenuParticle();
            particle.x = org.apache.commons.lang3.RandomUtils.nextFloat(0.75F, Math.max(0.75F, width - 0.75F));
            particle.y = -0.75F;
            particles.add(particle);
            particleTicks = 0;
        }
    }

    @Override public void onGuiClosed() {
        openingReady = false;
    }

    public static String formatDuration(long millis) {
        if (millis >= 0x496CEBB801L) return null;
        if (millis < 1501L) return "";
        long seconds = millis / 1000L;
        if (seconds < 61L) return seconds + " second" + (seconds == 1L ? "" : "s");
        long minutes = seconds / 60L;
        if (minutes < 61L) return minutes + " minute" + (minutes == 1L ? "" : "s");
        long hours = minutes / 60L;
        if (hours < 25L) return hours + " hour" + (hours == 1L ? "" : "s");
        long days = hours / 24L;
        if (days >= 31L) {
            long months = days / 30L;
            long remainder = days - months * 30L;
            return months + " month" + (months == 1L ? "" : "s")
                    + (remainder > 0L ? " and " + remainder + " day" + (remainder == 1L ? "" : "s") : "");
        }
        if (days < 15L) return days + " day" + (days == 1L ? "" : "s");
        long weeks = days / 7L;
        long remainder = days - weeks * 7L;
        return weeks + " weeks" + (remainder > 0L
                ? " and " + remainder + " day" + (remainder == 1L ? "" : "s") : "");
    }

    private void drawTitle(Vec2f mouse) {
        float centerX = width * 0.5F;
        float y = height / 2.35F - 7.0F;
        float dx = mouse.x - centerX;
        float dy = mouse.y - (y - 22.0F);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        int alpha = Math.min(230, Math.max(-50, (int) (190.0F - distance * 0.4F)) + 190);
        if (VoronoiBackground.transition != null) alpha = Math.min(alpha, (int) (VoronoiBackground.update() * 255.0F));
        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        accent = ColorUtil.withAlpha(accent, Math.max(0, alpha));
        NvgFontRenderer initial = Fonts.TAHOMA_22;
        NvgFontRenderer rest = Fonts.TAHOMA_20;
        String initialText = ChatColor.BOLD.toString() + "A";
        float titleWidth = initial.getStringWidth(initialText) + rest.getStringWidth("djust");
        float x = centerX - titleWidth / 2.0F;
        x = initial.drawString(initialText, x, y - 7.0F, accent.getRGB());
        rest.drawString("djust", x, y - 6.0F, new Color(210, 205, 209, Math.max(0, alpha)).getRGB());
    }

    @Override protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case 1:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 2:
                mc.displayGuiScreen(Adjust.INSTANCE.accountManager);
                break;
            case 3:
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case 4:
                DialogBuilder dialog = new DialogBuilder("Are you sure?");
                dialog.message.append("Exit Minecraft?");
                dialog.addButton("Exit",
                        screen -> mc.shutdown(), DialogBuilder.ALWAYS_ENABLED);
                dialog.addButton("Cancel", null, DialogBuilder.ALWAYS_ENABLED);
                dialog.build();
                break;
            case 5:
                Adjust.INSTANCE.screenStack.openFriendsScreen();
                break;
            default:
                break;
        }
    }

    @Override protected void keyTyped(char typedChar, int keyCode) {
    }

    private void drawCornerCircles(Vec2f mouse, ScaledResolution resolution) {
        float dx = mouse.x + 1.0F;
        float dy = mouse.y - 3.0F;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        int alpha = Math.min(110, Math.max(-130, (int) (190.0F - distance * 1.5F)) + 190);
        drawCircle(16, new Color(225, 228, 228, alpha).getRGB(), -1.0F, 3.0F, (float) (resolution.getScaledHeight_double() * 0.25D));
        float x = (float) (resolution.getScaledWidth_double() + 1.0D);
        float y = (float) (resolution.getScaledHeight_double() + 1.0D);
        dx = mouse.x - x;
        dy = mouse.y - y;
        distance = (float) Math.sqrt(dx * dx + dy * dy);
        alpha = Math.min(110, Math.max(-130, (int) (190.0F - distance * 2.0F)) + 190);
        drawCircle(12, new Color(225, 228, 228, alpha).getRGB(), x, y, (float) (resolution.getScaledHeight_double() * (double) 0.15F));
    }

    private void drawSanta(ScaledResolution resolution, Vec2f mouse) {
        if (resolution.getScaledWidth() < 470 || resolution.getScaledHeight() < 340) return;
        float x = resolution.getScaledWidth() - 102.57143F - 2.0F;
        float y = resolution.getScaledHeight() - 221.14285F + 1.0F;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float dx = mouse.x - (x + 51.285713F);
        float dy = mouse.y - (y + 110.57143F);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        int alpha = Math.min(160, Math.max(-80, (int) (190.0F - distance * 2.0F)) + 190);
        GlStateManager.color(0.9F, 0.9F, 0.9F, alpha / 255.0F);
        GlStateManager.enableAlpha();
        mc.getTextureManager().bindTexture(santa);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 102.57143F, 221.14285F, 102.57143F, 221.14285F);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        if (changelog != null && mouseX > 0 && mouseX <= changelog.width
                && mouseY >= height - changelog.height && mouseY <= height
                && !com.adjustclient.utilities.ScreenUtil.isAdjustScreenOpen()
                && (VoronoiBackground.transition == null || VoronoiBackground.transition.finished)) {
            VoronoiBackground.close();
            return;
        }
        PollService poll = Adjust.INSTANCE.screenStack.poll;
        if (mouseButton == 0 && poll != null && System.currentTimeMillis() < poll.pollId) {
            for (com.adjustclient.managers.poll.PollOptionButton option : poll.options) {
                if (!option.isHovered(mouseX, mouseY)) continue;
                if (poll.field40 || poll.timer.lastMs + 650L > System.currentTimeMillis()) return;
                com.adjustclient.managers.poll.PollOptionButton previous = poll.selected;
                poll.select(previous == option ? null : option);
                com.adjustclient.utilities.SoundUtil.play("party.message", 0.33F, 0.3F);
                poll.timer.lastMs = System.currentTimeMillis();
                poll.field40 = false;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void drawPoll(Vec2f mouse, ScaledResolution resolution, PollService poll) {
        if (poll == null) return;
        NvgFontRenderer small = Fonts.TAHOMA_10;
        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        String remaining = ChatColor.WHITE.toString() + "-" + ChatColor.RESET + " "
                + com.adjustclient.utilities.TimeUtil.timeLeft(poll.pollId - System.currentTimeMillis());
        String heading = "Weekly Poll ";
        float right = resolution.getScaledWidth();
        small.drawString(heading, right - small.getStringWidth(heading + remaining) - 2.5F, 4.0F, accent.getRGB() & 0xFFFFFF | 0xD2000000);
        small.drawString(remaining, right - small.getStringWidth(remaining) - 2.5F, 4.0F, new Color(245, 245, 245, 140).getRGB());
        poll.prepareQuestion();
        poll.prepareQuestion();
        float y = 11.0F;
        for (String line : poll.questionLines) {
            Fonts.TAHOMA_12.drawString(line, right - Fonts.TAHOMA_12.getStringWidth(line) - 1.0F, y, new Color(241, 240, 245, 210).getRGB());
            y += 7.5F;
        }
        float optionWidth = Math.max(poll.questionWidth / 1.65F, 80.0F);
        y += 1.5F;
        for (int i = 0; i < poll.options.size(); i++) {
            com.adjustclient.managers.poll.PollOptionButton option = poll.options.get(i);
            float x = right - optionWidth - 1.0F;
            float labelEnd = small.getStringWidth(option.text) + x;
            String percentage = option.percentage + "%";
            float percentageWidth = small.getStringWidth(percentage);
            option.x = x - 1.0F;
            option.y = y - (i == 0 ? 2.0F : 1.0F);
            option.width = right - x;
            option.height = i == 0 ? 5.0F : 6.0F;
            float barStart = labelEnd + 2.0F;
            float barWidth = right - 4.0F - percentageWidth - barStart;
            float requestedWidth = option.percentage / 100.0F * barWidth;
            option.hover.update(System.currentTimeMillis() < poll.pollId && option.isHovered(mouse.x, mouse.y) ? 1.0F : 0.0F);
            option.selection.update(option.poll.selected == option ? 1.0F : 0.0F);
            float selected = option.selection.value;
            float hover = option.hover.value;
            int opacity = (int) (((1.0F - selected) * 5.0F + 10.0F) * hover) + (int) (selected * 45.0F);
            int grey = opacity / 3 + 215;
            option.result.update(requestedWidth);
            option.result.duration = 170L;
            float filled = Math.min(Math.max(0.0F, option.result.value), barWidth);
            small.drawString(option.text, x, y, new Color(grey, grey, grey, opacity + 200).getRGB());
            small.drawString(percentage, right - 2.0F - percentageWidth, y, new Color(200, 200, 200, opacity + 180).getRGB());
            NvgDraw.fillRect(new Color(235, 235, 235, (int) (opacity / 1.65F) + 27).getRGB(),
                    barStart + filled, y - 1.0F, barStart + barWidth, y + 3.0F);
            Color barColor = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            NvgDraw.fillRect(((int) (opacity / 1.65F + 67.0F) << 24) | barColor.getRGB() & 0xFFFFFF,
                    barStart, y - 1.0F, barStart + filled, y + 3.0F);
            if (selected != 0.0F) {
                small.drawString("You voted ", x - small.getStringWidth("You voted ") - 4.0F, y, new Color(225, 225, 225, (int) (selected * 120.0F)).getRGB());
            }
            NvgDraw.fillCircle(new Color(235, 235, 235, 60).getRGB(), x - 3.0F, y + 0.5F, 2.0F);
            if (opacity != 0) {
                Color dotColor = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
                NvgDraw.fillCircle((opacity << 26) | dotColor.getRGB() & 0xFFFFFF, x - 3.0F, y + 0.5F, 1.0F);
            }
            y += 7.5F;
        }
    }

    private float addIconButton(String[] labels, float y, float offset) {
        float x = width / 2.0F - 65 * labels.length / 2.0F - offset + 3.5F;
        for (String label : labels) {
            buttonList.add(new AdjustButton(buttonList.size(), x, y, 60, 11, label, this));
            x += 65.0F;
        }
        return x;
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NvgFontRenderer font = Fonts.TAHOMA_12;
        int grey = openingSplash ? 200 : 222;
        String line = ChatColor.ITALIC.toString() + String.valueOf(originalSplash);
        float textAlpha = interactive ? getOfflineFade() * 211.0F : VoronoiBackground.update() * 175.0F;
        font.drawCenteredStringNoShadow(line, width * 0.5F, height / 2.35F + 35.0F,
                new Color(grey, grey, grey, (int) textAlpha).getRGB());
        float opacity = VoronoiBackground.update();
        NvgDraw.setGlobalAlpha(opacity);
        menuActionPending = true;
        Color backgroundColor = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        VoronoiBackground.draw(this, mouseX, mouseY, backgroundColor);
        ScaledResolution resolution = new ScaledResolution(mc);
        Vec2f mouse = new Vec2f(mouseX, mouseY);
        com.adjustclient.utilities.HolidayTheme theme = ServiceUtil.resolveHolidayTheme();
        if (theme == com.adjustclient.utilities.HolidayTheme.HALLOWEEN) {
            drawCornerCircles(mouse, resolution);
        }
        for (MenuParticle particle : particles) particle.draw(mouse);
        drawStatus();
        drawPoll(mouse, resolution, Adjust.INSTANCE.screenStack.poll);
        drawTitle(mouse);
        clientUiAllowed = true;
        changelog.render(width, height, (float) mouseX, (float) mouseY);
        for (GuiButton button : buttonList) {
            if (Adjust.INSTANCE.screenStack.getTopOverlay() != null) button.hovered = false;
        }
        if (theme == com.adjustclient.utilities.HolidayTheme.CHRISTMAS) drawSanta(resolution, mouse);
        super.drawScreen(mouseX, mouseY, partialTicks);
        NvgDraw.setGlobalAlpha(NvgDraw.getGlobalAlpha());
    }

    private static void drawCircle(int segments, int color, float x, float y, float radius) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F, (color >>> 24) / 255.0F);
        for (int i = 0; i < segments; i++) {
            double angle = 2 * i * Math.PI / segments;
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(0.0F, 0.0F);
            GL11.glVertex2f((float) Math.cos(angle) * radius, (float) Math.sin(angle) * radius);
            GL11.glEnd();
        }
        for (int ring = 1; ring <= segments; ring++) {
            float ringRadius = ring * radius / segments;
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i < segments; i++) {
                double angle = 2 * i * Math.PI / segments;
                GL11.glVertex2f((float) Math.cos(angle) * ringRadius, (float) Math.sin(angle) * ringRadius);
            }
            GL11.glEnd();
        }
        GL11.glPopMatrix();
    }

    @Override public void initGui() {
        particles.clear();
        if (ServiceUtil.resolveHolidayTheme() == com.adjustclient.utilities.HolidayTheme.CHRISTMAS) {
            int count = (int) ((height + width) * 0.065F);
            for (int i = 0; i < count; i++) {
                MenuParticle particle = new MenuParticle();
                particle.x = org.apache.commons.lang3.RandomUtils.nextFloat(0.75F, width - 0.75F);
                particle.y = org.apache.commons.lang3.RandomUtils.nextFloat(0.75F, height - 0.75F);
                particles.add(particle);
            }
        }
        openingSplash = openingReady;
        interactive = openingReady;
        originalSplash = openingSplash ? getOfflineTip() : splash;
        if (!initialTransitionSeeded && openingReady) {
            initialTransitionSeeded = true;
            VoronoiBackground.previousColor = new Color(160, 160, 160);
            VoronoiBackground.transition = new com.adjustclient.core.rendering.animation.Animation(
                    com.adjustclient.core.rendering.animation.Easing.LINEAR, 220L);
            VoronoiBackground.reset();
        }
        Calendar date = Calendar.getInstance();
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DAY_OF_MONTH);
        if (month == Calendar.DECEMBER && day >= 23 && day <= 27) originalSplash = "Merry X-mas!";
        else if (month == Calendar.JANUARY && day < 3) originalSplash = "Happy new year!";
        else if (month == Calendar.OCTOBER && day == 31) originalSplash = "OOoooOOOoooo! Spooky!";
        createButtons();
        VoronoiBackground.resize(width, height);
    }

    private void drawStatus() {
        com.adjustclient.managers.servertracker.KnownPlayers snapshot = Adjust.INSTANCE.screenStack.knownPlayers;
        if (snapshot.timestamp == 0L || snapshot.players.isEmpty()) return;
        NvgFontRenderer font = Fonts.TAHOMA_10;
        Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        int titleColor = accent.getRGB() & 0xFFFFFF | 0xD2000000;
        float x = font.drawString("Detection Status", 2.5F, 4.0F, titleColor);
        x = font.drawString(" - ", x, 4.0F, new Color(180, 180, 180, 180).getRGB());
        x = font.drawString("Hypixel", x, 4.0F, titleColor);
        font.drawString(" - " + com.adjustclient.utilities.TimeUtil.timeAgo(snapshot.timestamp, " ago"), x, 4.0F, new Color(180, 180, 180, 180).getRGB());
        float widest = 0.0F;
        for (String key : snapshot.players.keySet()) widest = Math.max(widest, font.getStringWidth(key));
        int row = 0;
        for (java.util.Map.Entry<String, com.adjustclient.managers.servertracker.TrackerScope> entry : snapshot.players.entrySet()) {
            String key = entry.getKey().replace(":", ChatColor.GRAY.toString() + ":" + ChatColor.RESET)
                    .replace(")", ")" + ChatColor.RESET).replace("(", ChatColor.GRAY.toString() + "(");
            float baseline = row * 6.0F + 10.5F;
            Color bullet = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            font.drawString("*", 3.0F, baseline + 1.0F, ColorUtil.withAlpha(bullet, 90).getRGB());
            font.drawString(key, 5.0F, baseline, new Color(230, 230, 230, 180).getRGB());
            font.drawString(" " + entry.getValue().field16, widest + 8.0F, baseline, ((Color) entry.getValue().field20).getRGB());
            row++;
        }
    }

    private void createButtons() {
        buttonList.clear();
        float y = height / 2.35F;
        addIconButton(new String[] {
                net.minecraft.util.StringUtils.stripControlCodes(I18n.format("menu.singleplayer")),
                net.minecraft.util.StringUtils.stripControlCodes(I18n.format("menu.multiplayer")),
                "Account Manager"
        }, y, 0.0F);
        float x = width / 2.0F - 65.0F - 11.0F + 3.5F;
        y += 16.0F;
        buttonList.add(new AdjustButton(buttonList.size(), x, y, 60, 11, "Game Settings", this));
        x += 65.0F;
        buttonList.add(new AdjustButton(buttonList.size(), x, y, 60, 11, "Quit Playing", this));
        x += 65.0F;
        AdjustButton friends = new AdjustButton(buttonList.size(), x, y, 11, 11, "k", this);
        friends.font = Fonts.ICONS_13;
        buttonList.add(friends);
    }

    @Override public int multiplyAlpha(int alpha) { return alpha; }

    @Override public boolean doesGuiPauseGame() { return false; }

    private static float getOfflineFade() {
        return OFFLINE_SPLASH_FADE.update(1.0F);
    }

    private static String getOfflineTip() {
        if (offlineTipTemplate == null) {
            String tip = ChatColor.WHITE.toString() + ChatColor.BOLD + "Tip: "
                    + ChatColor.RESET + OFFLINE_TIPS[com.adjustclient.utilities.RandomUtil.RANDOM.nextInt(OFFLINE_TIPS.length)];
            if (Math.random() >= 0.999D) {
                tip = ChatColor.RESET + "This is a special message with a chance of "
                        + ChatColor.WHITE + ChatColor.BOLD.toString() + "1 in 1,000"
                        + ChatColor.RESET + "! Congratulations!";
            }
            while (tip.indexOf('*') != -1) {
                tip = tip.replaceFirst("\\*", ChatColor.WHITE.toString());
                tip = tip.replaceFirst("\\*", ChatColor.RESET.toString());
            }
            offlineTipTemplate = tip;
        }
        return offlineTipTemplate
                .replace("^^", String.valueOf(UserConfiguration.IRC_PREFIX.getChar()))
                .replace("^", String.valueOf(UserConfiguration.COMMAND_PREFIX.getChar()));
    }

    @SuppressWarnings("unused")
    private void vendorUpdateScreen() {
        boolean online = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        for (GuiButton button : buttonList) button.enabled = !online || HydraMessageDispatcher.repositoriesReady;
        if (online && ServiceUtil.recoveryRequested) {
            mc.displayGuiScreen(new RecoveryKeyScreen());
        } else if (online && HydraMessageDispatcher.repositoriesReady && UserConfiguration.changelogUnread && !changelogOpened) {
            int changes = Changelog.initialUnreadCount == null ? 0 : Changelog.initialUnreadCount;
            ChatMessage text = new ChatMessage();
            text.append("You just updated to version ");
            text.bold = true;
            text.append(Changelog.latest.title);
            text.append("! There " + (changes == 1 ? "is " : "are "));
            text.bold = true;
            text.append(Integer.toString(changes));
            text.append(" new change" + (changes == 1 ? "" : "s") + ". Would you like to view the changelog?");
            DialogBuilder dialog = new DialogBuilder("Update Notification");
            dialog.message = text;
            dialog.addButton("Later", null, DialogBuilder.ALWAYS_ENABLED);
            dialog.addButton("View Changelog",
                    screen -> {
                        java.util.Objects.requireNonNull(changelog);
                        VoronoiBackground.close();
                    }, DialogBuilder.ALWAYS_ENABLED);
            dialog.build();
            changelogOpened = true;
        }
    }

    @SuppressWarnings("unused")
    private boolean vendorCheckConnectionGate(GuiButton button) {
        boolean online = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (online && (!ConfigurationService.configurationsAvailable || !ConfigurationService.configurationReceived)) {
            mc.displayGuiScreen(new AuthenticatingScreen());
            return false;
        }
        if (button.id != 4 && !ServiceUtil.challengeAccepted) {
            DialogBuilder dialog = new DialogBuilder("Fatal error inside Hydra protocol");
            dialog.message.append("An unrecoverable error occurred inside the Hydra protocol. Please notify Adjust staff about this!");
            dialog.addButton("OK", null, DialogBuilder.ALWAYS_ENABLED);
            dialog.build();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private void vendorOpenAuthenticating() {
        boolean online = Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated;
        if (online && (!ConfigurationService.configurationsAvailable || !ConfigurationService.configurationReceived) && !AuthenticatingScreen.authenticationFinished) {
            mc.displayGuiScreen(new AuthenticatingScreen());
        }
    }

    @SuppressWarnings("unused")
    private void vendorSendVote(int index) {
        PollService poll = Adjust.INSTANCE.screenStack.poll;
        if (poll == null) return;
        poll.field40 = true;
        com.adjustclient.utilities.backend.api.request.polls.VoteRequest request =
                new com.adjustclient.utilities.backend.api.request.polls.VoteRequest(index);
        com.adjustclient.utilities.backend.api.PendingRequest pending = request.toPacket();
        pending.sendAsync(
                response -> {
                    poll.field40 = false;
                    if (response.status != ResponseStatus.OK) {
                        Logger.log(LogLevel.WARN, true, "Failed to send poll vote: {}", response);
                        if (poll.selected != null) {
                            poll.selected.votes--;
                            poll.selected.poll.recount();
                        }
                        poll.selected = null;
                    }
                });
    }

    @SuppressWarnings("unused")
    private static boolean vendorCheckSubscriptionAccess(boolean online, boolean subscribed) {
        if (online && !subscribed) {
            if (notificationTimer.hasElapsed(1800L)) {
                com.adjustclient.core.gui.hud.notification.NotificationBuilder builder =
                        new com.adjustclient.core.gui.hud.notification.NotificationBuilder("Subscription")
                                .addText("Your Adjust subscription has expired.", 0xFFBEBCBD);
                builder.lifetimeSteps = 40;
                builder.show();
                notificationTimer.reset();
            }
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    private static void vendorDrawSubscriptionCountdown(int screenWidth, int screenHeight, long remainingMillis) {
        String formatted = formatDuration(remainingMillis);
        if (formatted == null) return;
        int textGrey = 0xD2DEDEDE; // new Color(222, 222, 222, 210)
        TextComponent[] components;
        if (formatted.isEmpty()) {
            components = new TextComponent[] { new TextComponent("Your subscription expired", textGrey) };
        } else {
            Color accent = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
            components = new TextComponent[] {
                new TextComponent("Your subscription expires in ", textGrey),
                new TextComponent(formatted, accent.getRGB())
            };
        }
        float textWidth = TextComponent.getWidth(components, Fonts.TAHOMA_13);
        TextComponent.draw(components, Fonts.TAHOMA_13, screenWidth - 2.0F - textWidth, screenHeight - 5.5F);
    }

    @Override public int getMaxFps() {
        if (!windowActive) return 60;
        return Math.min(175, Minecraft.getMinecraft().gameSettings.limitFramerate);
    }
}
