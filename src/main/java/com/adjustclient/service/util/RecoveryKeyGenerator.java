package com.adjustclient.service.util;

import com.adjustclient.auth.RandomNames;
import com.adjustclient.util.RandomUtil;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

public class RecoveryKeyGenerator {
    public static String generate() {
        StringBuilder phrase = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            String[] words = Math.random() > 0.5 ? RandomNames.adjectives : RandomNames.nouns;
            String word = words.length == 1 ? words[0] : words[RandomUtil.RANDOM.nextInt(words.length)];
            if (i != 0) phrase.append(' ');
            phrase.append(word);
        }
        return phrase.toString().toLowerCase();
    }

    public static long hash(String phrase) {
        if (phrase == null || phrase.length() < 6) throw new RuntimeException();
        return Hashing.sipHash24().hashBytes(phrase.toLowerCase().getBytes(StandardCharsets.UTF_16)).asLong();
    }
}
