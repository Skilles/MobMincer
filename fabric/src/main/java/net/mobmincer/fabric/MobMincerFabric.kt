package net.mobmincer.fabric

import fuzs.forgeconfigapiport.api.config.v3.ForgeConfigRegistry
import net.fabricmc.api.ModInitializer
import net.mobmincer.MobMincer
import net.mobmincer.common.config.MobMincerConfig
import net.neoforged.fml.config.ModConfig

object MobMincerFabric : ModInitializer {
    override fun onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(MobMincer.MOD_ID, ModConfig.Type.COMMON, MobMincerConfig.SPEC)
        MobMincer.init()
    }
}
