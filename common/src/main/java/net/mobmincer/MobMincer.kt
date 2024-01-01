package net.mobmincer

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry
import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.mobmincer.client.model.MobMincerModel
import net.mobmincer.client.render.MobMincerEntityRenderer
import net.mobmincer.core.registry.MincerEntities
import net.mobmincer.core.registry.MincerItems
import net.mobmincer.core.registry.MincerTabs
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object MobMincer {
    const val MOD_ID: String = "mobmincer"

    val logger: Logger = LoggerFactory.getLogger("Mob Mincer")

    @JvmStatic
    fun init() {
        MincerTabs.register()
        MincerItems.register()
        MincerEntities.register()

        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(FakePlayer::unload)
    }

    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun initClient() {
        EntityRendererRegistry.register(MincerEntities.MOB_MINCER, MobMincerEntityRenderer.Provider())
        EntityModelLayerRegistry.register(MobMincerModel.LAYER_LOCATION, MobMincerModel::createBodyLayer)
    }
}
