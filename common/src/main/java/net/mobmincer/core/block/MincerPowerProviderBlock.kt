package net.mobmincer.core.block

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.FurnaceMenu
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.mobmincer.energy.MMEnergyBlock
import java.util.function.Supplier

class MincerPowerProviderBlock(properties: Properties) : BaseEntityBlock(properties), MMEnergyBlock, MenuProvider {

    private lateinit var blockEntityType: Supplier<BlockEntityType<MincerPowerProviderBlockEntity>>

    constructor(properties: Properties, blockEntitySupplier: Supplier<BlockEntityType<MincerPowerProviderBlockEntity>>) : this(
        properties
    ) {
        this.blockEntityType = blockEntitySupplier
    }

    override fun getEnergyCapacity(): Long {
        return 10000
    }

    override fun getEnergyMaxInput(side: Direction?): Long {
        return 1000
    }

    override fun getEnergyMaxOutput(side: Direction?): Long {
        return 1000
    }

    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        val blockEntity = level.getBlockEntity(pos) as? MincerPowerProviderBlockEntity ?: return

        blockEntity.chargeNearbyMincers()
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return MincerPowerProviderBlockEntity(pos, state)
    }

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return FurnaceMenu(i, inventory)
    }

    override fun getDisplayName(): Component {
        return Component.translatable("gui.mobmincer.power_provider.title")
    }
}
