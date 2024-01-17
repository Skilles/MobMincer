package net.mobmincer.neoforge

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.mobmincer.MobMincer
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.item.MobMincerItem
import net.mobmincer.energy.neoforge.MMCapabilities
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.event.AttachCapabilitiesEvent
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runWhenOn

@Mod(MobMincerForge.ID)
object MobMincerForge {
    const val ID = MobMincer.MOD_ID

    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobMincerConfig.SPEC)
        MobMincer.init()
        runWhenOn(Dist.CLIENT) { MobMincer.initClient() }

        /*LOADING_CONTEXT.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent -> parent }
        }*/

        FORGE_BUS.register(this)
    }

    @SubscribeEvent
    fun attachCapabilities(event: AttachCapabilitiesEvent<*>) {
        val obj = event.`object`
        if (obj is ItemStack && obj.item is MobMincerItem) {
            event.addCapability(
                ResourceLocation(MobMincer.MOD_ID, "energy_item"),
                MMCapabilities.createEnergyItemProvider(obj)
            )
        } else if (obj is SidedEnergyBlockEntity) {
            event.addCapability(
                ResourceLocation(MobMincer.MOD_ID, "energy_block"),
                MMCapabilities.createEnergyBlockProvider(obj)
            )
        }
    }
}
