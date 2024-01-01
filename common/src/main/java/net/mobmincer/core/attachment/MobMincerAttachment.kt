package net.mobmincer.core.attachment

class MobMincerAttachment<T : AttachmentInstance>(private val factory: AttachmentFactory<T>) {

    fun create(): T {
        return this.factory.create(this)
    }

    fun interface AttachmentFactory<T : AttachmentInstance> {
        fun create(attachment: MobMincerAttachment<T>): T
    }
}
