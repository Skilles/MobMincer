package net.mobmincer.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mobmincer.fabriclike.MobMincerFabricLike;

@Environment(EnvType.CLIENT)
public class ModMincerFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MobMincerFabricLike.initClient();
    }
}
