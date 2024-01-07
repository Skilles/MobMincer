package net.mobmincer.core.attachment

import net.minecraft.network.chat.Component
import net.mobmincer.core.entity.MobMincerEntity

class MobMincerAttachment<T : AttachmentInstance>(type: Attachments, private val factory: AttachmentFactory<T>, val name: Component) {
    val item = type.item

    fun create(mincer: MobMincerEntity): T {
        return this.factory.create(this, mincer)
    }

    class Builder<T : AttachmentInstance>(val type: Attachments, private val factory: AttachmentFactory<T>) {

        var name: Component? = null

        fun withName(name: Component): Builder<T> {
            this.name = name
            return this
        }

        fun build(): MobMincerAttachment<T> {
            return MobMincerAttachment(
                type,
                factory,
                name ?: Component.translatable("mobmincer.attachment.${type.name.lowercase()}")
            )
        }

        companion object {
            fun <T : AttachmentInstance> of(type: Attachments, factory: AttachmentFactory<T>): Builder<T> {
                return Builder(type, factory)
            }
        }
    }

    fun interface AttachmentFactory<T : AttachmentInstance> {
        fun create(attachment: MobMincerAttachment<T>, mincer: MobMincerEntity): T
    }
}
