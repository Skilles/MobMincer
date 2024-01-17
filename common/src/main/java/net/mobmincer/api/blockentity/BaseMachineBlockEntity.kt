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

    override fun getMaxStackSize(): Int {
        return getOptionalInventory().map { it.getMaxStackSize() }.orElse(0)
    }

    override fun canPlaceItem(index: Int, stack: ItemStack): Boolean {
        return getOptionalInventory().map { it.canPlaceItem(index, stack) }.orElse(false)
    }

    override fun canTakeItem(target: Container, index: Int, stack: ItemStack): Boolean {
        return getOptionalInventory().map { it.canTakeItem(target, index, stack) }.orElse(false)
    }

    override fun clearContent() {
        getOptionalInventory().ifPresent { it.clearContent() }
    }

    override fun getContainerSize(): Int {
        return getOptionalInventory().map { it.getContainerSize() }.orElse(0)
    }

    override fun isEmpty(): Boolean {
        return getOptionalInventory().map { it.isEmpty }.orElse(true)
    }

    override fun getItem(slot: Int): ItemStack {
        return getOptionalInventory().map { it.getItem(slot) }.orElse(ItemStack.EMPTY)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        return getOptionalInventory().map { it.removeItem(slot, amount) }.orElse(ItemStack.EMPTY)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return getOptionalInventory().map { it.removeItemNoUpdate(slot) }.orElse(ItemStack.EMPTY)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        getOptionalInventory().ifPresent { it.setItem(slot, stack) }
    }

    override fun stillValid(player: Player): Boolean {
        return getOptionalInventory().map { it.stillValid(player) }.orElse(false)
    }

    override fun getSlotsForFace(side: Direction): IntArray {
        return getOptionalInventory().map { it.getSlotsForFace(side) }.orElse(IntArray(0))
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        return getOptionalInventory().map { it.canPlaceItemThroughFace(index, itemStack, direction) }.orElse(false)
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return getOptionalInventory().map { it.canTakeItemThroughFace(index, stack, direction) }.orElse(false)
    }

    fun getOptionalInventory(): Optional<MachineInventory<*>> {
        return Optional.ofNullable(getInventory() as? MachineInventory<*> ?: throw IllegalStateException("Inventory is not a MachineInventory"))
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        getOptionalInventory().ifPresent { it.readFromNBT(tag) }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        getOptionalInventory().ifPresent { it.writeToNBT(tag) }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag)
        return tag
    }

    override fun getDisplayName(): Component {
        return blockState.block.name
    }

    fun onPlace(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
    }

    fun onBreak(level: Level, player: Player, pos: BlockPos, state: BlockState, tool: ItemStack) {
    }
}
