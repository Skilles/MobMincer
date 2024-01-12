package net.mobmincer.core.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MincerBlocks
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.SidedEnergyBlockEntity

class MincerPowerProviderBlockEntity(pos: BlockPos, blockState: BlockState) : SidedEnergyBlockEntity(
    MincerBlocks.POWER_PROVIDER_BLOCK_ENTITY.get(),
    pos,
    blockState
) {

    fun chargeNearbyMincers() {
        if (this.energy.isEmpty) return

        val nearbyMincers = level!!.getEntitiesOfClass(MobMincerEntity::class.java, AABB(blockPos).inflate(5.0)).map {
            EnergyUtil.getEnergyStorage(it.sourceStack)
        }

        EnergyUtil.transferEnergy(this, nearbyMincers, 10)
    }
}
