package net.mobmincer.api.inventory

import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.mobmincer.api.blockentity.BaseMachineBlockEntity


fun interface InventoryAccess<T : BaseMachineBlockEntity> {

    fun canHandleIO(slotID: Int, stack: ItemStack, face: Direction, direction: AccessDirection, blockEntity: T): Boolean

    enum class AccessDirection {
        INSERT,
        EXTRACT
    }
}