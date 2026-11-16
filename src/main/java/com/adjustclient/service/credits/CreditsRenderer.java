package com.adjustclient.service.credits;

import com.adjustclient.event.Render2DEvent;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class CreditsRenderer {
    public static CreditsRenderer INSTANCE;

    private static final String[] ENTRIES = {
            "استعراض اقوي نسخه من برنامج نجرات |2024| NjRat 0.7D Danger Edition",
            "اقوي نسخه من برنامج نجرات |2024| Dangerous RAT 2022 V5 Cracked by Unknown Venom Full Free Version",
            "crypter 100% fuud 2024 #remcos #xworm #quasar etc شكرا البرنامج روعة",
            "تشفير سرفر نجرات كلين وتخطي حمايه وندوز ديفندر Encrypt files and bypass Windows Defender protection",
            "أفضل برامج الVPN لفتــح بـورتـات نـجـرات وضمان وصول التبليغ الخارجي 2019",
            "Download a Professional Notepad تحميل مذكره احترافية 2017",
            "اختراق اي جهاز عن طريق صوره | دمج سيرفر اي رات بصوره حقيقيه | Hack any device by image",
            " تم تغيير اسم القناه من Ahmed El-Sisi Hacker اله Hamoudi Hacker",
            "مفكرة احترافية للشروحات | Professional Notepad"
    };

    public String title;
    private final CreditLink[] links;
    private CreditEntry entry;
    public int gamesPlayed;
    public int kills;
    public int bonusKills;

    public CreditsRenderer() {
        CreditLink[] array = new CreditLink[20];
        for (int i = 0; i < 20; i++) {
            float progress = (float) (i + 1) / 21.0F;
            float displacement = 1.0F - progress;
            float initialValue = progress * 17.0F;
            CreditLink link = new CreditLink();
            link.displacement = displacement;
            link.returning = false;
            Animation animation = new Animation(Easing.LINEAR, 650L);
            animation.from = initialValue;
            animation.value = initialValue;
            link.animation = animation;
            array[i] = link;
        }
        this.title = "Ahmed El-Sisi Hacker";
        this.links = array;
        this.entry = new CreditEntry(1, ENTRIES[1]);
    }

    public void render(Render2DEvent event) {
        ServiceUtil.ahmedElSisiMode = false;
        ScaledResolution sr = event.scaledResolution;
        float baseY = (float) (sr.getScaledHeight() - 40);
        float x = (float) sr.getScaledWidth() / 1.745F;
        for (int i = 0; i < this.title.length(); i++) {
            CreditLink link = this.links[i];
            Animation animation = link.animation;
            animation.update(link.returning ? link.displacement : link.displacement * 16.7F);
            float wobble = 0.0F;
            if (animation.isFinished() && link.timer != null) {
                float elapsed = (float) (int) (System.currentTimeMillis() - link.timer.lastMs);
                wobble = ((elapsed % 800.0F) / 800.0F) * (1.51F * link.displacement);
            }
            float charY = wobble + (baseY + animation.getValue());
            x = Fonts.TAHOMA_20.drawString(String.valueOf(this.title.charAt(i)), x, charY, -1);
            if (animation.isFinished()) {
                if (link.timer == null) {
                    link.timer = new Timer();
                }
                long waitMs = link.returning ? 200L : 1500L;
                if (link.timer.hasElapsed(waitMs)) {
                    link.returning = !link.returning;
                    animation.restart();
                    link.timer = null;
                }
            }
        }

        Gui.drawRect(new Color(40, 40, 40, 255).getRGB(), 0.0F, 0.0F, (float) sr.getScaledWidth(), 12.0F);
        float entryWidth = Fonts.TAHOMA_20.getStringWidth(this.entry.text);
        float marqueeX = ((float) sr.getScaledWidth() + entryWidth * 1.1F) * this.entry.getProgress() - entryWidth;
        Fonts.TAHOMA_20.drawString(this.entry.text, marqueeX, 2.0F, new Color(0, 121, 24, 255).getRGB());

        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRendererObj.drawString("ج", (float) sr.getScaledWidth() / 2.0F, (float) sr.getScaledHeight() / 2.0F, -1, true, 1.0F);

        int alpha = (int) (RandomUtil.RANDOM.nextDouble() * 10.0 + 222.0);
        int textColor = (alpha << 24) | 0xFFFFFF;
        this.drawSocialIcon("facebook", 3, 80);
        Fonts.TAHOMA_12.drawString(" تم تغيير اسم القناه من Ahmed El-Sisi Hacker اله Hamoudi Hacker", 3.0F, 105.0F, textColor);
        Fonts.TAHOMA_12.drawString("حبيت اقدم لكم برنامج BD2.Net Injector سهل وبسيط لدمج اي سيرفر رات اختراق اجهزه بصوره حقيقيه", 3.0F, 115.0F, textColor);
        Fonts.TAHOMA_12.drawString("https://t.me/c/2171088087/8137@", 3.0F, 123.0F, textColor);
        this.drawSocialIcon("whatsapp", 40, 80);

        if (mc.currentScreen instanceof GuiChat) {
            this.drawDangerous(0, sr.getScaledHeight());
        }

        ServiceUtil.ahmedElSisiMode = true;
        if (this.entry.animation.isFinished()) {
            int nextIndex = this.entry.index >= 8 ? 0 : this.entry.index + 1;
            this.entry = new CreditEntry(nextIndex, ENTRIES[nextIndex]);
        }
    }

    private void drawDangerous(int x, int screenHeight) {
        ResourceLocation location = new ResourceLocation("adjust", "textures/ahmedelsisi/dangerous.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        Gui.drawModalRectWithCustomSizedTexture((float) x, (float) (screenHeight - 169), 0.0F, 0.0F, 378.0F, 169.0F, 378.0F, 169.0F);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawSocialIcon(String icon, int x, int y) {
        ResourceLocation location = new ResourceLocation("adjust", "textures/ahmedelsisi/" + icon + ".png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture((float) x, (float) y, 0.0F, 0.0F, 32.0F, 32.0F, 32.0F, 32.0F);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawManager(int left, float top, int right, float bottom) {
        ResourceLocation location = new ResourceLocation("adjust", "textures/ahmedelsisi/manager.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
        GL11.glPushMatrix();
        float width = (float) (right - left);
        float height = bottom - top;
        Gui.drawModalRectWithCustomSizedTexture((float) left, top, 0.0F, 0.0F, width, height, width, height);
        GL11.glPopMatrix();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
