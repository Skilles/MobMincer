package net.mobmincer.core.attachment

import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.mobmincer.core.entity.MobMincerEntity

class SpreaderAttachment(type: MobMincerAttachment<*>, mincer: MobMincerEntity) : AttachmentInstance(type, mincer) {

    override fun onDeath(reason: MobMincerEntity.DestroyReason): Boolean {
        if (reason != MobMincerEntity.DestroyReason.TARGET_KILLED) {
            return super.onDeath(reason)
        }

        // Get nearby mobs of the same type and attach a MobMincer to one of them
        val nearbyMobs = this.mincer.level().getEntitiesOfClass(
            this.mincer.target.javaClass,
            this.mincer.boundingBox.inflate(5.0)
        )
            .filter { MobMincerEntity.canAttach(it, mincer.sourceStack) }
            .sortedBy { it.distanceTo(this.mincer) }

        if (nearbyMobs.isNotEmpty()) {
            val mob = nearbyMobs[0] as LivingEntity
            this.mincer.changeTarget(mob)
            this.mincer.level().playSound(
                null,
                this.mincer.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                1.0f,
                1.5f
            )

            // Skip killing the mincer
            return true
        }

        return super.onDeath(reason)
    }
}
