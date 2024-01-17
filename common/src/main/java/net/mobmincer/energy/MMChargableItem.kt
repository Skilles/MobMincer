package net.mobmincer.energy

import net.minecraft.world.item.ItemStack

interface MMChargableItem {

    /**
     * @param stack Current stack.
     * @return The max energy that can be stored in this item stack (ignoring current stack size).
     */
    fun getEnergyCapacity(stack: ItemStack): Long

    /**
     * @param stack Current stack.
     * @return The max amount of energy that can be inserted in this item stack (ignoring current stack size) in a single operation.
     */
    fun getEnergyMaxInput(stack: ItemStack): Long

    /**
     * @param stack Current stack.
     * @return The max amount of energy that can be extracted from this item stack (ignoring current stack size) in a single operation.
     */
    fun getEnergyMaxOutput(stack: ItemStack): Long = 0

    fun supportsInsertion(stack: ItemStack): Boolean = getEnergyMaxInput(stack) > 0

    fun supportsExtraction(stack: ItemStack): Boolean = getEnergyMaxOutput(stack) > 0
}
