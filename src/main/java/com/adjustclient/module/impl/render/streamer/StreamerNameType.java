package com.adjustclient.module.impl.render.streamer;

import com.adjustclient.Adjust;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.name.NameGenerator;
import com.adjustclient.util.name.RandomCharNameGenerator;
import com.adjustclient.util.name.WordNameGenerator;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public enum StreamerNameType implements ModeOption {
    USERNAMES("Usernames", random -> new WordNameGenerator(random)),
    FIRST_NAMES("First Names", random -> new RandomCharNameGenerator(random));
    private final String displayName;
    private final Function<Random, NameGenerator> factory;
    StreamerNameType(String displayName, Function<Random, NameGenerator> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }
    public String getName() { return displayName; }
    public NameGenerator createGenerator(String name) {
        int nameHash = name.hashCode();
        int identity;
        if (Adjust.INSTANCE.screenStack.partyService.party != null) {
            identity = Objects.hash(0, Adjust.INSTANCE.screenStack.partyService.party.id);
        } else {
            identity = Objects.hash(0, 0L ^ 0x20L);
        }
        return factory.apply(new Random(((long) nameHash << 32) | Integer.toUnsignedLong(identity)));
    }

    @SuppressWarnings("unused")
    private static boolean vendorHwidComputed;

    @SuppressWarnings("unused")
    private static long vendorCachedHwid;

    @SuppressWarnings("unused")
    private static long vendorSeedSalt() {
        if (!vendorHwidComputed) {
            vendorHwidComputed = true;
            vendorCachedHwid = com.adjustclient.backend.hwid.HardwareId.computeHardwareId();
        }
        return vendorCachedHwid ^ 0x20L;
    }
}
