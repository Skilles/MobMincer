package net.mobmincer.core.attachment

import java.util.*

internal object AttachmentRegistry {
    private val attachments: MutableMap<Attachments, MobMincerAttachment<*>> = EnumMap(Attachments::class.java)

    val STORAGE: MobMincerAttachment<StorageAttachment> =
        registerAttachment(MobMincerAttachment.Builder.of(Attachments.STORAGE, ::StorageAttachment))

    private fun <T : AttachmentInstance> registerAttachment(builder: MobMincerAttachment.Builder<T>): MobMincerAttachment<T> {
        val mincerAttachment = builder.build()
        attachments[builder.type] = mincerAttachment
        return mincerAttachment
    }

    fun get(attachment: Attachments): MobMincerAttachment<*>? {
        return attachments[attachment]
    }
}
