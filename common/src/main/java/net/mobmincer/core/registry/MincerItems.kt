package net.mobmincer.core.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.mobmincer.MobMincer.MOD_ID
import net.mobmincer.core.item.MobMincerItem
import java.util.function.Function

object MincerItems {
    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registries.ITEM)
    val MOB_MINCER: RegistrySupplier<Item> = registerItem("mob_mincer") { properties: Item.Properties -> properties.stacksTo(1).defaultDurability(100) }

    fun register() {
        ITEMS.register()
    }

    private fun registerItem(id: String, properties: Function<Item.Properties, Item.Properties> = Function { properties: Item.Properties -> properties }): RegistrySupplier<Item> {
        return ITEMS.register(id) { MobMincerItem(properties.apply(Item.Properties().`arch$tab`(MincerTabs.CREATIVE_TAB))) }
    }
}
