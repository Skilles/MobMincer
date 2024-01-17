package net.mobmincer.client.menu

import net.minecraft.util.Mth
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.mobmincer.client.menu.slot.FuelSlot

class PowerProviderMenu(containerId: Int, private val playerInventory: Inventory, private val container: Container, private val data: ContainerData) : AbstractContainerMenu(
    Menus.POWER_PROVIDER.get(),
    containerId
) {

    val burnTime: Int
        get() = data.get(0)
    val maxBurnTime: Int
        get() = data.get(1)
    val energy: Int
        get() = data.get(2)
    val capacity: Int
        get() = data.get(3)
    val isActive: Boolean
        get() = data.get(4) == 1

    val burnProgress: Float
        get() {
            var i = maxBurnTime
            if (i == 0) {
                i = 200
            }
            return Mth.clamp(burnTime.toFloat() / i.toFloat(), 0.0f, 1.0f)
        }

    val energyProgress: Float
        get() {
            var i = capacity
            if (i == 0) {
                i = 1000
            }
            return Mth.clamp(energy.toFloat() / i.toFloat(), 0.0f, 1.0f)
        }

    constructor(containerId: Int, playerInventory: Inventory) : this(
        containerId,
        playerInventory,
        playerInventory,
        SimpleContainerData(5)
    )

    init {
        checkContainerSize(container, 1)
        checkContainerDataCount(data, 1)

        this.addSlot(FuelSlot(container, 0, 56, 17))

        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        for (i in 0..8) {
            this.addSlot(Slot(playerInventory, i, 8 + i * 18, 142))
        }

        this.addDataSlots(data)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val itemStack2 = slot.item
            itemStack = itemStack2.copy()
            if (index == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY
                }
                slot.onQuickCraft(itemStack2, itemStack)
            } else if (index != 1 && index != 0) {
                if (AbstractFurnaceBlockEntity.isFuel(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (index in 2..28) {
                    if (!this.moveItemStackTo(itemStack2, 29, 38, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (index in 29..37 && !this.moveItemStackTo(itemStack2, 2, 29, false)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(itemStack2, 2, 38, false)) {
                return ItemStack.EMPTY
            }

            if (itemStack2.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (itemStack2.count == itemStack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, itemStack2)
        }

        return itemStack
    }

    override fun stillValid(player: Player): Boolean {
        return container.stillValid(player)
    }
}
