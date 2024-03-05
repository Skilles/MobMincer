package net.mobmincer.api.block

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.WorldlyContainerHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.BlockHitResult
import net.mobmincer.api.blockentity.BaseMachineBlockEntity
import net.mobmincer.api.blockentity.EnergyMachineBlockEntity
import net.mobmincer.api.blockentity.MachineGuiHandler
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.energy.EnergyUtil.setEnergyUnchecked
import net.mobmincer.energy.MMEnergyBlock
import java.util.function.BiFunction

abstract class BaseMachineBlock<T : BlockEntity, I : BlockItem>(blockEntityFactory: BiFunction<BlockPos, BlockState, T>, properties: Properties) :
    BaseEntityBlock<T, I>(
        blockEntityFactory,
        properties
    ),
    MMEnergyBlock,
    WorldlyContainerHolder {

    open fun getGui(): MachineGuiHandler = MachineGuiHandler.SIMPLE

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)

        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is BaseMachineBlockEntity) {
            blockEntity.onPlace(level, pos, state, placer, stack)
        }
    }

    override fun playerDestroy(level: Level, player: Player, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool)

        if (blockEntity is BaseMachineBlockEntity) {
            blockEntity.onBreak(level, player, pos, state, tool)
        }
    }

    override fun playerWillDestroy(level: Level, pos: BlockPos, state: BlockState, player: Player) {
        super.playerWillDestroy(level, pos, state, player)
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity is BaseMachineBlockEntity) {
            val energyStorage = blockEntity.getEnergyStorage()!!
            if (energyStorage.isEmpty && blockEntity.getInventory().isEmpty) {
                return
            }
            val itemStack = ItemStack(this)
            blockEntity.saveToItem(itemStack)
            itemStack.setEnergyUnchecked(energyStorage.energy)
            val itemEntity = ItemEntity(
                level,
                pos.x.toDouble() + 0.5,
                pos.y.toDouble() + 0.5,
                pos.z.toDouble() + 0.5,
                itemStack
            )
            level.addFreshEntity(itemEntity)
        }
    }

    override fun getDrops(state: BlockState, params: LootParams.Builder): MutableList<ItemStack> {
        val drops = super.getDrops(state, params)
        val item = asItem()
        drops.removeIf { it.item == item }
        return drops
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult {
        if (!player.isShiftKeyDown && !level.isClientSide) {
            getGui().open(player, pos, level)
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun hasAnalogOutputSignal(state: BlockState): Boolean {
        return state.block is MMEnergyBlock
    }

    override fun getAnalogOutputSignal(state: BlockState, level: Level, pos: BlockPos): Int {
        val blockEntity = level.getBlockEntity(pos)
        return if (blockEntity is EnergyMachineBlockEntity) {
            val storage = blockEntity.energyStorage
            val energy = storage.energy
            val maxEnergy = storage.energyCapacity.toFloat()
            return Mth.lerpDiscrete(energy / maxEnergy, 0, 15)
        } else {
            0
        }
    }

    override fun getContainer(state: BlockState, level: LevelAccessor, pos: BlockPos): WorldlyContainer {
        val blockEntity = level.getBlockEntity(pos)
        check(blockEntity is WorldlyContainer) { "Missing container block entity" }
        return blockEntity
    }
}
