package com.adjustclient.core.gui.hud.information;

import com.adjustclient.core.events.Listener;
import com.adjustclient.core.rendering.animation.Animation;
import com.adjustclient.core.rendering.font.Fonts;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.gui.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import com.adjustclient.Adjust;
import com.adjustclient.core.changelog.Changelog;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.rotation.handler.RenderRotationTracker;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.gui.hud.watermark.WatermarkStandalone;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;

public final class Information extends HudElement implements Listener {
    public static double accumulatedSpeed;
    public static int displayedCounter;

    private final InformationSettings settings;
    private final Animation animation;
    private final List<Line> lines;
    private Object field20;
    private int valueColor;
    private int labelColor;
    private Object field32;
    private int field36;

    public Information(InformationSettings settings) {
        this.movable = com.adjustclient.core.gui.hud.Anchor.BOTTOM_LEFT;
        this.settings = settings;
        this.animation = new Animation(com.adjustclient.core.rendering.animation.Easing.EASE_OUT_CUBIC, 420L);
        this.animation.value = -2.0F;
        this.lines = new ArrayList<>();
        this.field20 = new java.text.DecimalFormat("#0.0");
        this.valueColor = new Color(224, 224, 224, 220).getRGB();
        this.labelColor = new Color(140, 140, 140, 220).getRGB();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void update() {
        this.lines.clear();

        Minecraft mc = Minecraft.getMinecraft();

        // ---- Coordinates ----
        if (this.settings.coordinatesEnabled.isEnabled()) {
            EntityPlayerSP player = mc.thePlayer;
            if (!this.settings.netherCoordinates.isEnabled() || mc.theWorld.provider.getDimensionId() == 1) {
                this.lines.add(this.format("{}, {}, {}", (int) player.posX, (int) player.posY, (int) player.posZ));
            } else {
                double factor = mc.theWorld.provider.getDimensionId() == 0 ? 0.125 : 8.0;
                this.lines.add(this.format("{}, {}, {} [{}, {}]",
                        (int) player.posX, (int) player.posY, (int) player.posZ,
                        (int) (player.posX * factor), (int) (player.posZ * factor)));
            }
        }

        // ---- Angles ----
        if (this.settings.angles.isEnabled()) {
            EntityPlayerSP player = mc.thePlayer;
            float yaw;
            float pitch;
            if (this.settings.serverside.isEnabled()) {
                com.adjustclient.utilities.Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
                yaw = rotation != null ? rotation.x : player.rotationYaw;
                pitch = rotation != null ? rotation.y : player.rotationPitch;
            } else {
                yaw = player.rotationYaw;
                pitch = player.rotationPitch;
            }
            yaw = MathHelper.wrapAngleTo180_float(yaw);
            yaw = RandomUtil.round(yaw, 1);         // round to 1 decimal place
            pitch = RandomUtil.round(pitch, 1);
            if (this.settings.direction.isEnabled()) {
                EnumFacing facing = EnumFacing.getHorizontal(MathHelper.floor_double(yaw / 90.0 + 0.5) & 3);   // HORIZONTALS[(floor & 3) % length]
                this.lines.add(this.format("{} / {} [{}] {}", yaw, pitch, axisLabel(facing), facing.getName()));
            } else {
                this.lines.add(this.format("{} / {}", yaw, pitch));
            }
        }

        // ---- Speed ----
        if (this.settings.speedEnabled.isEnabled()) {
            double speed = getSpeed();
            String unit = "B/s";
            if (this.settings.speedFormat.getValue() == SpeedUnit.KMPH) {
                speed = speed * 3.6;
                unit = "Km/h";
            }
            double rounded = RandomUtil.round(speed, 2);
            this.lines.add(new Line(new Segment[] {
                    new Segment(Double.toString(rounded), this.valueColor, false),
                    new Segment(unit, this.labelColor, true)
            }));
        }

        if (this.settings.tps.isEnabled()) {
            float tps = RandomUtil.round(ServerTracker.INSTANCE.tps, 2);
            this.lines.add(new Line(new Segment[] {
                    new Segment(Float.toString(tps), this.valueColor, false),
                    new Segment("T/s", this.labelColor, true)
            }));
        }

        if (this.settings.fps.isEnabled()) {
            this.lines.add(new Line(new Segment[] {
                    new Segment(displayedCounter + " ", this.valueColor),
                    new Segment("FPS", this.labelColor)
            }));
        }
    }

    /**
     * SLF4J style formatter: every "{}" of the pattern is replaced by the next argument (value colour); the literal text
     * between the placeholders becomes label coloured segments. A missing argument stays "{}", a null argument prints "null".
     */
    private Line format(String pattern, Object... args) {
        List<Segment> segments = new ArrayList<>();
        StringBuilder literal = new StringBuilder();
        int argIndex = 0;
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            if (c == '{' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '}') {
                String arg;
                if (argIndex >= args.length) {
                    arg = "{}";
                } else {
                    Object a = args[argIndex++];
                    arg = a != null ? a.toString() : "null";
                }
                if (literal.length() > 0) {
                    segments.add(new Segment(literal.toString(), this.labelColor));
                    literal.setLength(0);
                }
                segments.add(new Segment(arg, this.valueColor));
                i += 2;
            } else {
                literal.append(c);
                i++;
            }
        }
        if (literal.length() > 0) {
            segments.add(new Segment(literal.toString(), this.labelColor));
        }
        return new Line(segments.toArray(Segment[]::new));
    }

    private static String axisLabel(EnumFacing facing) {
        switch (InformationSwitchMap.$SwitchMap$net$minecraft$util$EnumFacing[facing.ordinal()]) {
            case 1: return "Z-";
            case 2: return "Z+";
            case 3: return "X-";
            case 4: return "X+";
            default: throw new IllegalStateException();
        }
    }

    /** Blocks per second: horizontal distance moved since the last tick (plus an accumulator that is reset) * 20 * timerSpeed. */
    private static float getSpeed() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        double dist = Math.hypot(player.posX - player.lastTickPosX, player.posZ - player.lastTickPosZ);
        double total = dist + accumulatedSpeed;
        accumulatedSpeed = 0;
        double speed = total * 20.0 * mc.timer.timerSpeed;
        return speed >= 0.0 ? (float) speed : 0.0F;
    }

    @Override
    public void render(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean chatOpen = mc.currentScreen instanceof GuiChat;
        NvgFontRenderer font12 = Fonts.TAHOMA_12;
        float target = font12.size;
        if (chatOpen) {
            target = target - (float) (13 * (this.lines.size() + 1));
        }
        this.animation.update(target);
        float height = (float) sr.getScaledHeight();

        boolean ahmedMode = ServiceUtil.ahmedElSisiMode;
        String name;
        if (ahmedMode) {
            name = "Ahmed El-Sisi";
        } else {
            var userManager = Adjust.INSTANCE != null ? (com.adjustclient.core.settings.property.UserManager) Adjust.INSTANCE.field40 : null;
            var selfUser = userManager != null ? (com.adjustclient.managers.user.ClientUser) userManager.field16 : null;
            name = selfUser != null && selfUser.name != null ? selfUser.name : Adjust.username;
            if (name == null) {
                name = "";
            }
        }

        ServiceUtil.ahmedElSisiMode = false;
        float y = height - this.animation.getValue();
        Color hud = HUDModule.getHudColor();
        int argb = hud.getRGB();
        if ((argb >>> 24) != 220) {
            argb = new Color(hud.getRed(), hud.getGreen(), hud.getBlue(), 220).getRGB();
        }
        font12.drawString(name, 2.0F, y, argb);

        NvgFontRenderer font10 = Fonts.TAHOMA_10;
        String edition = ahmedMode
                ? ChatColor.RED + "" + ChatColor.ITALIC + ChatColor.UNDERLINE + "Dangerous Edition 0.7D"
                : Changelog.latest.shortTitle;
        float nameWidth = font12.getStringWidth(name);
        float y2 = (height - this.animation.getValue()) + 1.0F;
        font10.drawString(edition, nameWidth + 3.5F, y2, new Color(208, 208, 208, 220).getRGB());
        ServiceUtil.ahmedElSisiMode = ahmedMode;

        // ---- the information lines, bottom up, 6.0 apart ----
        float step = 6.0F;
        for (Line line : this.lines) {
            Segment[] segments = line.segments();
            if (segments.length > 0) {
                float baseY = (height - this.animation.getValue()) - step;
                float x = 2.5F;
                for (Segment segment : segments) {
                    float drawY = baseY;
                    if (segment.raised) {
                        x += 0.5F;
                        drawY = baseY - 1.0F;
                    }
                    font10.renderString(segment.text, x + 0.5F, drawY + 0.5F, segment.color, true);
                    x = font10.renderString(segment.text, x, drawY, segment.color, false);
                }
            }
            step += 6.0F;
        }
    }
}
