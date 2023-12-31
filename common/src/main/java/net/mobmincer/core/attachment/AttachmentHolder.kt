package net.mobmincer.core.attachment

import net.mobmincer.core.entity.MobMincerEntity

class AttachmentHolder(mobMincer: MobMincerEntity) {
    private val attachments = mutableListOf<MobMincerAttachment>()

    fun addAttachment(attachment: MobMincerAttachment) {
        this.attachments.add(attachment)
    }

    fun removeAttachment(attachment: MobMincerAttachment) {
        this.attachments.remove(attachment)
    }

    fun hasAttachment(attachmentClass: Class<out MobMincerAttachment>): Boolean {
        return this.attachments.any { attachmentClass.isInstance(it) }
    }
}