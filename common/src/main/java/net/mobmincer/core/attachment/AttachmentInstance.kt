package net.mobmincer.core.attachment

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.mobmincer.core.entity.MobMincerEntity

abstract class AttachmentInstance(val type: MobMincerAttachment<*>, protected val mincer: MobMincerEntity) {

    open fun onAttach() {}

    open fun onSpawn() {}

    open fun onDeath(reason: MobMincerEntity.DestroyReason): Boolean {
        if (reason != MobMincerEntity.DestroyReason.REMOVED) {
            mincer.spawnAtLocation(type.item)
        }

        return false
    }

    open fun onMince(dealtDamage: Float) {}

    open fun onInteract(player: Player) {}

    open fun serialize(tag: CompoundTag) {

    }

    open fun toTag(): CompoundTag? {
        return null
    }

    open fun fromTag(tag: CompoundTag) {

    }

    open fun deserialize(tag: CompoundTag, entity: MobMincerEntity) {

    }

    open fun getInteractionPriority(): Int {
        return 0
    }
}