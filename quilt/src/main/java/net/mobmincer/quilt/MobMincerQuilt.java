package net.mobmincer.quilt;

import net.mobmincer.fabriclike.MobMincerFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class MobMincerQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        MobMincerFabricLike.init();
    }
}
