package net.mobmincer.core.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.DispenserBlock
import net.mobmincer.MobMincer.MOD_ID
import net.mobmincer.core.item.MincerPowerProviderItem
import net.mobmincer.core.item.MobMincerItem
import java.util.function.Function

object MincerItems {
    private val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registries.ITEM)
    val MOB_MINCER: RegistrySupplier<Item> = registerItem(
        "mob_mincer",
        ::MobMincerItem,
    ) { properties: Item.Properties -> properties.stacksTo(1).defaultDurability(100) }

    val POWER_PROVIDER: RegistrySupplier<Item> = registerItem("mincer_power_provider", ::MincerPowerProviderItem)

    fun register() {
        MOB_MINCER.listen {
            DispenserBlock.registerBehavior(it, MobMincerItem.DISPENSE_BEHAVIOR)
        }
        ITEMS.register()
    }

    private fun registerItem(
        id: String,
        supplier: Function<Item.Properties, Item>,
        properties: Function<Item.Properties, Item.Properties> = Function {
            it
        }
    ): RegistrySupplier<Item> {
        return ITEMS.register(
            id
        ) { supplier.apply(properties.apply(Item.Properties().`arch$tab`(MincerTabs.CREATIVE_TAB))) }
    }
}
