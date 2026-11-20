package com.adjustclient.utilities;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.rewound.RewoundGame;
import com.adjustclient.utilities.game.rewound.RewoundGameState;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.game.hideandseek.HideAndSeekGameState;
import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import com.adjustclient.utilities.hypixel.game.hideandseek.HideAndSeekRole;
import com.adjustclient.utilities.game.player.InventoryStateTracker;

public final class GameStateUtil implements MinecraftInstance {
    public static boolean isActiveUnlessPlacing() {
        if (ScaffoldModule.INSTANCE.isEnabled() && ScaffoldModule.INSTANCE.placeReady) return false;
        return isActive();
    }
    public static boolean isActiveUnlessScaffolding() {
        if (ScaffoldModule.INSTANCE.isEnabled() && ScaffoldModule.INSTANCE.placeReady) return false;
        return isActive();
    }
    public static boolean isActive() {
        var serverState = ServerTracker.INSTANCE.serverState;
        if (serverState != null && serverState.antiCheatModel != null) {
            if (serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {
                HypixelGameState state = Adjust.gameStateTracker.state;
                if (Boolean.TRUE.equals(state.lobby)) {
                    if (state.game != HypixelGame.DUELS || mc.thePlayer == null) return false;
                    ItemStack slot = mc.thePlayer.inventory.mainInventory[8];
                    if (slot == null || !(slot.getItem() instanceof ItemBlock)
                            || ((ItemBlock) slot.getItem()).getBlock() != Blocks.barrier) return false;
                }
                if (state instanceof HideAndSeekGameState
                        && ((HideAndSeekGameState) state).role == HideAndSeekRole.HIDER) return false;
                if (state.isSpecialState() != 0 || state.isSpectator()) return false;
                if (Boolean.FALSE.equals(state.lobby) && state.startTimer == null && state.game != HypixelGame.UHC_CHAMPIONS) return false;
            } else if (serverState.antiCheatModel.serverType == ServerType.REWOUND) {
                RewoundGameState rewound = Adjust.rewoundGameTracker.state;
                if (Boolean.TRUE.equals(rewound.lobby)) {
                    if (rewound.game != RewoundGame.DUELS || mc.thePlayer == null) return false;
                    ItemStack slot = mc.thePlayer.inventory.mainInventory[0];
                    if (slot == null || slot.getItem() != Items.iron_sword) return false;
                }
                if (rewound.isInSpawn() || rewound.allowFlying) return false;
                if (Boolean.FALSE.equals(rewound.lobby) && rewound.startTimer == null) return false;
            }
        }
        return mc.thePlayer != null && mc.thePlayer.getHealth() > 0
                && mc.playerController.getCurrentGameType() != WorldSettings.GameType.SPECTATOR
                && !InventoryStateTracker.isInServersideGui();
    }
}
