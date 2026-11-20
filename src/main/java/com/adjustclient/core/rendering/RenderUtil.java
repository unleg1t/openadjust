package com.adjustclient.core.rendering;

import com.adjustclient.utilities.Vec2f;
import com.adjustclient.core.rendering.font.NvgFontRenderer;
import com.adjustclient.core.rendering.font.NvgContext;
import com.adjustclient.core.rendering.font.image.NvgDraw;
import com.adjustclient.utilities.game.entity.tracker.ServerPositionTracker;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.settings.ColorSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import com.adjustclient.core.commands.impl.debug.DebugEpsteinSubCommand;
import com.adjustclient.modules.impl.render.ThirdPersonModule;
import com.adjustclient.modules.impl.render.ItemPhysicsModule;

public final class RenderUtil {
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static final Matrix4f MODEL_VIEW = new Matrix4f();
    private static final Vector3f PROJECTED_POINT = new Vector3f();
    public static Frustum frustum;
    public static Integer cursor;
    public static int lastCursor;
    private static final Int2LongOpenHashMap cursors = new Int2LongOpenHashMap();

    public static void applyCursor() {
        if (cursor != null) {
            if (cursor.intValue() != lastCursor) {
                long window = com.adjustclient.utilities.ClipboardUtil.sharedWindowHandle;
                long handle = ((java.util.Map<Integer, Long>) cursors).computeIfAbsent(cursor,
                        ignored -> GLFW.glfwCreateStandardCursor(cursor.intValue()));
                GLFW.glfwSetCursor(window, handle);
                lastCursor = cursor.intValue();
            }
            cursor = null;
        }
    }

    public static void isRenderEntityOutlines(Minecraft minecraft) {
        minecraft.renderGlobal.loadRenderers();
    }

    public static Runnable outlineRefresh(Minecraft minecraft) {
        return () -> minecraft.renderGlobal.loadRenderers();
    }

    public static boolean isInFrustum(Entity entity) {
        if (entity.ignoreFrustumCheck) return true;
        AxisAlignedBB box = entity.getEntityBoundingBox();
        if (ServerPositionTracker.INSTANCE != null) {
            var tracked = ServerPositionTracker.INSTANCE.findTrackedPosition(entity);
            if (tracked != null) box = tracked.boundingBox;
        }
        return isInFrustum(box);
    }

    public static boolean isInFrustum(AxisAlignedBB box) {
        if (frustum == null) return false;
        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.cameraOverride != null ? mc.cameraOverride : mc.getRenderViewEntity();
        frustum.setPosition(camera.posX, camera.posY, camera.posZ);
        return frustum.isBoundingBoxInFrustum(box);
    }

    public static Color rainbow(int alpha, float hue, float offset, float speed) {
        return ColorUtil.withAlpha(Color.getHSBColor(hue * 0.33333334F, 0.7F, 0.7F), alpha);
    }

    public static Vec3 interpolatedPosition(Entity entity, double partialTicks) {
        return new Vec3(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks);
    }

    public static void drawHead(ResourceLocation skin, float x, float y) {
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 32.0F, 32, 32, 256.0F, 256.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 160.0F, 32.0F, 32, 32, 256.0F, 256.0F);
        GlStateManager.disableBlend();
    }

    public static Vector4d worldToScreen(Vec3 pos, Vec2f size, ScaledResolution resolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.entityRenderer.worldProjectionActive) {
            mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 0);
        }
        Matrix4f projection = PROJECTION.set(net.minecraft.client.renderer.ActiveRenderInfo.PROJECTION);
        Matrix4f modelView = MODEL_VIEW.set(net.minecraft.client.renderer.ActiveRenderInfo.MODELVIEW);
        projection.mul(modelView, net.minecraft.client.renderer.JomlProjection.MATRIX);
        double minX = Math.min(pos.xCoord - size.x, pos.xCoord + size.x);
        double minY = Math.min(pos.yCoord, pos.yCoord + size.y);
        double minZ = Math.min(pos.zCoord - size.x, pos.zCoord + size.x);
        double maxX = Math.max(pos.xCoord - size.x, pos.xCoord + size.x);
        double maxY = Math.max(pos.yCoord, pos.yCoord + size.y);
        double maxZ = Math.max(pos.zCoord - size.x, pos.zCoord + size.x);
        Vector3d[] corners = {
                new Vector3d(minX, minY, minZ), new Vector3d(minX, maxY, minZ),
                new Vector3d(maxX, minY, minZ), new Vector3d(maxX, maxY, minZ),
                new Vector3d(minX, minY, maxZ), new Vector3d(minX, maxY, maxZ),
                new Vector3d(maxX, minY, maxZ), new Vector3d(maxX, maxY, maxZ)};
        Vector4d bounds = null;
        for (int i = 0; i < corners.length; i++) {
            Vector3d point = corners[i];
            Vector3f screen = PROJECTED_POINT;
            boolean visible = net.minecraft.client.renderer.JomlProjection.project(net.minecraft.client.renderer.ActiveRenderInfo.VIEWPORT,
                    (float) (point.x - mc.getRenderManager().viewerPosX),
                    (float) (point.y - mc.getRenderManager().viewerPosY),
                    (float) (point.z - mc.getRenderManager().viewerPosZ),
                    screen);
            if (!visible || screen.z < 0.0F || screen.z >= 1.0F) continue;
            double x = screen.x / resolution.getScaleFactor_double();
            double y = (mc.displayHeight - screen.y) / resolution.getScaleFactor_double();
            if (bounds == null) {
                // The first corner's initialization really seeds maxX with depth, unlike subsequent corners.
                bounds = new Vector4d(x, y, i == 0 ? Math.max(x, screen.z) : screen.z,
                        i == 0 ? Math.max(y, 0.0) : 0.0);
                if (i == 0) continue;
            }
            bounds.x = Math.min(bounds.x, x);
            bounds.y = Math.min(bounds.y, y);
            bounds.z = Math.max(bounds.z, x);
            bounds.w = Math.max(bounds.w, y);
        }
        return bounds;
    }

    public static float drawColoredString(NvgFontRenderer font, String text, int color, float x, float y) {
        if (HUDModule.isSecondaryActive()) {
            Color original = new Color(color, true);
            Color secondary = ColorUtil.secondary(original);
            for (int i = 0; i < text.length(); i++) {
                String character = String.valueOf(text.charAt(i));
                x = font.drawString(character, x, y,
                        ColorUtil.blend(secondary, original, (float) i / text.length() * 0.8F).getRGB());
            }
        } else if (HUDModule.colorSetting.mode == ColorSetting.Mode.RAINBOW) {
            for (int i = 0; i < text.length(); i++) {
                String character = String.valueOf(text.charAt(i));
                int rgb = ColorUtil.rainbow(i * 4.0F, 0.45F, 0.8F).getRGB();
                x = font.drawString(character, x, y, (color & 0xFF000000) | (rgb & 0xFFFFFF));
            }
        } else {
            return font.drawString(text, x, y, color);
        }
        return x;
    }

    public static void drawPotionIcon(int u, int v, float x, float y) {
        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.WorldRenderer buffer = tess.getWorldRenderer();
        buffer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + 18.0F, 0.0).tex(u / 256.0F, (v + 18) / 256.0F).endVertex();
        buffer.pos(x + 18.0F, y + 18.0F, 0.0).tex((u + 18) / 256.0F, (v + 18) / 256.0F).endVertex();
        buffer.pos(x + 18.0F, y, 0.0).tex((u + 18) / 256.0F, v / 256.0F).endVertex();
        buffer.pos(x, y, 0.0).tex(u / 256.0F, v / 256.0F).endVertex();
        tess.draw();
    }

    public static void drawThemedRect(int color, float x1, float y1, float x2, float y2, float blend) {
        if (HUDModule.isSecondaryActive()) {
            Color original = new Color(color, true);
            int other = ColorUtil.blend(ColorUtil.secondary(original), original, blend * 0.85F).getRGB();
            NvgDraw.fillGradientRect(color, other, x1, y1, x2, y2, x1, y1, x2, y1);
        } else NvgDraw.fillRect(color, x1, y1, x2, y2);
    }

    public static void drawAccentGradient(float x, float y, float width, float height, float a, float b) {
        Color base = HUDModule.getHudColor();
        int alpha = Math.max(0, Math.min(255, (int) ((b - a * 0.3F) * 50.0F + 180.0F)));
        base = ColorUtil.withAlpha(base, alpha);
        int left = ColorUtil.brighter(base, 0.5F - (a * 0.08F + 0.08F) * b).getRGB();
        int right = ColorUtil.scale(ColorUtil.secondary(base), 1.0F - (a * 0.3F + 0.2F) * b).getRGB();
        Vector4f p = NvgContext.toPixels(x, y, x + width, y + height);
        NvgDraw.fillGradientRect(left, right, p.x, p.y, p.z, p.w, p.x, p.y, p.z, p.y);
    }

    public static void pushScissor(ScaledResolution context, int flags, float x, float y, float width, float height) {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        double scale = context.getScaleFactor_double();
        GL11.glScissor((int) (x * scale), (int) ((context.getScaledHeight() - (y + height)) * scale),
                (int) (width * scale), (int) (height * scale));
        if (flags != 0) {
            // Same shared stack used by NvgDraw.popScissor; no second OpenGL push here.
            com.adjustclient.core.gui.screen.AdjustComponent rect = new com.adjustclient.core.gui.screen.AdjustComponent();
            rect.x = x; rect.y = y; rect.width = width; rect.height = height;
            ((List<com.adjustclient.core.gui.screen.AdjustComponent>) NvgDraw.SCISSORS).add(rect);
            org.lwjgl.nanovg.NanoVG.nvgIntersectScissor(NvgContext.vg, x, y, width, height);
        }
    }

    public static void drawLine3d(int color, double x, double y, double z, double radius, float lineWidth) {
        GlStateManager.color(0.0F, 0.0F, 0.0F, (color >>> 24) / 255.0F);
        GL11.glLineWidth(lineWidth * 2.0F);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= 16; i++) {
            double angle = i * Math.PI / 8.0;
            GL11.glVertex3d(x + radius * Math.cos(angle), y, z + radius * Math.sin(angle));
        }
        GL11.glEnd();
        glColor(color);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i <= 16; i++) {
            double angle = i * Math.PI / 8.0;
            GL11.glVertex3d(x + radius * Math.cos(angle), y, z + radius * Math.sin(angle));
        }
        GL11.glEnd();
    }

    public static void drawCircle3d(int first, int second, double x, double y, double z, double radius, float lineWidth) {
        for (int pass = 0; pass < 2; pass++) {
            GL11.glLineWidth(pass == 0 ? lineWidth * 2.0F : lineWidth);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            var rotation = com.adjustclient.core.rotation.handler.RenderRotationTracker.INSTANCE.rotation;
            float yaw = rotation != null ? rotation.x : Minecraft.getMinecraft().thePlayer.rotationYaw;
            for (int i = 0; i <= 60; i++) {
                double angle = i * 0.1047197551196598;
                float ratio = Math.abs(MathHelper.wrapAngleTo180_float((float) (Math.toDegrees(angle) + 90.0) - yaw)) / 240.0F;
                Color mixed = ColorUtil.blend(new Color(first, true), new Color(second, true), ratio);
                if (pass == 0) GlStateManager.color(0.0F, 0.0F, 0.0F, mixed.getAlpha() / 255.0F);
                else glColor(mixed.getRGB());
                GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
            }
            GL11.glEnd();
        }
    }

    public static void popScissor() {
        if (NvgContext.currentScale != null) NvgDraw.popScissor();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    public static void setupOverlayRendering(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView == 0 && !ThirdPersonModule.INSTANCE.freeLooking && mc.gameSettings.viewBobbing) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GlStateManager.disableRescaleNormal();
            GlStateManager.enableBlend();
            mc.entityRenderer.setupViewBobbing(partialTicks);
        }
    }

    public static void drawOutlinedBox(AxisAlignedBB box) {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        var tess = net.minecraft.client.renderer.Tessellator.getInstance();
        var buffer = tess.getWorldRenderer();
        for (double y : new double[] {box.minY, box.maxY}) {
            buffer.begin(GL11.GL_LINE_STRIP, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
            buffer.pos(box.minX, y, box.minZ).endVertex();
            buffer.pos(box.maxX, y, box.minZ).endVertex();
            buffer.pos(box.maxX, y, box.maxZ).endVertex();
            buffer.pos(box.minX, y, box.maxZ).endVertex();
            buffer.pos(box.minX, y, box.minZ).endVertex();
            tess.draw();
        }
        buffer.begin(GL11.GL_LINES, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        buffer.pos(box.minX, box.minY, box.minZ).endVertex();
        buffer.pos(box.minX, box.maxY, box.minZ).endVertex();
        buffer.pos(box.maxX, box.minY, box.minZ).endVertex();
        buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
        buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
        buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        buffer.pos(box.minX, box.minY, box.maxZ).endVertex();
        buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
        tess.draw();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void drawRectOutline(float x1, float y1, float x2, float y2, float thickness) {
        drawQuad(x1, y1, x2, y1 - thickness);
        drawQuad(x1, y1 - thickness, x1 - thickness, y2 + thickness);
        drawQuad(x1, y2, x2 + thickness, y2 + thickness);
        drawQuad(x2, y1 - thickness, x2 + thickness, y2);
    }

    public static void drawLine2d(float x1, float y1, float x2, float y2, float lineWidth) {
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(lineWidth);
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    public static void drawBorderedRect(int color, float x1, float y1, float x2, float y2, float thickness) {
        enableBlend();
        glColor(color);
        drawRectOutline(x1, y1, x2, y2, thickness);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void enableBlend() {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    public static void drawFilledBox(AxisAlignedBB box) {
        AxisAlignedBB inner = new AxisAlignedBB(box.minX + 0.0019, box.minY + 0.0019, box.minZ + 0.0019,
                box.maxX - 0.0019, box.maxY - 0.0019, box.maxZ - 0.0019);
        double x1 = inner.minX, y1 = inner.minY, z1 = inner.minZ;
        double x2 = inner.maxX, y2 = inner.maxY, z2 = inner.maxZ;
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y1, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x1, y1, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x1, y2, z1);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x1, y1, z1); GL11.glVertex3d(x2, y1, z1);
        GL11.glVertex3d(x2, y1, z2); GL11.glVertex3d(x1, y1, z2);
        GL11.glEnd();
        GlStateManager.cullFace(GL11.GL_FRONT);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x1, y2, z1); GL11.glVertex3d(x2, y2, z1);
        GL11.glVertex3d(x2, y2, z2); GL11.glVertex3d(x1, y2, z2);
        GL11.glEnd();
        GlStateManager.cullFace(GL11.GL_BACK);
    }

    public static void drawQuad(float x1, float y1, float x2, float y2) {
        float right = Math.max(x1, x2), left = Math.min(x1, x2);
        float bottom = Math.max(y1, y2), top = Math.min(y1, y2);
        var tess = net.minecraft.client.renderer.Tessellator.getInstance();
        var buffer = tess.getWorldRenderer();
        buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
        buffer.pos(right, top, 0.0).endVertex(); buffer.pos(left, top, 0.0).endVertex();
        buffer.pos(left, bottom, 0.0).endVertex(); buffer.pos(right, bottom, 0.0).endVertex();
        tess.draw();
    }

    public static void glColor(int argb) {
        GlStateManager.color((argb >> 16 & 255) / 255.0F, (argb >> 8 & 255) / 255.0F,
                (argb & 255) / 255.0F, (argb >>> 24) / 255.0F);
    }

    public static void begin3d() {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glLineWidth(0.4F);
    }

    public static void end3d() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void h() {
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public static void i() {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableDepth();
    }

    public static void j() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
    }

    public static void k() {
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public static void drawTrail(List<Vec3> points, net.minecraft.client.renderer.entity.RenderManager camera,
                                 Color color, float lineWidth) {
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(770, 771);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        float fraction = 0.0F;
        Color current = color;
        for (Vec3 point : points) {
            current = ColorUtil.withAlpha(current, (int) (color.getAlpha() * fraction));
            glColor(current.getRGB());
            fraction += 1.0F / points.size();
            GL11.glVertex3d(point.xCoord - camera.viewerPosX,
                    point.yCoord - camera.viewerPosY, point.zCoord - camera.viewerPosZ);
        }
        GL11.glEnd();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public static AxisAlignedBB entityBoxAt(Entity entity, double x, double y, double z, double expand) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        double halfX = (box.maxX - box.minX) * 0.5;
        double halfZ = (box.maxZ - box.minZ) * 0.5;
        return new AxisAlignedBB(x - halfX - expand, y, z - halfZ - expand,
                x + halfX + expand, y + box.maxY - box.minY + (entity.isSneaking() ? -0.25 : 0.0) + expand,
                z + halfZ + expand);
    }

    public static void drawShadowRect(Color color, float x, float y, float width) {
        int argb = ColorUtil.withAlpha(color, Math.min(color.getAlpha(), 230)).getRGB();
        float middle = x + width / 2.0F;
        Vector4f left = NvgContext.toPixels(x, y, middle, y + 0.5F);
        NvgDraw.fillGradientRect(0, argb, left.x, left.y, left.z, left.w, left.x, left.y, left.z, left.y);
        Vector4f right = NvgContext.toPixels(middle, y, x + width, y + 0.5F);
        NvgDraw.fillGradientRect(argb, 0, right.x, right.y, right.z, right.w, right.x, right.y, right.z, right.y);
    }

    public static Vector4d projectEntityBounds(Entity entity, ScaledResolution resolution,
                                               boolean interpolate, float partialTicks) {
        double x = entity.posX, y = entity.posY, z = entity.posZ;
        double oldX = entity.lastTickPosX, oldY = entity.lastTickPosY, oldZ = entity.lastTickPosZ;
        if (ServerPositionTracker.INSTANCE != null) {
            var tracked = ServerPositionTracker.INSTANCE.findTrackedPosition(entity);
            if (tracked != null) {
                x = tracked.x;
                y = tracked.y;
                z = tracked.z;
                oldX = tracked.previousX;
                oldY = tracked.previousY;
                oldZ = tracked.previousZ;
            }
        }
        if (interpolate) {
            x = oldX + (x - oldX) * partialTicks;
            y = oldY + (y - oldY) * partialTicks;
            z = oldZ + (z - oldZ) * partialTicks;
        }
        if (entity instanceof EntityItem && !((com.adjustclient.modules.impl.render.ItemPhysicsModule) ItemPhysicsModule.INSTANCE).isEnabled()) {
            EntityItem item = (EntityItem) entity;
            y += MathHelper.sin(item.getAge() / 10.0F + item.hoverStart) * 0.1F + 0.1F;
        }
        float height = entity.height;
        if (entity instanceof EntityPlayer) {
            height += entity.isSneaking() ? 0.0F : 0.2F;
            if (DebugEpsteinSubCommand.enabled && !entity.isUser()) height /= 2.0F;
        } else if (entity instanceof EntityZombie && ((EntityZombie) entity).isChild()) height /= 1.75F;
        return worldToScreen(new Vec3(x, y, z), new Vec2f(entity.width / 1.65F, height), resolution);
    }
}
