package net.mobmincer.core.attachment

import net.mobmincer.core.entity.MobMincerEntity

class MobMincerAttachment<T : AttachmentInstance>(private val factory: AttachmentFactory<T>) {

    fun create(mincer: MobMincerEntity): T {
        return this.factory.create(this, mincer)
    }

    fun interface AttachmentFactory<T : AttachmentInstance> {
        fun create(attachment: MobMincerAttachment<T>, mincer: MobMincerEntity): T
    }
}
