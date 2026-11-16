package com.adjustclient.ui.hud.toggled;

import com.adjustclient.event.Listener;
import com.adjustclient.module.Module;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.ui.hud.HudElement;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatColor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.adjustclient.Adjust;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.ColorUtil;
import com.adjustclient.script.engine.ScriptManager;
import com.adjustclient.service.util.ServiceUtil;

public final class ToggledList extends HudElement implements Listener {
    public static ToggledList INSTANCE;

    public ToggledSettings settings;
    public com.adjustclient.game.movable.SnapGuide bounds;
    public List<ToggledEntry> entries;
    public List<ToggledEntry> visible;
    public float totalHeight;
    public int moduleListVersion;
    public boolean dirty;
    public Object field36;

    public ToggledList(ToggledSettings settings) {
        this.movable = com.adjustclient.ui.hud.Anchor.TOP_RIGHT;
        this.settings = settings;
        this.bounds = Adjust.movableManager.createGuide();
        this.moduleListVersion = -1;
        this.dirty = true;
        INSTANCE = this;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Initialisation (run once by the factory after construction): creates the two entry lists with the capacity of the module
     * collection, registers an entry for every module and installs a change listener on HUD > Toggled > Enabled.
     */
    public void initialize() {
        Collection<Module> modules = Adjust.INSTANCE.moduleManager.view;
        this.entries = new ArrayList<>(modules.size());
        this.visible = new ArrayList<>(modules.size());
        modules.forEach(module -> this.addEntry(module));
        this.settings.enabled.addListener(() -> {
            for (ToggledEntry entry : this.entries) {
                entry.xAnim = null;
                entry.yAnim = null;
            }
        });
    }

    /** Adds an entry for the module: owner = this, heightAnim = new Animation(easing, 250 ms) started now. */
    public void addEntry(Module module) {
        ToggledEntry entry = new ToggledEntry();
        entry.owner = this;
        entry.module = module;
        entry.heightAnim = new Animation(Easing.LINEAR, 250L);
        this.entries.add(entry);
    }

    /** Recomputes text / width / target of every entry, rebuilds the visible list and sorts the entries. */
    public void refresh() {
        if (this.entries.size() > 0) {
            this.entries.forEach(entry -> this.updateEntry(entry));
        }
        this.update();
        Collections.sort(this.entries);                                // ComparableTimSort
        this.dirty = false;
    }

    private void updateEntry(ToggledEntry entry) {
        Module module = entry.module;
        String name = module.getName();
        String suffix = module.getSuffix();
        if (suffix == null || !this.settings.suffix.isEnabled()) {
            entry.text = name;
        } else {
            entry.text = name + " " + ChatColor.GRAY + suffix;
        }
        entry.width = Fonts.TAHOMA_12.getStringWidth(entry.text);
        if ((module.flags & 4) != 0 && (module.flags & 2) != 0) {
            entry.removing = false;
            entry.targetX = -entry.width;
            entry.active = true;
        } else if (entry.active) {
            if (entry.xAnim != null && entry.xAnim.getValue() == entry.targetX && entry.removing) {
                entry.xAnim = null;
                entry.yAnim = null;
                entry.active = false;
            } else {
                entry.removing = true;
                entry.targetX = 4.0F;
            }
        }
    }

    /**
     * Keeps the entries in sync with the script manager's list revision: when the script manager's revision differs
     * from the stored one, stale entries are checked and any entries from {@link ScriptManager#getScripts()} are appended.
     */
    private void sync() {
        ScriptManager moduleManager = ScriptManager.INSTANCE;
        if (moduleManager == null) {
            return;
        }
        int version = moduleManager.revision;
        if (version != this.moduleListVersion) {
            this.moduleListVersion = version;
            this.entries.removeIf(entry -> false);
            // for each element of ScriptManager.getScripts() a new ToggledEntry(owner = this) is appended
            for (Object element : moduleManager.getScripts()) {
                this.entries.add(newEntryFor(element));
            }
            this.dirty = true;
        }
    }

    private ToggledEntry newEntryFor(Object element) {
        ToggledEntry entry = new ToggledEntry();
        entry.owner = this;
        entry.module = (Module) element;
        entry.heightAnim = new Animation(Easing.LINEAR, 250L);
        return entry;
    }

    @Override
    public void update() {
        this.sync();
        this.visible.clear();
        int row = 0;
        for (ToggledEntry entry : this.entries) {
            this.updateEntry(entry);
            if (!entry.active) {
                continue;
            }
            float rowHeight = ServiceUtil.ahmedElSisiMode ? entry.heightAnim.getValue() : this.settings.padding.getValue().rowHeight;
            entry.targetY = (float) row * rowHeight;
            if (entry.xAnim == null) {
                entry.xAnim = new Animation(Easing.EASE_OUT_EXPO, 600L);
                entry.xAnim.value = 3.5F;                               // initial value
            }
            if (entry.yAnim == null) {
                entry.yAnim = new Animation(Easing.EASE_OUT_EXPO, 600L);
                entry.yAnim.value = entry.targetY - 16.0F;
            } else if (entry.removing) {
                entry.targetY = entry.targetY - 16.0F;
            }
            if (!entry.removing) {
                row++;
            }
            this.visible.add(entry);
        }
    }

    @Override
    public void render(ScaledResolution sr) {
        if (!this.settings.enabled.isEnabled()) {
            return;
        }
        if (this.dirty) {
            this.update();
            this.entries.sort(null);
            this.dirty = false;
        }

        Color primary = HUDModule.getHudColor();
        Color secondary;
        if (HUDModule.isSecondaryActive()) {
            secondary = ColorUtil.secondary(primary);                // colour util
        } else {
            secondary = new Color((int) (primary.getRed() * 0.8F), (int) (primary.getGreen() * 0.8F), (int) (primary.getBlue() * 0.8F), primary.getAlpha());
        }

        float maxY = 0.0F;
        ToggledEntry previous = null;
        int count = this.visible.size();
        for (int i = 0; i < count; i++) {
            ToggledEntry entry = this.visible.get(i);
            entry.render(sr, previous, i == count - 1, primary, secondary);
            if (!entry.removing) {
                previous = entry;
            }
            maxY = Math.max(maxY, entry.yAnim.getValue());
        }

        // overall height, scaled by half the scale factor
        this.totalHeight = (maxY + this.settings.padding.getValue().rowHeight) * (float) (0.5 * sr.getScaleFactor_double());
        // bounds holder
        this.bounds.active = true;
        float extra = this.settings.border.getValue() == ToggledBorder.FULL ? 1.0F : 0.0F;
        this.bounds.x = 0.0F;
        this.bounds.y = this.totalHeight + extra;
    }
}
