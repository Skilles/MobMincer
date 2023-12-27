package net.mobmincer.fabric;

import net.mobmincer.fabriclike.MobMincerFabricLike;
import net.fabricmc.api.ModInitializer;

public class MobMincerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MobMincerFabricLike.init();
    }
}
