package net.mobmincer.client.menu

import net.minecraft.util.Mth
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.mobmincer.client.menu.slot.FuelSlot
import net.mobmincer.client.menu.slot.PowerSlot
import net.mobmincer.core.registry.MMContent
import net.mobmincer.energy.EnergyUtil.usesEnergy

class PowerProviderMenu(containerId: Int, playerInventory: Inventory, private val container: Container, private val data: ContainerData, private val access: ContainerLevelAccess) :
    AbstractContainerMenu(
        Menus.POWER_PROVIDER.get(),
        containerId
    ) {

    var burnTime: Int
        get() = data.get(0)
        set(value) = data.set(0, value)
    var maxBurnTime: Int
        get() = data.get(1)
        set(value) = data.set(1, value)
    var energy: Int
        get() = data.get(2)
        set(value) = data.set(2, value)
    var capacity: Int
        get() = data.get(3)
        set(value) = data.set(3, value)
    val isBurning: Boolean
        get() = burnTime > 0

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
        SimpleContainer(2),
        SimpleContainerData(4),
        ContainerLevelAccess.NULL
    )

    init {
        checkContainerSize(container, 2)
        checkContainerDataCount(data, 4)

        this.addSlot(PowerSlot(container, 0, 19, 17))
        this.addSlot(FuelSlot(container, 1, 19, 53))
        this.addDataSlots(data)

        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        for (i in 0..8) {
            this.addSlot(Slot(playerInventory, i, 8 + i * 18, 142))
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]
        if (slot.hasItem()) {
            val itemStack2 = slot.item
            itemStack = itemStack2.copy()
            if (index != 1 && index != 0) {
                if (AbstractFurnaceBlockEntity.isFuel(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (slot.item.usesEnergy()) {
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
        return container.stillValid(player) && stillValid(
            access,
            player,
            MMContent.POWER_PROVIDER.block.get()
        )
    }
}
