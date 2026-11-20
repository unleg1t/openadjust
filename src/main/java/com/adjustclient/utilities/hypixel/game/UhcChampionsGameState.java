package com.adjustclient.utilities.hypixel.game;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
public final class UhcChampionsGameState extends HypixelGameState {
    public UhcChampionsGameState() { super(HypixelGame.UHC_CHAMPIONS); }
    @Override public Integer getAdditionalData() { java.util.Objects.requireNonNull(mc.theWorld); return 256; }
}
