package net.mobmincer.core.attachment

import java.util.*

internal object AttachmentRegistry {
    private val attachments: MutableMap<Attachments, MobMincerAttachment<*>> = EnumMap(Attachments::class.java)

    val STORAGE: MobMincerAttachment<StorageAttachment> =
        registerAttachment(Attachments.STORAGE, MobMincerAttachment(::StorageAttachment))

    private fun <T : AttachmentInstance> registerAttachment(attachment: Attachments, mobMincerAttachment: MobMincerAttachment<T>): MobMincerAttachment<T> {
        attachments[attachment] = mobMincerAttachment
        return mobMincerAttachment
    }

    fun get(attachment: Attachments): MobMincerAttachment<*>? {
        return attachments[attachment]
    }
}
