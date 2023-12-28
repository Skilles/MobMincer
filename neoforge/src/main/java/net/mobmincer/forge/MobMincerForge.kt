package net.mobmincer.forge

import net.mobmincer.MobMincer
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod
import thedarkcolour.kotlinforforge.neoforge.forge.runWhenOn

@Mod(MobMincerForge.ID)
object MobMincerForge {
    const val ID = MobMincer.MOD_ID

    init {
        MobMincer.init()
        runWhenOn(Dist.CLIENT) { MobMincer.initClient() }
    }
}
