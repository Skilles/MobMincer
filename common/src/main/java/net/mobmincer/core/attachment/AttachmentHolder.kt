package net.mobmincer.core.attachment

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.mobmincer.common.AttachmentRegistry
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.util.EncodingUtils.getOrCreateTag
import java.util.*

class AttachmentHolder(private val mobMincer: MobMincerEntity) {
    private val attachments: MutableMap<Attachments, AttachmentInstance> = EnumMap(Attachments::class.java)

    private fun addAttachment(attachment: Attachments): AttachmentInstance? {
        return AttachmentRegistry.get(attachment)
            ?.create(mobMincer)
            ?.also(AttachmentInstance::onAttach)
            ?.also { instance -> this.attachments[attachment] = instance }
    }

    fun removeAttachment(attachment: Attachments) {
        this.attachments.remove(attachment)
    }

    fun hasAttachment(attachment: Attachments): Boolean {
        return this.attachments.containsKey(attachment)
    }

    fun getAttachment(attachment: Attachments): AttachmentInstance? {
        return this.attachments[attachment]
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
        this.attachments.values.forEach { it.onSpawn() }
    }

    fun onDeath(reason: MobMincerEntity.DestroyReason): Boolean {
        val addTag = reason == MobMincerEntity.DestroyReason.UNEQUIPPED
        if (addTag) {
            val stackTag = mobMincer.sourceStack.getOrCreateTagElement("MobMincer")
            val newTag = ListTag()
            var stopMincerDeath = false
            this.attachments.entries.forEach {
                val thisTag = CompoundTag()
                thisTag.putString("Type", it.key.name)
                it.value.toTag()?.let { tag -> thisTag.put("Data", tag) }
                newTag.add(thisTag)
                stopMincerDeath = it.value.onDeath(reason)
            }
            stackTag.put("Attachments", newTag)
            return stopMincerDeath
        } else {
            return this.attachments.values.any { it.onDeath(reason) }
        }
    }

    fun onMince(dealtDamage: Float) {
        this.attachments.values.forEach { it.onMince(dealtDamage) }
    }

    fun onInteract(player: Player) {
        this.attachments.values.sortedByDescending(AttachmentInstance::getInteractionPriority).forEach {
            it.onInteract(player)
        }
    }

    fun onAttach() {
        require(attachments.isEmpty()) { "Attachments already exist" }
        this.mobMincer.sourceStack.getOrCreateTagElement(
            "MobMincer"
        ).getOrCreateTag("Attachments", ListTag::class.java).forEach { tag ->
            tag as CompoundTag
            val name = tag.getString("Type")
            val data = tag.getCompound("Data")
            val attachment = addAttachment(Attachments.valueOf(name))
            attachment?.let {
                it.fromTag(data)
                it.onAttach()
            } ?: error("Failed to add attachment $name from item")
        }
    }

    fun toEntityTag(): ListTag {
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

    fun fromEntityTag(tag: ListTag) {
        tag.forEach { attachmentTag ->
            attachmentTag as CompoundTag
            val attachment = Attachments.valueOf(attachmentTag.getString("type"))
            AttachmentRegistry.get(attachment)?.create(mobMincer)?.let {
                it.deserialize(attachmentTag.getCompound("data"), mobMincer)
                this.attachments[attachment] = it
            }
        }
    }

    val values: Collection<AttachmentInstance>
        get() = this.attachments.values
}
