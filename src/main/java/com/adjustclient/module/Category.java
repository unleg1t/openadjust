package com.adjustclient.module;

import com.adjustclient.setting.ModeOption;
import java.util.ArrayList;
import java.util.List;

public class Category implements ModeOption {
    public static final Category COMBAT = new Category("Combat");
    public static final Category EXPLOITS = new Category("Exploits");
    public static final Category MOVEMENT = new Category("Movement");
    public static final Category PLAYER = new Category("Player");
    public static final Category RENDER = new Category("Render");
    private static final Category[] VALUES = {COMBAT, EXPLOITS, MOVEMENT, PLAYER, RENDER};

    public String name;
    public List<Module> modules;
    public int field12;
    public byte id;
    public Object field20;

    private Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Category[] values() { return VALUES.clone(); }
    public List<Module> getModules() { return modules; }
}
