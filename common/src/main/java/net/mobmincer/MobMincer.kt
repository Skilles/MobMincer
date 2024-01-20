package net.mobmincer

import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry
import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import dev.architectury.registry.menu.MenuRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.mobmincer.client.menu.Menus
import net.mobmincer.client.menu.screen.PowerProviderScreen
import net.mobmincer.client.model.MobMincerModel
import net.mobmincer.client.render.MobMincerEntityRenderer
import net.mobmincer.core.item.MobMincerType
import net.mobmincer.core.item.MobMincerType.Companion.getMincerType
import net.mobmincer.core.recipe.RecipeSerializers
import net.mobmincer.core.registry.MMContent
import net.mobmincer.core.registry.MincerRegistry
import net.mobmincer.network.MincerNetwork
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MobMincer {
    const val MOD_ID: String = "mobmincer"

    val logger: Logger = LoggerFactory.getLogger("Mob Mincer")

    init {
        MMContent.init()
        Menus.init()
        RecipeSerializers.register()
    }

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
        MenuRegistry.registerScreenFactory(Menus.POWER_PROVIDER.get(), ::PowerProviderScreen)
        EntityRendererRegistry.register(MMContent.MOB_MINCER_ENTITY, MobMincerEntityRenderer.Provider())
        EntityModelLayerRegistry.register(MobMincerModel.LAYER_LOCATION, MobMincerModel::createBodyLayer)

        ItemProperties.register(
            MMContent.MOB_MINCER_ITEM.get(),
            ResourceLocation("type")
        ) { stack, _, _, _ ->
            Mth.clampedMap(
                stack.getMincerType().ordinal.toDouble(),
                0.0,
                MobMincerType.entries.size.toDouble(),
                0.0,
                1.0
            ).toFloat()
        }

        MincerNetwork.registerClientRecievers()
    }
}
