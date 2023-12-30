package net.mobmincer.fabriclike

import fuzs.forgeconfigapiport.api.config.v3.ForgeConfigRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.mobmincer.MobMincer
import net.mobmincer.core.config.MobMincerConfig
import net.neoforged.fml.config.ModConfig

object MobMincerFabricLike {
    @JvmStatic
    fun init() {
        ForgeConfigRegistry.INSTANCE.register(MobMincer.MOD_ID, ModConfig.Type.COMMON, MobMincerConfig.SPEC)
        MobMincer.init()
    }

    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun initClient() {
        MobMincer.initClient()
    }
}
