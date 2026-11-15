package com.adjustclient.module.impl.render.hud.playerlist;

import net.minecraft.util.ChatColor;

public enum StatColorScale {

    KILLS {
        @Override
        public ChatColor getColor(double value) {
            if (value < 1000.0) {
                return ChatColor.GRAY;
            } else if (value < 2000.0) {
                return ChatColor.WHITE;
            } else if (value < 3500.0) {
                return ChatColor.GOLD;
            } else if (value < 5000.0) {
                return ChatColor.DARK_GREEN;
            } else if (value < 10000.0) {
                return ChatColor.RED;
            } else if (value < 15000.0) {
                return ChatColor.DARK_RED;
            } else if (value < 20000.0) {
                return ChatColor.LIGHT_PURPLE;
            } else {
                return ChatColor.DARK_PURPLE;
            }
        }
    },

    PING {
        @Override
        public ChatColor getColor(double value) {
            if (value < 100.0) {
                return ChatColor.DARK_PURPLE;
            } else if (value < 250.0) {
                return ChatColor.LIGHT_PURPLE;
            } else if (value < 500.0) {
                return ChatColor.DARK_RED;
            } else if (value < 750.0) {
                return ChatColor.RED;
            } else if (value < 1000.0) {
                return ChatColor.DARK_GREEN;
            } else if (value < 1500.0) {
                return ChatColor.GOLD;
            } else if (value < 2000.0) {
                return ChatColor.WHITE;
            } else {
                return ChatColor.GRAY;
            }
        }
    },

    GOLD {
        @Override
        public ChatColor getColor(double value) {
            if (value < 500.0) {
                return ChatColor.GRAY;
            } else if (value < 1500.0) {
                return ChatColor.WHITE;
            } else if (value < 2500.0) {
                return ChatColor.GOLD;
            } else if (value < 5000.0) {
                return ChatColor.DARK_GREEN;
            } else if (value < 7500.0) {
                return ChatColor.RED;
            } else if (value < 10000.0) {
                return ChatColor.DARK_RED;
            } else if (value < 15000.0) {
                return ChatColor.LIGHT_PURPLE;
            } else {
                return ChatColor.DARK_PURPLE;
            }
        }
    },

    KDR {
        @Override
        public ChatColor getColor(double value) {
            if (value < 1.5) {
                return ChatColor.GRAY;
            } else if (value < 3.5) {
                return ChatColor.WHITE;
            } else if (value < 5.0) {
                return ChatColor.GOLD;
            } else if (value < 10.0) {
                return ChatColor.DARK_GREEN;
            } else if (value < 20.0) {
                return ChatColor.RED;
            } else if (value < 50.0) {
                return ChatColor.DARK_RED;
            } else if (value < 100.0) {
                return ChatColor.LIGHT_PURPLE;
            } else {
                return ChatColor.DARK_PURPLE;
            }
        }
    },

    WLR {
        @Override
        public ChatColor getColor(double value) {
            if (value < 1.0) {
                return ChatColor.GRAY;
            } else if (value < 2.0) {
                return ChatColor.WHITE;
            } else if (value < 3.0) {
                return ChatColor.GOLD;
            } else if (value < 5.0) {
                return ChatColor.DARK_GREEN;
            } else if (value < 10.0) {
                return ChatColor.RED;
            } else if (value < 15.0) {
                return ChatColor.DARK_RED;
            } else if (value < 50.0) {
                return ChatColor.LIGHT_PURPLE;
            } else {
                return ChatColor.DARK_PURPLE;
            }
        }
    },

    WIN_STREAK {
        @Override
        public ChatColor getColor(double value) {
            if (value < 5.0) {
                return ChatColor.GRAY;
            } else if (value < 10.0) {
                return ChatColor.WHITE;
            } else if (value < 20.0) {
                return ChatColor.GOLD;
            } else if (value < 35.0) {
                return ChatColor.DARK_GREEN;
            } else if (value < 50.0) {
                return ChatColor.RED;
            } else if (value < 75.0) {
                return ChatColor.DARK_RED;
            } else if (value < 100.0) {
                return ChatColor.LIGHT_PURPLE;
            } else {
                return ChatColor.DARK_PURPLE;
            }
        }
    };

    public abstract ChatColor getColor(double value);

}
