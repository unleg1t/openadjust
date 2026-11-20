package com.adjustclient.modules.impl.render.streamer;

import java.util.Objects;

final class StreamerName {
    static double seedFactor;
    final String originalName;
    final String replacement;
    final FirstNameSource source;
    StreamerName(String originalName, String replacement, FirstNameSource source) {
        this.originalName = originalName;
        this.replacement = replacement;
        this.source = source;
    }
    StreamerName(String name) {
        FirstNameSource kind;
        String suffix;
        if (Character.isDigit(name.charAt(1))) {
            kind = FirstNameSource.fromPrefix(name.charAt(0));
            suffix = name.substring(1);
        } else {
            int firstDigit = 0;
            while (firstDigit < name.length() && !Character.isDigit(name.charAt(firstDigit))) firstDigit++;
            kind = FirstNameSource.fromPrefix(name.substring(0, firstDigit));
            suffix = name.substring(firstDigit);
        }
        if (kind == null) throw new RuntimeException("Type not found?");
        int numericId = Integer.parseInt(suffix.replaceAll("\\D", ""));
        int seed = kind.ordinal() ^ (int) (seedFactor * 1000000.0) ^ numericId
                ^ com.adjustclient.utilities.HashUtil.hash(com.adjustclient.Adjust.username);
        String candidate;
        for (int candidateSeed = seed; ; candidateSeed++) {
            java.util.Random random = new java.util.Random(candidateSeed);
            int count = random.nextInt(kind == FirstNameSource.MEGA ? 1 : 2) + 1;
            StringBuilder generated = new StringBuilder();
            for (int i = 0; i < count; i++) generated.append("1234567890".charAt(random.nextInt(10)));
            // The non-MEGA path advances the random state once before selecting the suffix letter.
            if (kind != FirstNameSource.MEGA) random.nextInt();
            generated.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(random.nextInt(26)));
            candidate = generated.toString();
            if (candidateSeed + 1 >= 17 || !candidate.equalsIgnoreCase(name)) break;
        }
        this.originalName = suffix;
        this.replacement = candidate;
        this.source = kind;
    }
    boolean matches(String name) {
        FirstNameSource kind = FirstNameSource.fromPrefix(name.charAt(0));
        return kind != null && originalName.equals(name.substring(1)) && source == kind;
    }
    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof StreamerName name)) return false;
        return Objects.equals(originalName, name.originalName) && source == name.source;
    }
    @Override public int hashCode() { return Objects.hash(originalName, source); }
    String display(boolean abbreviated) {
        return (abbreviated ? String.valueOf(source.abbreviation) : source.prefix) + replacement;
    }
}
