package net.mobmincer.api.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Container
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
    Container,
    InventoryProvider {

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

    fun getOptionalInventory(): Optional<MachineInventory<*>> {
        return Optional.ofNullable(getInventory() as? MachineInventory<*>)
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

    fun onPlace(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {

    }

    fun onBreak(level: Level, player: Player, pos: BlockPos, state: BlockState, tool: ItemStack) {

    }
}
