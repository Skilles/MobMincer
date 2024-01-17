package net.mobmincer.core.block

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.api.inventory.InventoryAccess
import net.mobmincer.api.inventory.MachineInventory
import net.mobmincer.client.menu.PowerProviderMenu
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MMContent
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.energy.MMChargableItem


class MincerPowerProviderBlockEntity(pos: BlockPos, blockState: BlockState) :
    SidedEnergyBlockEntity(
        MMContent.POWER_PROVIDER.blockEntity.get(),
        pos,
        blockState
    ),
    BlockEntityTicker<MincerPowerProviderBlockEntity> {

    val inventory = MachineInventory(
        2,
        "PowerProviderBlockEntity",
        64,
        this
    ) { slotID, stack, face, direction, blockEntity ->
        when {
            direction != InventoryAccess.AccessDirection.INSERT -> true

            when (slotID) {
                FUEL_SLOT -> isFuel(stack)
                CHARGE_SLOT -> stack.item is MMChargableItem
                else -> false
            } -> true

            else -> false
        }
    }

    var burnTime = 0
    val maxBurnTime = 100
    val isBurning = false
    val lastTickBurning = false

    fun getItemBurnTime(stack: ItemStack): Int {
        if (stack.isEmpty) {
            return 0
        }
        val burnMap: Map<Item, Int> = AbstractFurnaceBlockEntity.getFuel()
        if (burnMap.containsKey(stack.item)) {
            return burnMap[stack.item]!! / 4
        }
        return 0
    }

    override fun tick(level: Level, blockPos: BlockPos, blockState: BlockState, blockEntity: MincerPowerProviderBlockEntity) {
        if (level.isClientSide) return

        if (burnTime > 0) {
            burnTime--
            blockEntity.energyStorage.insert(1)
        } else {
            val fuelStack = inventory.getItem(0)
            if (isFuel(fuelStack)) {
                burnTime = getBurnDuration(fuelStack)
                fuelStack.shrink(1)
            }
        }
        chargeNearbyMincers(level, blockPos, blockEntity)
    }

    fun getBurnDuration(fuel: ItemStack): Int {
        if (fuel.isEmpty) {
            return 0
        } else {
            val item = fuel.item
            return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0) as Int
        }
    }

    override fun getInventory(): Container = inventory

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return PowerProviderMenu(i, inventory, this, data)
    }

    private val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0 -> burnTime
            1 -> maxBurnTime
            2 -> energyStorage.energy.toInt()
            3 -> energyStorage.energyCapacity.toInt()
            4 -> if (isBurning) 1 else 0
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            burnTime = value
        }

        override fun getCount(): Int = 5
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        burnTime = tag.getInt("BurnTime")
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putInt("BurnTime", burnTime)
    }

    companion object {
        fun chargeNearbyMincers(
            level: Level,
            pos: BlockPos,
            blockEntity: MincerPowerProviderBlockEntity
        ) {
            if (blockEntity.energyStorage.isEmpty) return

            val nearbyMincers = level.getEntitiesOfClass(
                MobMincerEntity::class.java,
                AABB(pos).inflate(5.0)
            ).map { it.getEnergyStorage() }

            EnergyUtil.transferEnergy(blockEntity, nearbyMincers, 10)
        }

        const val FUEL_SLOT = 0
        const val CHARGE_SLOT = 1
    }
}
