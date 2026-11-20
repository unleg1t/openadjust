package com.adjustclient.utilities.hypixel;

import com.adjustclient.utilities.game.server.ServerGame;
import com.adjustclient.utilities.hypixel.game.*;
import com.adjustclient.utilities.hypixel.game.bedwars.BedWarsGameState;
import com.adjustclient.utilities.hypixel.game.build.*;
import com.adjustclient.utilities.hypixel.game.duels.DuelsGameState;
import com.adjustclient.utilities.hypixel.game.hideandseek.HideAndSeekGameState;
import com.adjustclient.utilities.hypixel.game.murdermystery.MurderMysteryGameState;
import com.adjustclient.utilities.hypixel.game.pit.PitGameState;
import com.adjustclient.utilities.hypixel.game.replay.*;
import com.adjustclient.utilities.hypixel.game.zombies.ZombiesGameState;
import java.util.Arrays;
import net.minecraft.util.StringUtils;

public enum HypixelGame implements ServerGame<HypixelGameState, HypixelGame> {
    BUILD_BATTLE("Build Battle", 0xb0,
            BuildBattleGameState::new),
    UHC_CHAMPIONS("UHC Champions", 0,
            UhcChampionsGameState::new),
    HOUSING("Housing", 0x84,
            HousingGameState::new),
    WOOL_WARS("Wool Wars", 0x90),
    BLITZ_SG("Blitz SG", 0x20),
    MURDER_MYSTERY("Murder Mystery", 0x90,
            MurderMysteryGameState::new),
    SKYWARS("SkyWars", 8,
            SkyWarsGameState::new),
    BED_WARS("Bed Wars", 8,
            BedWarsGameState::new),
    BED_WARS_PRACTICE("Bed Wars Practice", 0x94),
    REPLAY("Replay", 0x12,
            ReplayGameState::new),
    ATLAS("Atlas", 0x12,
            AtlasGameState::new),
    CAPTURE_THE_WOOL("Capture The Wool", 0x74),
    THE_HYPIXEL_PIT("Pit", 0xf4,
            PitGameState::new),
    DUELS("Duels", 8,
            DuelsGameState::new),
    GRINCH_SIMULATOR("Grinch Simulator", 0x90),
    HALLOWEEN_SIMULATOR("Halloween Simulator", 0x90),
    EASTER_SIMULATOR("Easter Simulator", 0x90),
    SCUBA_SIMULATOR("Scuba Simulator", 0x2b0,
            ScubaSimulatorGameState::new),
    TNT_TAG("TNT Tag", 0x90),
    TNT_RUN("TNT Run", 0x90),
    THE_TNT_GAMES("The TNT Games", 0x90),
    MINI_WALLS("Mini Walls", 0x80),
    MEGA_WALLS("Mega Walls", 0x20,
            MegaWallsGameState::new),
    SHEEP_WARS("Sheep Wars", 0xb0),
    SKYBLOCK("SkyBlock", 0x84),
    FARM_HUNT("Farm Hunt", 0xb0),
    PIXEL_PARTY("Pixel Party", 0xb0,
            PixelPartyGameState::new),
    WARLORDS("Warlords", 0x90),
    GUESS_THE_BUILD("Guess the Build", 0xb0,
            GuessTheBuildGameState::new),
    PROTOTYPE("Prototype", 0),
    ZOMBIES("Zombies", 0xa0,
            ZombiesGameState::new),
    HIDE_AND_SEEK("Hide And Seek", 0x90,
            HideAndSeekGameState::new),
    SPEED_BUILDERS("Speed Builders", 0xb0,
            SpeedBuildersGameState::new),
    COPS_AND_CRIMS("Cops And Crims", 0),
    DISASTERS("Distasters", 0xa0),
    MAIN_LOBBY("Main Lobby", 0x90),
    UNKNOWN_GAME("Unknown Game", 0x80);

    public final String displayName;
    public final StateFactory stateFactory;
    public final int flags;
    public Object field28;

    HypixelGame(String displayName, int flags) {
        this.displayName = displayName;
        this.flags = flags;
        this.stateFactory = () -> new HypixelGameState(this);
    }

    HypixelGame(String displayName, int flags, StateFactory stateFactory) {
        this.displayName = displayName;
        this.flags = flags;
        this.stateFactory = stateFactory;
    }

    public static HypixelGame fromName(String title) {
        String name = StringUtils.stripControlCodes(title).replaceAll(" ", "_");
        if (name.equalsIgnoreCase("plotting")) return HOUSING;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException exception) {
            return UNKNOWN_GAME;
        }
    }

    public boolean isAnyOf(HypixelGame... games) {
        return Arrays.stream(games).anyMatch(
                game -> game == this);
    }

    @FunctionalInterface
    public interface StateFactory {
        HypixelGameState create();
    }
}
