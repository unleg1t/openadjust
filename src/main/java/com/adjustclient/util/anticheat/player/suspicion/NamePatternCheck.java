package com.adjustclient.util.anticheat.player.suspicion;

import java.util.List;
import java.util.regex.Pattern;

final class NamePatternCheck extends NameCheck {
    public Object field4;
    @Override public double score(String name, String skinUrl, double score) {
        if (!name.matches("^[A-Za-z]{1,9}_[A-Za-z]{6}$")) return score;
        List<String> prefixes = List.of("Mal", "Whale", "Liddell", "Bestow", "RRR", "ONE",
                "Eudemonia", "DuanQiao", "Toprak", "Coke", "dashen", "iceaa66", "ice324", "Mayi",
                "mchka", "XBI", "zuichu", "coke", "Ght", "WhiteDog", "JINGYU");
        return score * 1.1 + (prefixes.contains(name.split("_")[0]) ? 0.925 : 0.8);
    }
}
