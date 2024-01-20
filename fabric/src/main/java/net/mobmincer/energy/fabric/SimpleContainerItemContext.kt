package net.mobmincer.energy.fabric

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage
import net.fabricmc.fabric.impl.transfer.context.SingleSlotContainerItemContext
import net.minecraft.world.item.ItemStack

class SimpleContainerItemContext(stack: ItemStack) : SingleSlotContainerItemContext(object : SingleStackStorage() {
    override fun getStack(): ItemStack = stack

    override fun setStack(newStack: ItemStack) {
        require(
            stack.isEmpty || newStack.isEmpty || stack.item === newStack.item
        ) { "Cannot set a stack with a different item" }
        getStack().count = newStack.count
        getStack().tag = newStack.tag
        getStack().popTime = newStack.popTime
    }

    override fun onFinalCommit() {
        // NO-OP
    }
})
