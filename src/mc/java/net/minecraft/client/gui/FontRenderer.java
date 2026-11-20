package net.minecraft.client.gui;

import com.adjustclient.core.rendering.font.TextRenderer;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import optifine.Config;
import optifine.CustomColors;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class FontRenderer implements IResourceManagerReloadListener, TextRenderer
{
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    private static final String ASCII_CHARS = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
    protected int[] charWidth = new int[256];
    protected float[] charWidthFloat = new float[256];
    public int forkField68 = 0;
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    protected byte[] glyphWidth = new byte[65536];
    private int[] colorCode = new int[32];
    protected ResourceLocation locationFontTexture;
    protected final ResourceLocation locationFontTextureBase;
    private final TextureManager renderEngine;
    private final GameSettings gameSettings;
    public Float fixedCharWidth = null;
    protected float posX;
    protected float posY;
    private boolean unicodeFlag;
    private boolean bidiFlag;
    private float red;
    private float blue;
    private float green;
    private float alpha;
    private int textColor;
    public float offsetBold = 1.0F;
    public boolean randomStyle;
    public boolean boldStyle;
    public boolean italicStyle;
    public boolean underlineStyle;
    public boolean strikethroughStyle;
    public boolean enabled = true;
    public boolean forkField87 = true;
    private boolean blend = false;
    private float fontScaleFactor = 1.0F;

    public FontRenderer(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode)
    {
        this.gameSettings = gameSettingsIn;
        this.locationFontTextureBase = location;
        this.locationFontTexture = resolveHdFontLocation(location);
        this.renderEngine = textureManagerIn;
        this.unicodeFlag = unicode;

        if (textureManagerIn != null)
        {
            textureManagerIn.bindTexture(this.locationFontTexture);
        }

        this.initColorCodes();
        this.readGlyphSizes();
        this.readFontTextureSafe();
    }

    private void initColorCodes()
    {
        for (int i = 0; i < 32; ++i)
        {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;

            if (i == 6)
            {
                k += 85;
            }

            if (this.gameSettings != null && this.gameSettings.anaglyph)
            {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }

            if (i >= 16)
            {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            int col = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;

            if (Config.isCustomColors())
            {
                col = CustomColors.getTextColor(i, col);
            }

            this.colorCode[i] = col;
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.locationFontTexture = resolveHdFontLocation(this.locationFontTextureBase);
        Arrays.fill(unicodePageLocations, null);
        this.initColorCodes();
        this.readFontTexture();
        this.readGlyphSizes();
    }

    private void readFontTextureSafe()
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc != null && mc.getResourceManager() != null && this.locationFontTexture != null)
        {
            try
            {
                this.readFontTexture();
            }
            catch (RuntimeException ignored)
            {
            }
        }
    }

    private void readFontTexture()
    {
        BufferedImage bufferedimage;
        InputStream inputstream = null;

        try
        {
            inputstream = getResourceInputStream(this.locationFontTexture);
            bufferedimage = TextureUtil.readBufferedImage(inputstream);
        }
        catch (IOException ioexception)
        {
            throw new RuntimeException(ioexception);
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }

        Properties props = readFontProperties(this.locationFontTexture);
        int imgWidth = bufferedimage.getWidth();
        int imgHeight = bufferedimage.getHeight();
        int charWidthPx = imgWidth / 16;
        int charHeightPx = imgHeight / 16;
        float scaleFactor = (float)imgWidth / 128.0F;
        this.fontScaleFactor = scaleFactor;
        this.offsetBold = scaleFactor < 1.0F ? 1.0F : (scaleFactor <= 2.0F ? 1.0F / scaleFactor : 0.5F);
        float customOffsetBold = readFloatProperty(props, "offsetBold", -1.0F);

        if (customOffsetBold >= 0.0F)
        {
            this.offsetBold = customOffsetBold;
        }

        this.blend = readBooleanProperty(props, "blend", false);
        int[] aint = new int[imgWidth * imgHeight];
        bufferedimage.getRGB(0, 0, imgWidth, imgHeight, aint, 0, imgWidth);

        for (int ch = 0; ch < 256; ++ch)
        {
            int cellX = ch % 16;
            int cellY = ch / 16;
            int col = charWidthPx - 1;

            for (; col >= 0; --col)
            {
                int xPx = cellX * charWidthPx + col;
                boolean emptyCol = true;

                for (int row = 0; row < charHeightPx && emptyCol; ++row)
                {
                    int yOffset = (cellY * charHeightPx + row) * imgWidth;

                    if ((aint[xPx + yOffset] >>> 24) > 16)
                    {
                        emptyCol = false;
                    }
                }

                if (!emptyCol)
                {
                    break;
                }
            }

            if (ch == 32)
            {
                if (charWidthPx >= 9)
                {
                    col = (int)(1.5F * scaleFactor);
                }
                else
                {
                    col = (int)(2.0F * scaleFactor);
                }
            }

            this.charWidthFloat[ch] = (float)(col + 1) / scaleFactor + 1.0F;
        }

        readCustomCharWidths(props, this.charWidthFloat);

        for (int ch = 0; ch < this.charWidth.length; ++ch)
        {
            this.charWidth[ch] = Math.round(this.charWidthFloat[ch]);
        }
    }

    private void readGlyphSizes()
    {
        InputStream inputstream = null;

        try
        {
            inputstream = getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
            inputstream.read(this.glyphWidth);
        }
        catch (IOException ioexception)
        {
            throw new RuntimeException(ioexception);
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }
    }

    private static InputStream getResourceInputStream(ResourceLocation location) throws IOException
    {
        return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }

    private static boolean hasResource(ResourceLocation location)
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null || mc.getResourceManager() == null || location == null)
        {
            return false;
        }

        InputStream stream = null;

        try
        {
            stream = mc.getResourceManager().getResource(location).getInputStream();
            return stream != null;
        }
        catch (IOException ignored)
        {
            return false;
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }
    }

    private static ResourceLocation resolveHdFontLocation(ResourceLocation baseLocation)
    {
        if (baseLocation == null)
        {
            return null;
        }

        if (Config.getGameSettings() != null && !Config.isCustomFonts())
        {
            return baseLocation;
        }

        String path = baseLocation.getResourcePath();
        String texturesPrefix = "textures/";
        String mcpatcherPrefix = "mcpatcher/";

        if (!path.startsWith(texturesPrefix))
        {
            return baseLocation;
        }

        ResourceLocation candidate = new ResourceLocation(baseLocation.getResourceDomain(), mcpatcherPrefix + path.substring(texturesPrefix.length()));
        return hasResource(candidate) ? candidate : baseLocation;
    }

    private static Properties readFontProperties(ResourceLocation fontLocation)
    {
        Properties properties = new Properties();

        if (fontLocation == null)
        {
            return properties;
        }

        String path = fontLocation.getResourcePath();

        if (!path.endsWith(".png"))
        {
            return properties;
        }

        String propsPath = path.substring(0, path.length() - 4) + ".properties";
        ResourceLocation propsLocation = new ResourceLocation(fontLocation.getResourceDomain(), propsPath);
        InputStream stream = null;

        try
        {
            stream = getResourceInputStream(propsLocation);

            if (stream != null)
            {
                properties.load(stream);
            }
        }
        catch (FileNotFoundException ignored)
        {
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }

        return properties;
    }

    private static void readCustomCharWidths(Properties props, float[] widths)
    {
        for (Object keyObj : props.keySet())
        {
            String key = (String)keyObj;
            String prefix = "width.";

            if (key.startsWith(prefix))
            {
                int index = parseInt(key.substring(prefix.length()), -1);

                if (index >= 0 && index < widths.length)
                {
                    float value = parseFloat(props.getProperty(key), -1.0F);

                    if (value >= 0.0F)
                    {
                        widths[index] = value;
                    }
                }
            }
        }
    }

    private static float readFloatProperty(Properties props, String key, float def)
    {
        String value = props.getProperty(key);
        return value == null ? def : parseFloat(value, def);
    }

    private static boolean readBooleanProperty(Properties props, String key, boolean def)
    {
        String value = props.getProperty(key);

        if (value == null)
        {
            return def;
        }

        String trimmed = value.trim().toLowerCase(Locale.ENGLISH);

        if (trimmed.equals("true") || trimmed.equals("on"))
        {
            return true;
        }

        if (trimmed.equals("false") || trimmed.equals("off"))
        {
            return false;
        }

        return def;
    }

    private static int parseInt(String str, int def)
    {
        if (str == null)
        {
            return def;
        }

        try
        {
            return Integer.parseInt(str.trim());
        }
        catch (NumberFormatException ignored)
        {
            return def;
        }
    }

    private static float parseFloat(String str, float def)
    {
        if (str == null)
        {
            return def;
        }

        try
        {
            return Float.parseFloat(str.trim());
        }
        catch (NumberFormatException ignored)
        {
            return def;
        }
    }

    private void bindFontTexture()
    {
        this.renderEngine.bindTexture(this.locationFontTexture);

        if (this.fontScaleFactor > 1.0F || this.blend)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            if (this.blend)
            {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            }
        }
    }

    private float renderChar(char ch, boolean italic)
    {
        return this.renderChar(ch, italic, 1.0F);
    }

    private float renderChar(char ch, boolean italic, float scale)
    {
        float advance;

        if (ch == 32)
        {
            advance = (this.unicodeFlag ? 4.0F : this.charWidthFloat[32]) * scale;
        }
        else
        {
            int i = ASCII_CHARS.indexOf(ch);

            if (i != -1 && !this.unicodeFlag)
            {
                advance = this.renderDefaultChar(i, italic, scale);
            }
            else
            {
                advance = this.renderUnicodeChar(ch, italic, scale);
            }
        }

        return this.fixedCharWidth != null ? this.fixedCharWidth.floatValue() * scale : advance;
    }

    protected float renderDefaultChar(int ch, boolean italic)
    {
        return this.renderDefaultChar(ch, italic, 1.0F);
    }

    protected float renderDefaultChar(int ch, boolean italic, float scale)
    {
        int i = ch % 16 * 8;
        int j = ch / 16 * 8;
        float italicShift = italic ? 1.0F * scale : 0.0F;
        this.bindFontTexture();
        float cellWidth = 6.99F * scale;
        float cellHeight = 7.99F * scale;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f((float)i / 128.0F, (float)j / 128.0F);
        GL11.glVertex3f(this.posX + italicShift, this.posY, 0.0F);
        GL11.glTexCoord2f((float)i / 128.0F, ((float)j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - italicShift, this.posY + cellHeight, 0.0F);
        GL11.glTexCoord2f(((float)i + 7.99F - 1.0F) / 128.0F, (float)j / 128.0F);
        GL11.glVertex3f(this.posX + cellWidth + italicShift, this.posY, 0.0F);
        GL11.glTexCoord2f(((float)i + 7.99F - 1.0F) / 128.0F, ((float)j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + cellWidth - italicShift, this.posY + cellHeight, 0.0F);
        GL11.glEnd();
        return this.charWidthFloat[ch] * scale;
    }

    private ResourceLocation getUnicodePageLocation(int page)
    {
        if (unicodePageLocations[page] == null)
        {
            unicodePageLocations[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", new Object[] {Integer.valueOf(page)}));
            unicodePageLocations[page] = resolveHdFontLocation(unicodePageLocations[page]);
        }

        return unicodePageLocations[page];
    }

    private void loadGlyphTexture(int page)
    {
        this.renderEngine.bindTexture(this.getUnicodePageLocation(page));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    }

    protected float renderUnicodeChar(char ch, boolean italic)
    {
        return this.renderUnicodeChar(ch, italic, 1.0F);
    }

    protected float renderUnicodeChar(char ch, boolean italic, float scale)
    {
        if (this.glyphWidth[ch] == 0)
        {
            return 0.0F;
        }
        else
        {
            int i = ch / 256;
            this.loadGlyphTexture(i);
            int j = (this.glyphWidth[ch] & 255) >>> 4;
            int k = this.glyphWidth[ch] & 15;
            float f = (float)j;
            float f1 = (float)(k + 1);
            float f2 = (float)(ch % 16 * 16) + f;
            float f3 = (float)((ch & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = italic ? 1.0F * scale : 0.0F;
            float quadW = f4 / 2.0F * scale;
            float quadH = 7.99F * scale;
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, 0.0F);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + quadH, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + quadW + f5, this.posY, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + quadW - f5, this.posY + quadH, 0.0F);
            GL11.glEnd();
            return ((f1 - f) / 2.0F + 1.0F) * scale;
        }
    }

    public int drawStringWithShadow(String text, float x, float y, int color)
    {
        return this.drawString(text, x, y, color, true);
    }

    public int drawString(String text, int x, int y, int color)
    {
        return !this.enabled || !this.forkField87 ? 0 : this.drawString(text, (float)x, (float)y, color, false);
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow)
    {
        return (int)this.drawString(text, color, dropShadow, x, y, 1.0F);
    }

    @Override
    public float drawString(String text, float x, float y, int color)
    {
        return this.drawString(text, color, true, x, y, 1.0F);
    }

    @Override
    public float drawString(String text, float x, float y, int color, float scale)
    {
        return this.drawString(text, color, true, x, y, scale);
    }

    @Override
    public void drawStringNoShadow(String text, float x, float y, int color)
    {
        this.drawString(text, color, false, x, y, 1.0F);
    }

    public float drawString(String text, float x, float y, int color, boolean dropShadow, float scale)
    {
        return this.drawString(text, color, dropShadow, x, y, scale);
    }

    public float drawString(String text, int color, boolean dropShadow, float x, float y, float scale)
    {
        if (!this.enabled || !this.forkField87)
        {
            return 0.0F;
        }

        if (scale == 1.0F && !this.unicodeFlag && this.fontScaleFactor <= 1.0F && this.isGuiScaleOne())
        {
            x = (float)Math.floor((double)x);
            y = (float)Math.floor((double)y);
        }

        GlStateManager.enableAlpha();
        this.resetStyles();
        float f;

        if (dropShadow)
        {
            f = this.renderString(text, color, true, x + scale, y + scale, scale);
            f = Math.max(f, this.renderString(text, color, false, x, y, scale));
        }
        else
        {
            f = this.renderString(text, color, false, x, y, scale);
        }

        return f;
    }

    private boolean isGuiScaleOne()
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null || mc.gameSettings == null)
        {
            return false;
        }

        int scale = 1;
        int maxScale = mc.gameSettings.guiScale == 0 ? 1000 : mc.gameSettings.guiScale;

        while (scale < maxScale && mc.displayWidth / (scale + 1) >= 320 && mc.displayHeight / (scale + 1) >= 240)
        {
            ++scale;
        }

        if (mc.isUnicode() && scale % 2 != 0 && scale != 1)
        {
            --scale;
        }

        return scale == 1;
    }

    private String bidiReorder(String text)
    {
        try
        {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException var3)
        {
            return text;
        }
    }

    private void resetStyles()
    {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    private void renderStringAtPos(String text, boolean shadow)
    {
        this.renderStringAtPos(text, shadow, 1.0F);
    }

    private void renderStringAtPos(String text, boolean shadow, float scale)
    {
        boolean needsBlend = this.blend || this.fontScaleFactor > 1.0F || this.unicodeFlag || this.alpha < 1.0F;
        boolean restoreDisableBlend = false;

        if (needsBlend)
        {
            restoreDisableBlend = !GL11.glIsEnabled(GL11.GL_BLEND);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        }

        for (int i = 0; i < text.length(); ++i)
        {
            char c0 = text.charAt(i);

            if (c0 == 167 && i + 1 < text.length())
            {
                int i1 = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

                if (i1 < 16)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (i1 < 0 || i1 > 15)
                    {
                        i1 = 15;
                    }

                    if (shadow)
                    {
                        i1 += 16;
                    }

                    int j1 = this.colorCode[i1];

                    if (Config.isCustomColors())
                    {
                        j1 = CustomColors.getTextColor(i1, j1);
                    }

                    this.textColor = j1;
                    this.forkField68 = j1;
                    GlStateManager.color((float)(j1 >> 16) / 255.0F, (float)(j1 >> 8 & 255) / 255.0F, (float)(j1 & 255) / 255.0F, this.alpha);
                }
                else if (i1 == 16)
                {
                    this.randomStyle = true;
                }
                else if (i1 == 17)
                {
                    this.boldStyle = true;
                }
                else if (i1 == 18)
                {
                    this.strikethroughStyle = true;
                }
                else if (i1 == 19)
                {
                    this.underlineStyle = true;
                }
                else if (i1 == 20)
                {
                    this.italicStyle = true;
                }
                else if (i1 == 21)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    GlStateManager.color(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            }
            else
            {
                int j = ASCII_CHARS.indexOf(c0);

                if (this.randomStyle && j != -1)
                {
                    int k = (int)(Math.round(this.getCharWidthFloat(c0)) * scale);
                    char c1;

                    while (true)
                    {
                        j = this.fontRandom.nextInt(ASCII_CHARS.length());
                        c1 = ASCII_CHARS.charAt(j);

                        if (k == (int)(Math.round(this.getCharWidthFloat(c1)) * scale))
                        {
                            break;
                        }
                    }

                    c0 = c1;
                }

                float f1 = (j == -1 || this.unicodeFlag ? 0.5F : this.offsetBold) * scale;
                boolean flag = (c0 == 0 || j == -1 || this.unicodeFlag) && shadow;

                if (flag)
                {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderChar(c0, this.italicStyle, scale);

                if (flag)
                {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle)
                {
                    this.posX += f1;

                    if (flag)
                    {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderChar(c0, this.italicStyle, scale);
                    this.posX -= f1;

                    if (flag)
                    {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    if (this.fixedCharWidth == null)
                    {
                        f += f1;
                    }
                }

                if (this.strikethroughStyle)
                {
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    GlStateManager.disableTexture2D();
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION);
                    float strikeY = this.posY + (float)(this.FONT_HEIGHT / 2) * scale;
                    worldrenderer.pos((double)this.posX, (double)strikeY, 0.0D).endVertex();
                    worldrenderer.pos((double)(this.posX + f), (double)strikeY, 0.0D).endVertex();
                    worldrenderer.pos((double)(this.posX + f), (double)(strikeY - scale), 0.0D).endVertex();
                    worldrenderer.pos((double)this.posX, (double)(strikeY - scale), 0.0D).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                }

                if (this.underlineStyle)
                {
                    Tessellator tessellator1 = Tessellator.getInstance();
                    WorldRenderer worldrenderer1 = tessellator1.getWorldRenderer();
                    GlStateManager.disableTexture2D();
                    worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
                    float l = (this.underlineStyle ? -1.0F : 0.0F) * scale;
                    float underY = this.posY + (float)this.FONT_HEIGHT * scale;
                    worldrenderer1.pos((double)(this.posX + l), (double)underY, 0.0D).endVertex();
                    worldrenderer1.pos((double)(this.posX + f), (double)underY, 0.0D).endVertex();
                    worldrenderer1.pos((double)(this.posX + f), (double)(underY - scale), 0.0D).endVertex();
                    worldrenderer1.pos((double)(this.posX + l), (double)(underY - scale), 0.0D).endVertex();
                    tessellator1.draw();
                    GlStateManager.enableTexture2D();
                }

                this.posX += f;
            }
        }

        if (restoreDisableBlend)
        {
            GlStateManager.disableBlend();
        }
    }

    private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow)
    {
        if (this.bidiFlag)
        {
            int i = (int)this.getStringWidth(this.bidiReorder(text));
            x = x + width - i;
        }

        return this.renderString(text, (float)x, (float)y, color, dropShadow);
    }

    private int renderString(String text, float x, float y, int color, boolean dropShadow)
    {
        return (int)this.renderString(text, color, dropShadow, x, y, 1.0F);
    }

    public float renderString(String text, int color, boolean dropShadow, float x, float y, float scale)
    {
        if (!this.enabled || !this.forkField87 || text == null)
        {
            return 0.0F;
        }
        else
        {
            if (com.adjustclient.managers.util.ServiceUtil.ahmedElSisiMode)
            {
                text = com.adjustclient.managers.util.ServiceUtil.substituteTextCharacters(text);
            }

            if (this.bidiFlag)
            {
                text = this.bidiReorder(text);
            }

            if ((color & -67108864) == 0)
            {
                color |= -16777216;
            }

            if (dropShadow)
            {
                color = (color & 16579836) >> 2 | color & -16777216;
            }

            this.red = (float)(color >> 16 & 255) / 255.0F;
            this.blue = (float)(color >> 8 & 255) / 255.0F;
            this.green = (float)(color & 255) / 255.0F;
            this.alpha = (float)(color >> 24 & 255) / 255.0F;
            GlStateManager.color(this.red, this.blue, this.green, this.alpha);
            this.posX = x;
            this.posY = y;
            this.renderStringAtPos(text, dropShadow, scale);
            return this.posX;
        }
    }

    @Override
    public float getStringWidth(String text)
    {
        return this.getStringWidth(text, 1.0F);
    }

    @Override
    public float getStringWidth(String text, float scale)
    {
        if (text == null)
        {
            return 0.0F;
        }
        else
        {
            if (com.adjustclient.managers.util.ServiceUtil.ahmedElSisiMode)
            {
                text = com.adjustclient.managers.util.ServiceUtil.substituteTextCharacters(text);
            }

            float width = 0.0F;
            boolean bold = false;

            for (int j = 0; j < text.length(); ++j)
            {
                char c0 = text.charAt(j);
                float charW = this.getCharWidthFloat(c0) * scale;

                if (charW < 0.0F && j < text.length() - 1)
                {
                    ++j;
                    c0 = text.charAt(j);

                    if (c0 != 108 && c0 != 76)
                    {
                        if (c0 == 114 || c0 == 82)
                        {
                            bold = false;
                        }
                    }
                    else
                    {
                        bold = true;
                    }

                    charW = 0.0F;
                }

                width += charW;

                if (this.fixedCharWidth == null && bold && charW > 0.0F)
                {
                    width += (this.unicodeFlag ? 1.0F : this.offsetBold) * scale;
                }
            }

            return (float)((int)width);
        }
    }

    public int getCharWidth(char character)
    {
        return Math.round(this.getCharWidthFloat(character));
    }

    public float getCharWidthFloat(char character)
    {
        if (character == 167)
        {
            return -1.0F;
        }
        else if (this.fixedCharWidth != null)
        {
            return this.fixedCharWidth.floatValue();
        }
        else
        {
            return this.getRawCharWidthFloat(character);
        }
    }

    private float getRawCharWidthFloat(char character)
    {
        if (character == 167)
        {
            return -1.0F;
        }
        else if (character == 32)
        {
            return this.charWidthFloat[32] > 0.0F ? this.charWidthFloat[32] : 4.0F;
        }
        else
        {
            int i = ASCII_CHARS.indexOf(character);

            if (character > 0 && i != -1 && !this.unicodeFlag)
            {
                return this.charWidthFloat[i];
            }
            else if (this.glyphWidth[character] != 0)
            {
                int j = (this.glyphWidth[character] & 255) >>> 4;
                int k = this.glyphWidth[character] & 15;
                ++k;
                return (float)((k - j) / 2 + 1);
            }
            else
            {
                return 0.0F;
            }
        }
    }

    public String trimStringToWidth(String text, float width)
    {
        return this.trimStringToWidth(text, width, false);
    }

    @Override
    public String trimStringToWidth(String text, float width, boolean reverse)
    {
        if (text == null)
        {
            return "";
        }

        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && f < width; l += k)
        {
            char c0 = text.charAt(l);
            float charW = this.getCharWidthFloat(c0);

            if (flag)
            {
                flag = false;

                if (c0 != 108 && c0 != 76)
                {
                    if (c0 == 114 || c0 == 82)
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (charW < 0.0F)
            {
                flag = true;
            }
            else
            {
                f += charW;

                if (this.fixedCharWidth == null && flag1)
                {
                    f += 1.0F;
                }
            }

            if (f > width)
            {
                break;
            }

            if (reverse)
            {
                stringbuilder.insert(0, (char)c0);
            }
            else
            {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private String trimStringNewline(String text)
    {
        while (text != null && text.endsWith("\n"))
        {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor)
    {
        this.resetStyles();
        this.textColor = textColor;
        this.forkField68 = textColor;
        str = this.trimStringNewline(str);
        this.renderSplitString(str, x, y, wrapWidth, false);
    }

    private void renderSplitString(String str, int x, int y, int wrapWidth, boolean addShadow)
    {
        for (String s : this.listFormattedStringToWidth(str, wrapWidth))
        {
            this.renderStringAligned(s, x, y, wrapWidth, this.textColor, addShadow);
            y += this.FONT_HEIGHT;
        }
    }

    public int splitStringWidth(String str, int maxLength)
    {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
    }

    public void setUnicodeFlag(boolean unicodeFlagIn)
    {
        this.unicodeFlag = unicodeFlagIn;
    }

    public boolean getUnicodeFlag()
    {
        return this.unicodeFlag;
    }

    public void setBidiFlag(boolean bidiFlagIn)
    {
        this.bidiFlag = bidiFlagIn;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth)
    {
        return Arrays.<String>asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth)
    {
        int i = this.sizeStringToWidth(str, wrapWidth);

        if (str.length() <= i)
        {
            return str;
        }
        else
        {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == 32 || c0 == 10;
            String s1 = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth)
    {
        int i = str.length();
        float width = 0.0F;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k)
        {
            char c0 = str.charAt(k);

            switch (c0)
            {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    width += this.getCharWidthFloat(c0);

                    if (this.fixedCharWidth == null && flag)
                    {
                        width += 1.0F;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1)
                    {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 108 && c1 != 76)
                        {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10)
            {
                ++k;
                l = k;
                break;
            }

            if (width > (float)wrapWidth)
            {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }

    private static boolean isFormatSpecial(char formatChar)
    {
        return formatChar >= 107 && formatChar <= 111 || formatChar >= 75 && formatChar <= 79 || formatChar == 114 || formatChar == 82;
    }

    public static String getFormatFromString(String text)
    {
        String s = "";
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1)
        {
            if (i < j - 1)
            {
                char c0 = text.charAt(i + 1);

                if (isFormatColor(c0))
                {
                    s = "\u00a7" + c0;
                }
                else if (isFormatSpecial(c0))
                {
                    s = s + "\u00a7" + c0;
                }
            }
        }

        return s;
    }

    public boolean getBidiFlag()
    {
        return this.bidiFlag;
    }

    public int getColorCode(char character)
    {
        int i = "0123456789abcdef".indexOf(character);

        if (i >= 0 && i < this.colorCode.length)
        {
            int j = this.colorCode[i];

            if (Config.isCustomColors())
            {
                j = CustomColors.getTextColor(i, j);
            }

            return j;
        }
        else
        {
            return 16777215;
        }
    }
}
