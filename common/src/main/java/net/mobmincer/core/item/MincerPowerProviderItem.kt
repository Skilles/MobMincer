package net.mobmincer.core.item

import net.minecraft.ChatFormatting
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.mobmincer.api.item.BaseBlockItem
import net.mobmincer.core.block.MincerPowerProviderBlock
import net.mobmincer.energy.EnergyUtil.getEnergyBarColor
import net.mobmincer.energy.EnergyUtil.getEnergyBarWidth
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.energy.MMChargableItem
import net.mobmincer.util.StringUtils

class MincerPowerProviderItem(block: Block) : BaseBlockItem(block, Properties()), MMChargableItem {
    override fun getEnergyCapacity(stack: ItemStack): Long {
        return (block as MincerPowerProviderBlock).getEnergyCapacity()
    }

    override fun getEnergyMaxInput(stack: ItemStack): Long {
        return (block as MincerPowerProviderBlock).getEnergyMaxInput(null)
    }

    override fun isBarVisible(stack: ItemStack): Boolean {
        val energyStorage = stack.getEnergyStorage()
        return energyStorage?.isEmpty == false
    }

    override fun getBarWidth(stack: ItemStack): Int {
        return stack.getEnergyBarWidth()
    }

    override fun getBarColor(stack: ItemStack): Int {
        return stack.getEnergyBarColor()
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltipComponents: MutableList<Component>, isAdvanced: TooltipFlag) {
        val energy = stack.getEnergyStorage()?.energy ?: return
        val maxEnergy = getEnergyCapacity(stack)
        tooltipComponents.add(
            Component.translatable(
                "mobmincer.tooltip.energy"
            ).withStyle(
                ChatFormatting.GOLD
            ).append(
                StringUtils.getPercentageText(
                    energy.toInt(),
                    maxEnergy.toInt(),
                    Screen.hasShiftDown()
                )
            )
        )
    }
}
