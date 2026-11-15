package com.adjustclient.ui.mainmenu;

import com.adjustclient.Adjust;
import com.adjustclient.changelog.Changelog;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.util.HashUtil;
import com.adjustclient.util.Timer;
import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public final class VoronoiBackground {
    static Geometry diagram;
    static Color previousColor;
    public static Animation transition;
    static Runnable afterClose;
    static final Map<Geometry, VoronoiCell> cells = new HashMap<>();
    static final Polygon hitPolygon = new Polygon();
    static final Timer timer = new Timer(0L);
    static final java.util.function.Function<Geometry, VoronoiCell> createCell =
            key -> new VoronoiCell();
    public static boolean opening;
    static int seed;
    static int diagramWidth;
    static int diagramHeight;
    static boolean closing;

    public static Set<Geometry> neighbours(Geometry geometry) {
        Set<Geometry> result = new HashSet<>();
        for (int i = 0; i < diagram.getNumGeometries(); i++) {
            Geometry other = diagram.getGeometryN(i);
            if (other != geometry && geometry.touches(other)) result.add(other);
        }
        return result;
    }

    public static void close() {
        GuiScreen current = Minecraft.getMinecraft().currentScreen;
        if (current instanceof AdjustMainMenu) ((AdjustMainMenu) current).interactive = false;
        closing = true;
        afterClose = com.adjustclient.changelog.ChangelogPreview.OPEN_SCREEN;
        transition = new Animation(Easing.LINEAR, 145L);
        transition.value = 1.0F;
        transition.startedAt = System.currentTimeMillis();
        transition.from = transition.value;
        transition.finished = false;
    }

    public static void resize(int width, int height) {
        if (width == diagramWidth && height == diagramHeight) return;
        seed = HashUtil.hash(Changelog.latest.id + Adjust.username);
        List<Coordinate> sites = createSites(width, height);
        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(sites);
        diagram = builder.getDiagram(new GeometryFactory());
        diagramWidth = width;
        diagramHeight = height;
        cells.clear();
    }

    public static boolean contains(int x, int y, Coordinate[] coordinates) {
        hitPolygon.reset();
        for (Coordinate coordinate : coordinates) hitPolygon.addPoint((int) coordinate.x, (int) coordinate.y);
        return hitPolygon.contains(x, y);
    }

    public static void draw(GuiScreen screen, int mouseX, int mouseY, Color baseColor) {
        if (diagram == null) return;
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.bindTexture(0);
        GL11.glTranslatef(-(mouseX - screen.width * 0.5F) / 60.0F,
                -(mouseY - screen.height * 0.5F) / 60.0F, 0.0F);
        Random random = new Random(seed + 1);
        for (int i = 0; i < diagram.getNumGeometries(); i++) {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            Geometry geometry = diagram.getGeometryN(i);
            VoronoiCell cell = cells.computeIfAbsent(geometry, createCell);
            Coordinate[] coordinates = geometry.getCoordinates();
            float variation = random.nextFloat();
            float brightness = random.nextFloat() * 0.038F + 0.8825F;
            if (contains(mouseX, mouseY, coordinates)) {
                cell.highlight(1.0F);
                for (Geometry neighbour : neighbours(geometry)) {
                    VoronoiCell adjacent = cells.computeIfAbsent(neighbour, createCell);
                    if (adjacent != cell) adjacent.highlight(0.78F);
                }
            }
            if (cell.animation != null) {
                cell.update();
                brightness += (variation * 0.00015F + 0.02475F) * cell.animation.value;
                if (cell.animation.value == 0.0F && cell.animation.finished) cell.animation = null;
            }
            Color color = blendBackground(baseColor);
            float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            hsb[1] = Math.max(0.01F, Math.min(0.7F, hsb[1]));
            hsb[2] = Math.max(0.61F, Math.min(0.78F, hsb[2]));
            hsb[1] *= random.nextFloat() * 0.001105F + 0.5F;
            hsb[2] *= random.nextFloat() * 0.00102F + 0.4F;
            Color shaded = com.adjustclient.render.ColorUtil.scale(Color.getHSBColor(hsb[0], hsb[1], hsb[2]), brightness);
            GlStateManager.color(shaded.getRed() / 255.0F, shaded.getGreen() / 255.0F,
                    shaded.getBlue() / 255.0F, 1.0F);
            for (Coordinate coordinate : coordinates) GL11.glVertex2d(coordinate.x, coordinate.y);
            GL11.glEnd();
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    public static Color blendBackground(Color color) {
        if (previousColor == null) return color;
        if (transition == null) transition = new Animation(Easing.LINEAR, 220L);
        transition.update(1.0F);
        if (transition.finished) {
            previousColor = null;
            transition = null;
            return color;
        }
        return com.adjustclient.render.ColorUtil.blend(color, previousColor, transition.value);
    }

    private static List<Coordinate> createSites(int width, int height) {
        Random random = new Random(seed);
        int count = random.nextInt(18) + 114;
        List<Coordinate> sites = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sites.add(new Coordinate(random.nextDouble() * width, random.nextDouble() * height));
        }
        return sites;
    }

    public static void reset() {
        if (transition == null) return;
        if (timer.lastMs + 500L <= System.currentTimeMillis()) {
            timer.lastMs = System.currentTimeMillis();
            transition.value = -1.3F;
            transition.duration = 280L;
            transition.startedAt = System.currentTimeMillis();
            transition.from = transition.value;
            transition.finished = false;
        }
    }

    public static float update() {
        if (transition == null) return 1.0F;
        if (afterClose == null) return Math.max(0.0F, Math.min(1.0F, transition.value));
        if (transition.finished && closing) {
            afterClose.run();
            closing = false;
            return 0.0F;
        }
        if (opening) {
            transition.duration = 100L;
            transition.update(1.0F);
            if (transition.value == 1.0F) {
                opening = false;
                closing = false;
                afterClose = null;
                transition = null;
                return 1.0F;
            }
        } else {
            transition.update(0.0F);
        }
        return Math.max(0.0F, Math.min(1.0F, transition.value));
    }
}
