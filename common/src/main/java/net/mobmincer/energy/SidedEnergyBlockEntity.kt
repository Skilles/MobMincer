package net.mobmincer.energy

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

abstract class SidedEnergyBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) : BlockEntity(
    type,
    pos,
    blockState
) {

    val energy: MMSidedEnergyStorage by lazy { EnergyUtil.createSidedStorage(this) }

    fun getOrCreateEnergyStorage(direction: Direction? = null): MMEnergyStorage {
        return energy.getMMSideStorage(direction)
    }

    override fun load(tag: CompoundTag) {
        if (tag.contains("energy")) {
            energy.deserialize(tag.getCompound("energy"))
        }
    }

    override fun saveAdditional(tag: CompoundTag) {
        energy.serialize()?.let { tag.put("energy", it) }
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag)
        return tag
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }
}
