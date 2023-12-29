package net.mobmincer.fabriclike

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.mobmincer.MobMincer

object MobMincerFabricLike {
    @JvmStatic
    fun init() {
        MobMincer.init()
    }

    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun initClient() {
        MobMincer.initClient()
    }
}
