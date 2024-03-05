package net.mobmincer.core.attachment

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.AttachmentRegistry
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

    fun <T : AttachmentInstance> getAttachment(attachment: Attachments): T? {
        return this.attachments[attachment] as? T
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
        val addTag = reason != MobMincerEntity.DestroyReason.BROKEN
        val stopDeath = this.attachments.values.any { it.onDeath(reason) }
        if (addTag && attachments.isNotEmpty()) {
            val stackTag = mobMincer.sourceStack.getOrCreateTagElement(MobMincerEntity.ROOT_TAG)
            stackTag.put(MobMincerEntity.ATTACHMENTS_TAG, toTag())
        }
        return stopDeath
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
        this.attachments.values.forEach { it.onAttach() }
        /*require(attachments.isEmpty()) { "Attachments already exist" }
        val rootTag = this.mobMincer.sourceStack.getOrCreateTagElement(
            "MobMincer"
        )
        if (rootTag.contains("Attachments")) {
            rootTag.getList("Attachments", 10).forEach { tag ->
                tag as CompoundTag
                val name = tag.getString(MobMincerEntity.ATTACHMENTS_TYPE_TAG)
                val data = tag.getCompound(MobMincerEntity.ATTACHMENTS_DATA_TAG)
                val attachment = addAttachment(Attachments.valueOf(name))
                attachment?.let {
                    it.onAttach()
                } ?: error("Failed to add attachment $name from item")
            }
        }*/
    }

    fun toTag(): ListTag {
        val tag = ListTag()
        this.attachments.forEach { (attachment, instance) ->
            val baseTag = CompoundTag()
            baseTag.putString(MobMincerEntity.ATTACHMENTS_TYPE_TAG, attachment.name)
            val attachmentTag = CompoundTag()
            instance.serialize(attachmentTag)
            baseTag.put(MobMincerEntity.ATTACHMENTS_DATA_TAG, attachmentTag)
            tag.add(baseTag)
        }
        return tag
    }

    fun fromTag(tag: ListTag) {
        tag.forEach { attachmentTag ->
            attachmentTag as CompoundTag
            nbtDataFixer(attachmentTag)
            val attachment = Attachments.valueOf(attachmentTag.getString(MobMincerEntity.ATTACHMENTS_TYPE_TAG))
            AttachmentRegistry.get(attachment)?.create(mobMincer)?.let {
                it.deserialize(attachmentTag.getCompound(MobMincerEntity.ATTACHMENTS_DATA_TAG), mobMincer)
                this.attachments[attachment] = it
            }
        }
    }

    fun loadAttachment(attachment: Attachments, tag: CompoundTag) {
        if (this.hasAttachment(attachment)) {
            this.getAttachment<AttachmentInstance>(attachment)!!.deserialize(tag, mobMincer)
        } else {
            val instance = AttachmentRegistry.get(attachment)!!.create(mobMincer)
            instance.deserialize(tag, mobMincer)
            this.attachments[attachment] = instance
        }
    }

    private fun nbtDataFixer(attachmentTag: CompoundTag) {
        if (attachmentTag.contains("type")) {
            attachmentTag.putString(MobMincerEntity.ATTACHMENTS_TYPE_TAG, attachmentTag.getString("type"))
            attachmentTag.remove("type")
        }
        if (attachmentTag.contains("data")) {
            attachmentTag.put(MobMincerEntity.ATTACHMENTS_DATA_TAG, attachmentTag.getCompound("data"))
            attachmentTag.remove("data")
        }
    }

    val values: Collection<AttachmentInstance>
        get() = this.attachments.values

    fun isEmpty(): Boolean = this.attachments.isEmpty()
}
