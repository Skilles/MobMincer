package net.mobmincer.core.registry

import net.mobmincer.core.attachment.*
import java.util.*

object AttachmentRegistry {
    private val attachments: MutableMap<Attachments, MobMincerAttachment<*>> = EnumMap(Attachments::class.java)

    val STORAGE: MobMincerAttachment<StorageAttachment> =
        registerAttachment(MobMincerAttachment.Builder.of(Attachments.STORAGE, ::StorageAttachment))

    val PACIFIER: MobMincerAttachment<PacifierAttachment> =
        registerAttachment(MobMincerAttachment.Builder.of(Attachments.PACIFIER, ::PacifierAttachment))

    val FEEDER: MobMincerAttachment<FeederAttachment> =
        registerAttachment(MobMincerAttachment.Builder.of(Attachments.FEEDER, ::FeederAttachment))

    val SPREADER: MobMincerAttachment<SpreaderAttachment> =
        registerAttachment(MobMincerAttachment.Builder.of(Attachments.SPREADER, ::SpreaderAttachment))

    private fun <T : AttachmentInstance> registerAttachment(builder: MobMincerAttachment.Builder<T>): MobMincerAttachment<T> {
        val mincerAttachment = builder.build()
        attachments[builder.type] = mincerAttachment
        return mincerAttachment
    }

    fun get(attachment: Attachments): MobMincerAttachment<*>? {
        return attachments[attachment]
    }

    fun getName(attachment: MobMincerAttachment<*>): String {
        return attachments.entries.first { it.value == attachment }.key.name
    }

    fun getEntries(): Set<Map.Entry<Attachments, MobMincerAttachment<*>>> {
        return attachments.entries
    }
}
