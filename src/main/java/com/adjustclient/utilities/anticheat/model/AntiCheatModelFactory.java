package com.adjustclient.utilities.anticheat.model;

interface AntiCheatModelFactory<S extends AntiCheatModel> {
    S identifyKnownServerBy(String address, int port, net.minecraft.client.multiplayer.ServerData data);
}
