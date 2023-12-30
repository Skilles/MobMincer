package net.mobmincer.fabric

import net.fabricmc.api.ModInitializer
import net.mobmincer.fabriclike.MobMincerFabricLike

object MobMincerFabric : ModInitializer {
    override fun onInitialize() {
        MobMincerFabricLike.init()
    }
}
