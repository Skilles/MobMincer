package net.mobmincer.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.mobmincer.MobMincer

@Environment(EnvType.CLIENT)
object MobMincerFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        MobMincer.initClient()
    }
}
