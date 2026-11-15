package com.adjustclient.util.anticheat.player.suspicion;

import java.util.List;
import java.util.regex.Pattern;

final class AltShopNameCheck extends NameCheck {
    public Object field4;
    private static final Pattern[] PATTERNS;
    static {
        PATTERNS = new Pattern[] {
            Pattern.compile("^Blossom_[A-Za-z0-9]{6}$"),
            Pattern.compile("^qwq1_[A-Za-z0-9]{6}$"),
            Pattern.compile("^Chi_[A-Za-z]{6}_Dao$"),
            Pattern.compile("^Biscuits_[A-Za-z0-9]{6}$"),
            Pattern.compile("^HY_[A-Za-z0-9]{6}$"),
            Pattern.compile("^Ya__[A-Za-z0-9]{6}$"),
            Pattern.compile("^Hitori[A-Za-z0-9]{5}$"),
            Pattern.compile("^EKKOREE_[A-Za-z]{8}$"),
            Pattern.compile("^Ama_[A-Za-z]{6}_ranth$"),
            Pattern.compile("^Peppe_[A-Za-z0-9]{10}$"),
            Pattern.compile("^HuaJicow_[A-Za-z0-9]{6,7}$"),
            Pattern.compile("^Xmhk_[A-Za-z0-9]{6}$"),
            Pattern.compile("^Tzi_[A-Za-z0-9]{6}$"),
            Pattern.compile("^WGG_[A-Za-z0-9]{6}$"),
            Pattern.compile("^ColaAlts_[A-Za-z0-9]{6}$"),
            Pattern.compile("^MCLEAKS_[A-Za-z0-9]{8}$"),
            Pattern.compile("^DortHit_[a-z0-9]{8}$"),
            Pattern.compile("^[a-z]{2,4}_[a-z0-9]{10}$"),
            Pattern.compile("^Duan_\\d_\\d{1,2}_[A-Za-z0-9]{6}$"),
            Pattern.compile("^[A-Za-z]{2,3}_[A-Za-z0-9]{6}_\\d{2}_\\d{2}$")
        };
    }
    @Override public double score(String name, String skinUrl, double score) {
        for (Pattern pattern : PATTERNS) {
            if (pattern.matcher(name).matches()) {
                double bonus = 0.925;
                if (SkinNameSuspicionCheck.isCommonSkin(skinUrl)) {
                    bonus = 1.025;
                    score *= 1.05;
                }
                return bonus + score * 1.1;
            }
        }
        return score;
    }
}
