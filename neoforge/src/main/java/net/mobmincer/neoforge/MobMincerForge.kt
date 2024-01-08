package net.mobmincer.neoforge

import net.mobmincer.MobMincer
import net.mobmincer.common.config.MobMincerConfig
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import thedarkcolour.kotlinforforge.neoforge.forge.runWhenOn

@Mod(MobMincerForge.ID)
object MobMincerForge {
    const val ID = MobMincer.MOD_ID

    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobMincerConfig.SPEC)
        MobMincer.init()
        runWhenOn(Dist.CLIENT) { MobMincer.initClient() }
    }
}
