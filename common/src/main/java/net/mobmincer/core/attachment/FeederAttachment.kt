package net.mobmincer.core.attachment

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.entity.MobMincerEntity

class FeederAttachment(type: MobMincerAttachment<*>, mincer: MobMincerEntity) : AttachmentInstance(type, mincer) {

    override fun onMince(dealtDamage: Float) {
        mincer.target.heal(dealtDamage * MobMincerConfig.CONFIG.feederHealPercent.get().toFloat())
        mincer.level().playSound(null, mincer.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0f, 1.0f)
    }
}