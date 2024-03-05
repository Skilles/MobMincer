package net.mobmincer.api.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Container
import net.minecraft.world.MenuProvider
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.mobmincer.MobMincer
import net.mobmincer.api.inventory.InventoryProvider
import net.mobmincer.api.inventory.MachineInventory
import java.util.*

abstract class BaseMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, blockState: BlockState) :
    BlockEntity(
        type,
        pos,
        blockState
    ),
    WorldlyContainer,
    InventoryProvider,
    MenuProvider {

    val optionalInventory: Optional<MachineInventory<*>>
        get() = Optional.ofNullable(
            getInventory() as? MachineInventory<*> ?: null.also {
                MobMincer.logger.warn("$displayName inventory is not a MachineInventory")
            }
        )

    val hasInventory: Boolean
        get() = optionalInventory.isPresent

    override fun getMaxStackSize(): Int = optionalInventory.map { it.getMaxStackSize() }.orElse(0)

    override fun canPlaceItem(index: Int, stack: ItemStack): Boolean = optionalInventory.map { it.canPlaceItem(index, stack) }.orElse(false)

    override fun canTakeItem(target: Container, index: Int, stack: ItemStack): Boolean = optionalInventory.map { it.canTakeItem(target, index, stack) }.orElse(false)

    override fun clearContent() {
        optionalInventory.ifPresent { it.clearContent() }
    }

    override fun getContainerSize(): Int = optionalInventory.map { it.getContainerSize() }.orElse(0)

    override fun isEmpty(): Boolean = optionalInventory.map { it.isEmpty }.orElse(true)

    override fun getItem(slot: Int): ItemStack = optionalInventory.map { it.getItem(slot) }.orElse(ItemStack.EMPTY)

    override fun removeItem(slot: Int, amount: Int): ItemStack = optionalInventory.map { it.removeItem(slot, amount) }.orElse(ItemStack.EMPTY)

    override fun removeItemNoUpdate(slot: Int): ItemStack = optionalInventory.map { it.removeItemNoUpdate(slot) }.orElse(ItemStack.EMPTY)

    override fun setItem(slot: Int, stack: ItemStack) {
        optionalInventory.ifPresent { it.setItem(slot, stack) }
    }

    override fun stillValid(player: Player): Boolean = optionalInventory.map { it.stillValid(player) }.orElse(false)

    override fun getSlotsForFace(side: Direction): IntArray = optionalInventory.map { it.getSlotsForFace(side) }.orElse(IntArray(0))

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean =
        optionalInventory.map { it.canPlaceItemThroughFace(index, itemStack, direction) }.orElse(false)

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean = optionalInventory.map { it.canTakeItemThroughFace(index, stack, direction) }.orElse(false)

    override fun load(tag: CompoundTag) {
        super.load(tag)
        optionalInventory.ifPresent { it.readFromNBT(tag) }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        optionalInventory.ifPresent { it.writeToNBT(tag) }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag)
        return tag
    }

    override fun getDisplayName(): Component = blockState.block.name

    fun onPlace(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        stack.tag?.let(::load)
    }

    fun onBreak(level: Level, player: Player, pos: BlockPos, state: BlockState, tool: ItemStack) {

    }
}
