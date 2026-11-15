package com.adjustclient.util.name;

import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import com.adjustclient.auth.RandomNames;

public class WordNameGenerator extends NameGenerator {
    public Random random;
    public Object field8;
    public WordNameGenerator(Random random) { this.random = random; }
    @Override public String generate() {
        boolean useWords = random.nextBoolean();
        String prefix = "", suffix = null;
        if (random.nextDouble() > 0.8) {
            switch (random.nextInt(8)) {
                case 0: prefix = "_"; break;
                case 1: prefix = "xx"; if (random.nextDouble() > 0.4) suffix = "xx"; break;
                case 2: prefix = "ii"; if (random.nextDouble() > 0.7) suffix = "ii"; break;
                case 3: prefix = "x"; if (random.nextDouble() > 0.8) suffix = "x"; break;
                case 4:
                    boolean taiwan = random.nextBoolean();
                    suffix = taiwan ? (random.nextBoolean() ? "_TW" : "TW")
                            : (random.nextBoolean() ? "_CN" : "CN");
                    break;
                case 5: suffix = random.nextBoolean() ? "PVP" : "PvP"; break;
                case 6: suffix = "YT"; break;
                case 7: suffix = "Plays"; break;
            }
        }
        String result = prefix;
        try {
            String[] adjectives = RandomNames.adjectives;
            String[] nouns = RandomNames.nouns;
            if (adjectives != null) {
                String adjective = StringUtils.capitalize(adjectives[random.nextInt(adjectives.length - 1)]);
                if (nouns != null) {
                    String noun = StringUtils.capitalize(nouns[random.nextInt(nouns.length - 1)]);
                    if (!useWords) {
                        result = switch (random.nextInt(14)) {
                            case 0 -> prefix + noun + "Is" + adjective;
                            case 1 -> prefix + noun + "May" + adjective;
                            case 2 -> prefix + adjective + RandomStringUtils.randomNumeric(random.nextInt(2) + 1) + noun;
                            case 3 -> prefix + (adjective + noun).toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(2) + 1);
                            case 4 -> prefix + adjective.toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(6) + 1);
                            case 5 -> prefix + adjective.toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(2) + 1) + noun.toLowerCase();
                            case 6 -> prefix + noun.toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(7) + 1);
                            case 7 -> prefix + (adjective + noun).toLowerCase();
                            case 8 -> prefix + adjective.toLowerCase() + RandomStringUtils.randomNumeric(1) + noun.toLowerCase();
                            case 9 -> prefix + adjective.toLowerCase() + "_" + RandomStringUtils.randomNumeric(random.nextInt(6) + 1);
                            case 10 -> prefix + noun.toLowerCase() + "_" + RandomStringUtils.randomNumeric(random.nextInt(6) + 1);
                            case 11 -> prefix + adjective + "_" + RandomStringUtils.randomNumeric(random.nextInt(6) + 1);
                            case 12 -> prefix + noun + "_" + RandomStringUtils.randomNumeric(random.nextInt(6) + 1);
                            case 13 -> prefix + noun + "Not" + adjective;
                            default -> prefix;
                        };
                    } else if (RandomNames.names != null) {
                        String[] words = RandomNames.names;
                        String first = words[random.nextInt(words.length - 1)];
                        String second = words[random.nextInt(words.length - 1)];
                        result = switch (random.nextInt(12)) {
                            case 0 -> prefix + first + second + "_" + RandomStringUtils.randomNumeric(1);
                            case 1 -> prefix + first + RandomStringUtils.randomNumeric(random.nextInt(7) + 1);
                            case 2 -> prefix + first + "_" + second;
                            case 3 -> prefix + (first + second).toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(2) + 1);
                            case 4 -> prefix + first.toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(2) + 1) + second.toLowerCase();
                            case 5 -> prefix + adjective + "_" + first;
                            case 6 -> prefix + first.toLowerCase() + RandomStringUtils.randomNumeric(random.nextInt(7) + 1);
                            case 7 -> prefix + adjective + RandomStringUtils.randomNumeric(random.nextInt(2) + 1) + first;
                            case 8 -> prefix + second + adjective;
                            case 9 -> prefix + first + "da" + (random.nextDouble() <= 0.5 ? noun : StringUtils.capitalize(noun));
                            case 10 -> prefix + first + "The" + StringUtils.capitalize(noun);
                            case 11 -> prefix + (first + second).toLowerCase();
                            default -> prefix;
                        };
                    }
                }
            }
        } catch (Exception ignored) {
            // Failed word-list access leaves only the decoration prefix.
        }
        if (suffix != null) result += suffix;
        return result.length() < 17 ? result : result.substring(0, 16);
    }
}
