package net.mobmincer.core.attachment

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.mobmincer.core.entity.MobMincerEntity

abstract class AttachmentInstance(val type: MobMincerAttachment<*>) {

    open fun onSpawn(entity: MobMincerEntity) {}

    open fun onDeath(entity: MobMincerEntity) {}

    open fun onMince(entity: MobMincerEntity, dealtDamage: Float) {}

    open fun onInteract(entity: MobMincerEntity, player: Player) {}

    open fun serialize(tag: CompoundTag) {

    }

    open fun deserialize(tag: CompoundTag, entity: MobMincerEntity) {

    }

    open fun getInteractionPriority(): Int {
        return 0
    }
}