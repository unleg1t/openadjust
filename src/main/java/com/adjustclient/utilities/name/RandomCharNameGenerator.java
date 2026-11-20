package com.adjustclient.utilities.name;

import com.adjustclient.utilities.auth.RandomNames;
public final class RandomCharNameGenerator extends NameGenerator {
    public java.util.Random random;
    public Object field8;
    public RandomCharNameGenerator(java.util.Random random) { this.random = random; }
    @Override public String generate() {
        String[] names = RandomNames.names;
        return names.length == 1 ? names[0] : names[com.adjustclient.utilities.RandomUtil.nextInt(random, 0, names.length)];
    }
}
