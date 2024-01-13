package net.mobmincer.core.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.mobmincer.api.block.BaseMachineBlock
import net.mobmincer.api.blockentity.MachineGuiHandler
import net.mobmincer.core.item.MincerPowerProviderItem
import net.mobmincer.energy.MMEnergyBlock
import java.util.*
import java.util.function.BiFunction

class MincerPowerProviderBlock(blockEntityFactory: BiFunction<BlockPos, BlockState, MincerPowerProviderBlockEntity>) :
    BaseMachineBlock<MincerPowerProviderBlockEntity, MincerPowerProviderItem>(
        blockEntityFactory,
        Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(5.0f, 6.0f)
            .sound(SoundType.METAL)
    ),
    MMEnergyBlock {

    override fun getEnergyCapacity(): Long {
        return 10000
    }

    override fun getEnergyMaxInput(side: Direction?): Long {
        return 1000
    }

    override fun getEnergyMaxOutput(side: Direction?): Long {
        return 1000
    }

    override fun getGui(): MachineGuiHandler? {
        TODO("Not yet implemented")
    }

    override fun getBlockItem(): Optional<MincerPowerProviderItem> {
        return Optional.of(MincerPowerProviderItem(this))
    }
}
