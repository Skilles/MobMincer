package net.mobmincer.api.block

import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import java.util.function.BiFunction

abstract class BaseEntityBlock<T : BlockEntity, I : BlockItem>(private val blockEntityFactory: BiFunction<BlockPos, BlockState, T>, properties: Properties) :
    Block(properties),
    EntityBlock {

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, blockEntityType: BlockEntityType<T>): BlockEntityTicker<T> =
        BlockEntityTicker { level1, pos, blockState1, blockEntity ->
            (blockEntity as? BlockEntityTicker<T>)?.tick(
                level1,
                pos,
                blockState1,
                blockEntity
            )
        }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = constructTypedEntity(pos, state)

    fun constructTypedEntity(pos: BlockPos, state: BlockState): T = blockEntityFactory.apply(pos, state)

    open fun getBlockItem(): Optional<I> = Optional.empty()
}
