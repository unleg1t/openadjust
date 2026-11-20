package com.adjustclient.modules.impl.render.streamer;

import java.util.Arrays;

enum FirstNameSource {
    LOBBY("dynamiclobby", 'L'), MINI("mini", 'm'), MEGA("mega", 'M');
    final String prefix;
    final char abbreviation;
    FirstNameSource(String prefix, char abbreviation) {
        this.prefix = prefix;
        this.abbreviation = abbreviation;
    }
    static FirstNameSource fromPrefix(Object prefix) {
        java.util.Objects.requireNonNull(prefix);
        if (prefix instanceof Character character) {
            return Arrays.stream(values()).filter(source -> source.abbreviation == character)
                    .findFirst().orElse(null);
        }
        return Arrays.stream(values()).filter(source -> source.prefix.equals(prefix))
                .findFirst().orElse(null);
    }
}
