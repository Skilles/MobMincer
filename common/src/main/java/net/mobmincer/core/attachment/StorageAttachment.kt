package net.mobmincer.core.attachment

import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import net.mobmincer.core.entity.MobMincerEntity
import kotlin.random.Random

class StorageAttachment(type: MobMincerAttachment<StorageAttachment>, mincer: MobMincerEntity) :
    AttachmentInstance(
        type,
        mincer
    ),
    ContainerListener {

    var inventory: SimpleContainer = createInventory()

    override fun onAttach() {
        if (mincer.level().isClientSide) {
            Minecraft.getInstance().player?.let { playChestEquipsSound(it) }
        }
    }

    override fun onDeath(reason: MobMincerEntity.DestroyReason): Boolean {
        if (!mincer.level().isClientSide) {
            Containers.dropContents(mincer.level(), mincer.blockPosition(), this.inventory)
        }
        return super.onDeath(reason)
    }

    override fun onInteract(player: Player) {
        if (player.level().isClientSide) {
            return
        }
        player.openMenu(
            SimpleMenuProvider(
                { id, inventory, _: Player -> ChestMenu.threeRows(id, inventory, this.inventory) },
                mincer.displayName
            )
        )
    }

    override fun serialize(tag: CompoundTag) {
        val listTag = ListTag()
        for (i in 2 until this.inventory.containerSize) {
            val itemStack: ItemStack = this.inventory.getItem(i)
            if (itemStack.isEmpty) continue
            val compoundTag = CompoundTag()
            compoundTag.putByte("Slot", i.toByte())
            itemStack.save(compoundTag)
            listTag.add(compoundTag)
        }
        tag.put("Items", listTag)
    }

    override fun deserialize(tag: CompoundTag, entity: MobMincerEntity) {
        this.inventory = this.createInventory()
        val listTag = tag.getList("Items", 10)
        for (i in listTag.indices) {
            val compoundTag = listTag.getCompound(i)
            val j = compoundTag.getByte("Slot").toInt() and 0xFF
            if (j < 2 || j >= this.inventory.containerSize) continue
            this.inventory.setItem(j, ItemStack.of(compoundTag))
        }
    }

    override fun getInteractionPriority(): Int {
        return 1
    }

    private fun createInventory(): SimpleContainer {
        val inventory = object : SimpleContainer(INVENTORY_SIZE) {
            override fun stillValid(player: Player): Boolean {
                return mincer.isAlive
            }
        }
        inventory.addListener(this)
        this.containerChanged(inventory)
        return inventory
    }

    private fun playChestEquipsSound(player: Player) {
        player.playSound(
            SoundEvents.DONKEY_CHEST,
            1.0f,
            (Random.nextFloat() - Random.nextFloat()) * 0.2f + 1.0f
        )
    }

    override fun containerChanged(container: Container) {}

    fun hasInventoryChanged(inventory: Container): Boolean {
        return this.inventory !== inventory
    }

    companion object {
        const val INVENTORY_COLUMNS = 9
        private const val INVENTORY_SIZE = 27
    }
}
