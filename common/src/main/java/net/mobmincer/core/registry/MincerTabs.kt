package net.mobmincer.core.registry

import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.mobmincer.MobMincer.MOD_ID

object MincerTabs {
    private val TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB)

    val CREATIVE_TAB: RegistrySupplier<CreativeModeTab> = registerTab("mob_mincer", MincerItems.MOB_MINCER)

    private fun registerTab(id: String, icon: RegistrySupplier<Item>): RegistrySupplier<CreativeModeTab> {
        return TABS.register(
            id
        ) { CreativeTabRegistry.create(Component.translatable("itemGroup.$MOD_ID.$id")) { ItemStack(icon.get()) } }
    }

    fun register() {
        TABS.register()
    }
}
