package net.mobmincer.core.block

import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.mobmincer.api.blockentity.SidedEnergyBlockEntity
import net.mobmincer.api.inventory.MachineInventory
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MMContent
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.EnergyUtil.getEnergyStorage

class MincerPowerProviderBlockEntity(pos: BlockPos, blockState: BlockState) :
    SidedEnergyBlockEntity(
        MMContent.POWER_PROVIDER.blockEntity.get(),
        pos,
        blockState
    ),
    BlockEntityTicker<MincerPowerProviderBlockEntity> {

    val inventory = MachineInventory(1, "PowerProviderBlockEntity", 64, this)

    override fun tick(level: Level, blockPos: BlockPos, blockState: BlockState, blockEntity: MincerPowerProviderBlockEntity) {
        chargeNearbyMincers(level, blockPos, blockEntity)
    }

    override fun getInventory(): Container = inventory

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
    }
}
