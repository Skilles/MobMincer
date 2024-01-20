package net.mobmincer.compat.rei

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantments
import net.mobmincer.core.item.MobMincerType
import net.mobmincer.core.item.MobMincerType.Companion.setMincerType
import net.mobmincer.core.loot.KillDropLootEntry
import net.mobmincer.core.registry.MMContent

open class MobMincerReiPlugin : REIClientPlugin {

    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(MobMincerDisplayCategory())

        val mincerStack = ItemStack(MMContent.MOB_MINCER_ITEM)
        val enchantedStack = mincerStack.copy()
        enchantedStack.enchant(Enchantments.SILK_TOUCH, 1)
        val poweredStack = mincerStack.copy()
        poweredStack.setMincerType(MobMincerType.POWERED)
        val creativeStack = mincerStack.copy()
        creativeStack.setMincerType(MobMincerType.CREATIVE)
        registry.configure(MobMincerDisplayCategory.MOB_MINCER_CATEGORY) {
            it.addWorkstations(
                EntryStacks.of(mincerStack),
                EntryStacks.of(enchantedStack),
                EntryStacks.of(poweredStack),
                EntryStacks.of(creativeStack)
            )
        }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        KillDropLootEntry.createAll().forEach {
            registry.add(MobMincerDisplay(it))
        }
    }
}
