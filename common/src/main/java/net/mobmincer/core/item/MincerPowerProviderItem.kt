package net.mobmincer.core.item

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.mobmincer.api.item.BaseBlockItem
import net.mobmincer.core.block.MincerPowerProviderBlock
import net.mobmincer.energy.MMChargableItem

class MincerPowerProviderItem(block: Block) : BaseBlockItem(block, Properties()), MMChargableItem {
    override fun getEnergyCapacity(stack: ItemStack): Long {
        return (block as MincerPowerProviderBlock).getEnergyCapacity()
    }

    override fun getEnergyMaxInput(stack: ItemStack): Long {
        return (block as MincerPowerProviderBlock).getEnergyMaxInput(null)
    }

    override fun isBarVisible(stack: ItemStack): Boolean {
        return super.isBarVisible(stack)
    }

    override fun getBarWidth(stack: ItemStack): Int {
        return super.getBarWidth(stack)
    }

    override fun getBarColor(stack: ItemStack): Int {
        return super.getBarColor(stack)
    }
}