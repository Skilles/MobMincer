package net.mobmincer

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry
import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.mobmincer.client.model.MobMincerModel
import net.mobmincer.client.render.MobMincerEntityRenderer
import net.mobmincer.core.registry.MMContent
import net.mobmincer.core.registry.MincerRegistry
import net.mobmincer.network.MincerNetwork
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MobMincer {
    const val MOD_ID: String = "mobmincer"

    val logger: Logger = LoggerFactory.getLogger("Mob Mincer")

    @JvmStatic
    fun init() {
        MincerRegistry.performRegistration()

        LifecycleEvent.SERVER_LEVEL_UNLOAD.register {
            FakePlayer.unload(it)
        }

        MincerNetwork.registerServerRecievers()
    }

    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun initClient() {
        EntityRendererRegistry.register(MMContent.MOB_MINCER_ENTITY, MobMincerEntityRenderer.Provider())
        EntityModelLayerRegistry.register(MobMincerModel.LAYER_LOCATION, MobMincerModel::createBodyLayer)

        MincerNetwork.registerClientRecievers()
    }
}
