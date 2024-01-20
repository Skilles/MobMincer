package net.mobmincer.client.menu.slot

import net.minecraft.world.Container
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.mobmincer.energy.EnergyUtil.usesEnergy

class PowerSlot(container: Container, slot: Int, x: Int, y: Int) : Slot(container, slot, x, y) {

    override fun mayPlace(stack: ItemStack): Boolean {
        return stack.usesEnergy()
    }

    override fun getMaxStackSize(): Int {
        return 1
    }
}