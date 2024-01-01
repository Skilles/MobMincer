package net.mobmincer.core.attachment

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.mobmincer.core.entity.MobMincerEntity
import java.util.*

class AttachmentHolder(private val mobMincer: MobMincerEntity) {
    private val attachments: MutableMap<Attachments, AttachmentInstance> = EnumMap(Attachments::class.java)

    fun addAttachment(attachment: Attachments) {
        AttachmentRegistry.get(attachment)?.let { this.attachments.put(attachment, it.create()) }
    }

    fun removeAttachment(attachment: Attachments) {
        this.attachments.remove(attachment)
    }

    fun hasAttachment(attachment: Attachments): Boolean {
        return this.attachments.containsKey(attachment)
    }

    fun tryAddAttachment(item: Item): Boolean {
        val attachment = Attachments.fromItem(item)
        if (attachment != null && !this.hasAttachment(attachment)) {
            this.addAttachment(attachment)
            return true
        }

        return false
    }

    fun onSpawn() {
        this.attachments.values.forEach { it.onSpawn(mobMincer) }
    }

    fun onDeath() {
        this.attachments.values.forEach { it.onDeath(mobMincer) }
    }

    fun onMince(dealtDamage: Float) {
        this.attachments.values.forEach { it.onMince(mobMincer, dealtDamage) }
    }

    fun onInteract(player: Player) {
        this.attachments.values.sortedByDescending(AttachmentInstance::getInteractionPriority).forEach {
            it.onInteract(mobMincer, player)
        }
    }

    fun toTag(): ListTag {
        val tag = ListTag()
        this.attachments.forEach { (attachment, instance) ->
            val baseTag = CompoundTag()
            baseTag.putString("type", attachment.name)
            val attachmentTag = CompoundTag()
            instance.serialize(attachmentTag)
            baseTag.put("data", attachmentTag)
            tag.add(baseTag)
        }
        return tag
    }

    fun fromTag(tag: ListTag) {
        tag.forEach { attachmentTag ->
            attachmentTag as CompoundTag
            val attachment = Attachments.valueOf(attachmentTag.getString("type"))
            val instance = AttachmentRegistry.get(attachment)?.create()
            instance?.deserialize(attachmentTag.getCompound("data"), mobMincer)
            if (instance != null) {
                this.attachments[attachment] = instance
            }
        }
    }
}
